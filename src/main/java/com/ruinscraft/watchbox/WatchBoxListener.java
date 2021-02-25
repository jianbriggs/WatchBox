package com.ruinscraft.watchbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;


import net.md_5.bungee.api.ChatColor;

public class WatchBoxListener implements Listener{
	
	private WatchBox plugin;
	
	private HashMap<Player, TransactionHolder> activeTransactions = new HashMap<Player, TransactionHolder>();
	
	private final String PLUGIN_NAME = "WatchBox";
	
	private final String WATCH_SIGN_TEXT_IDENTIFIER = "[WatchBox]";
	public final String WATCH_SIGN_FULL_IDENTIFIER = "" + ChatColor.DARK_RED + WATCH_SIGN_TEXT_IDENTIFIER;
	public final String WATCH_SIGN_OWNER_COLOR = "" + ChatColor.DARK_BLUE;
	
	private final String MSG_PLAYER_SIGN_SELECTED = "" + ChatColor.GREEN + "WatchBox Selected";
	private final String MSG_WATCH_SIGN_REMOVED = "" + ChatColor.RED + "One of your WatchBox chests has been removed by %s!";
	
    public WatchBoxListener(WatchBox plugin) {
    	this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
    	Player player = evt.getPlayer();
    	this.plugin.getSelectedSignController().removeSelection(player);
    }
    
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent evt){
    	Player caller = (Player) evt.getPlayer();
    	
        if (evt.getInventory().getHolder() instanceof Chest || evt.getInventory().getHolder() instanceof DoubleChest){
            Chest chestBlock = (Chest) evt.getInventory().getHolder();
            Sign watchSign = getAttachedWatchSign(chestBlock);
	    	if(watchSign != null && caller.isOnline()) {
	    		TransactionHolder th = new TransactionHolder(caller.getUniqueId(), chestBlock);
	    		this.activeTransactions.put(caller, th);
	    		// TODO: debug
	    		caller.sendMessage("(Debug) New transaction holder created");
	    		////
	    	}
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
    	Player caller = (Player) evt.getPlayer();
    	
        if (evt.getInventory().getHolder() instanceof Chest || evt.getInventory().getHolder() instanceof DoubleChest){
            Chest chestBlock = (Chest) evt.getInventory().getHolder();
            Sign watchSign = getAttachedWatchSign(chestBlock);
	    	if(watchSign != null && caller.isOnline()) {
	    		if(activeTransactions.containsKey(caller)) {
	    			TransactionHolder th = activeTransactions.get(caller);
	    			activeTransactions.remove(caller);
	    			
	    			for(String log : th.formattedList()) {
	    				caller.sendMessage(ChatColor.AQUA + log);
	    			}
	    		}
	    	}
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
    	Player caller = (Player) evt.getWhoClicked();
    	
    	Inventory topInventory = evt.getView().getTopInventory();
    	Inventory bottomInventory = evt.getView().getBottomInventory();
    	Inventory clickedInventory = evt.getClickedInventory();
    	ItemStack itemInSlot = evt.getCursor();
    	ItemStack itemInHand = evt.getCurrentItem();
    	
    	if(topInventory.getHolder() instanceof Chest || topInventory.getHolder() instanceof DoubleChest) {
    		// get the chest block, as we will need to get sign information from it
	    	Chest openChestBlock = (Chest) topInventory.getHolder();
	    	Sign watchSign = getAttachedWatchSign(openChestBlock);
	    	if(watchSign != null) {
		    	String action = evt.getAction().name();
		    	// TODO: debug
		    	caller.sendMessage("(Debug) Action is " + action);
		    	if(itemInSlot != null && itemInHand != null) {
		    		caller.sendMessage("Item in slot: " + itemInSlot.getAmount() + "x " + itemInSlot.getType() + " | Item in hand: " + itemInHand.getAmount() + "x "+ itemInHand.getType());
		    	}
		    	////
		    	TransactionHolder th = activeTransactions.get(caller);
		    	
		    	// we only really need to monitor actions involving the top (chest) inventory
		    	if(clickedInventory != null) {
			    	if(clickedInventory.equals(topInventory)) {
			    		
			    		if(action.equals("PLACE_ONE")) {
			    			th.process(itemInSlot.getType().name(), 1);
			    		}
			    		else if(action.equals("PLACE_ALL") || action.equals("PLACE_SOME")) {
				    		th.process(itemInSlot.getType().name(), itemInSlot.getAmount());
				    	}
				    	else if(action.equals("MOVE_TO_OTHER_INVENTORY") || action.equals("PICKUP_ONE") || action.equals("PICKUP_ALL") || action.equals("PICKUP_SOME")
				    			|| action.equals("DROP_ALL_SLOT") || action.equals("DROP_ALL_CURSOR") || action.equals("HOTBAR_MOVE_AND_READD") || action.equals("HOTBAR_SWAP")) {
				    		th.process(itemInHand.getType().name(), -1*itemInHand.getAmount());
				    	}
				    	else if(action.equals("DROP_ONE_SLOT") || action.equals("DROP_ONE_CURSOR")) {
				    		th.process(itemInHand.getType().name(), -1);
				    	}
				    	// TODO: add more conditionals for other actions
			    	}
			    	else if(clickedInventory.equals(bottomInventory)) {
			    		if(action.equals("MOVE_TO_OTHER_INVENTORY")) {
			    			th.process(itemInHand.getType().name(), itemInHand.getAmount());
			    		}
			    	}
		    	}
	    	}
	    	else {
	    		// TODO: debug
	    		caller.sendMessage("(Debug) This chest does not have a WatchBox sign on it");
	    	}
    	}
    }
    
    /**
     * Attempts to find any WatchBox signs attached to a chest block
     * @param openChest - Chest object to inspect
     * @return Sign containing WatchBox information, null on failure
     */
    private Sign getAttachedWatchSign(Chest openChest) {
		ArrayList<Block> attached = new ArrayList<Block>();
		
		Block chestBlock = openChest.getBlock();
		
		if(blockIsSign(chestBlock.getRelative(BlockFace.WEST))) {
			attached.add(chestBlock.getRelative(BlockFace.WEST));
		}
		else if(blockIsSign(chestBlock.getRelative(BlockFace.EAST))) {
			attached.add(chestBlock.getRelative(BlockFace.EAST));
		}
		else if(blockIsSign(chestBlock.getRelative(BlockFace.SOUTH))) {
			attached.add(chestBlock.getRelative(BlockFace.SOUTH));
		}
		else if(blockIsSign(chestBlock.getRelative(BlockFace.NORTH))) {
			attached.add(chestBlock.getRelative(BlockFace.NORTH));
		}
		// TODO: are diagonal blocks counted for double chests?
		
		for(Block b : attached) {
			Sign s = (Sign) b.getState();
			
			if(signIsWatchSign(s)) {
				return s;
			}
		}
		
		return null;
	}

	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();
        
        if(evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	Block clickedBlock = evt.getClickedBlock();
        	
        	if(blockIsSign(clickedBlock)) {
            	// a shop is defined as a sign (formatted)
            	// and a chest block immediately below it.
                Sign sign = (Sign) clickedBlock.getState();

                if(signIsWatchSign(sign)) {
                	
                    org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) sign.getData();
                    Block block = null;
                    
                    // first, get the block that the sign is attached to
                    block = clickedBlock.getRelative(signMaterial.getAttachedFace());
                    
	                if(blockIsChest(block)) {
	                	// TODO: get WatchBox info from database lookup
	                }
                }
        	}
        }
    }
    
	@EventHandler
    public void onBlockBreak(BlockBreakEvent evt) {
    	Player player = evt.getPlayer();
    	Block  block  = evt.getBlock();
    }
	
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent evt) {
    	Player player = evt.getPlayer();
    	Block block = evt.getBlock();
    	
    	if(block.getState() instanceof Sign) {
	    	if(validateWatchSignEntry(evt.getLines())) {
	    		Sign sign = (Sign) block.getState();
	    		org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) sign.getData();
                Block blockToCheck = null;
                
                // first, get the block that the sign is attached to
                blockToCheck = block.getRelative(signMaterial.getAttachedFace());
                
                if(blockIsChest(blockToCheck)) {
                	playerConfirmCreation(player, sign);
                }
	    	}
    	}
    }
    
    private void playerConfirmCreation(Player player, Sign sign) {
		if(player.isOnline()) {
			String border = coloredTextBorder(ChatColor.DARK_RED, ChatColor.DARK_GRAY, 24);
			player.sendMessage(border);
			player.sendMessage(ChatColor.GRAY + "A new " + ChatColor.WHITE + "WatchBox " + ChatColor.GRAY + "chest will cost $" + this.plugin.getConfig().getInt("watchbox.chest-cost"));
			player.sendMessage("");
			player.sendMessage(ChatColor.GRAY + "To confirm, type " + ChatColor.GREEN + "/watchbox confirm");
			player.sendMessage(ChatColor.GRAY + "To cancel, simply remove this sign");
			player.sendMessage(border);
			
			this.plugin.getSelectedSignController().addSelection(player, sign);
		}
	}
    
	private String coloredTextBorder(ChatColor color1, ChatColor color2, int length) {
		String border = "";
		for(int i = 0; i < length; i++) {
			border += color1 + "-" + color2 + "-";
		}
		return border;
	}

	private String getItemDisplayName(ItemStack item) {
    	ItemMeta meta = item.getItemMeta();
    	Material mat = item.getType();
    	
    	// Custom/display names
		if(meta.hasDisplayName()) {
			return "" + ChatColor.ITALIC + meta.getDisplayName();
		}
		// Written book names
		else if(itemIsFinishedBook(item)) {
			BookMeta bookMeta = (BookMeta) meta;
			if(bookMeta.hasTitle()) {
				return "" + ChatColor.ITALIC + bookMeta.getTitle();
			}
		}
		// Potion names
		else if(itemIsPotion(item)) {
			return "" + ChatColor.DARK_RED + getPotionName(item);
		}
		// fix for raw meats
		else if((mat.name().contains("PORK") || mat.name().contains("CHICKEN") || mat.name().contains("MUTTON") || mat.name().contains("BEEF") || mat.name().equals("RABBIT") || mat.name().contains("SALMON") || mat.name().contains("COD")) && !mat.name().contains("COOKED")) {
			XMaterial itemMaterial = XMaterial.matchXMaterial(item);
			return "Raw " + materialPrettyPrint(itemMaterial.parseMaterial());
		}
		// fix for chestplates
		else if(mat.name().contains("CHESTPLATE")) {
			XMaterial itemMaterial = XMaterial.matchXMaterial(item);
			String[] temp = materialPrettyPrint(itemMaterial.parseMaterial()).split(" ");
			return temp[0] + " Chest.";
		}
		// fix for green, red, and yellow dyes
		else if(mat.name().equals("CACTUS_GREEN")) {
			return "Green Dye";
		}
		else if(mat.name().equals("DANDELION_YELLOW")) {
			return "Yellow Dye";
		}
		else if(mat.name().equals("ROSE_RED")) {
			return "Red Dye";
		}
		
		XMaterial itemMaterial = XMaterial.matchXMaterial(item);
		return materialPrettyPrint(itemMaterial.parseMaterial());
	}
	
    /**
     * Checks if a given sign's text is formatted for a watch sign
     * @param sign
     * @return
     */
    private boolean validateWatchSignEntry(String[] lines) {
    	return lines[0].equalsIgnoreCase(WATCH_SIGN_TEXT_IDENTIFIER);
    }
    /**
     * Checks if a given sign is formatted as a Watch Sign
     * @param sign - Sign block
     * @return True if sign matches validation, false otherwise
     */
    private boolean signIsWatchSign(Sign sign) {
    	return sign.getLine(0).equals(WATCH_SIGN_FULL_IDENTIFIER);
    }
	/**
	 * Checks whether a block is a sign.
	 * @param block
	 * @return True if block is sign, False otherwise
	 */
    private boolean blockIsSign(Block block) {
    	return block != null && (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN));
    }
    
    /**
     * Checks whether a block is a chest or double chest
     * (note that Enderchests are not checked)
     * @param block - Block to check
     * @return True if block is chest, False otherwise
     */
    private boolean blockIsChest(Block block) {
    	return block != null && (block.getState() instanceof Chest || block.getState() instanceof DoubleChest);
    }
    
    private String materialPrettyPrint(Material material) {
    	String[] words = material.toString().split("_");
    	String output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }
    
	private boolean playerCanStoreItem(Player player, ItemStack itemToBuy, short quantity) {
		PlayerInventory inv = player.getInventory();
		int maxStackSize = itemToBuy.getMaxStackSize();

		for(ItemStack item : inv.getContents()) {
			if(item == null || itemIsAir(item)) {
				continue;
			}
			// check if the slot's amount + quantity is
			// less than or equal to 64 (full stack)
			else if(item.isSimilar(itemToBuy) && item.getAmount() + quantity <= maxStackSize) {
				return true;
			}
		}
		
		// otherwise, return if there's a free, empty slot
		return inv.firstEmpty() >= 0;
	}

	private String truncateText(String message) {
    	if(message.length() >= 38) {
    		return message.substring(0, 34) + "...";
    	}
    	else {
    		return message;
    	}
    }
    
    private String prettyPrint(String message) {
    	String[] words = message.split("_");
    	String  output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }

    private boolean itemIsFinishedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITTEN_BOOK.parseMaterial());
    }
    
    private boolean itemIsWritableBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITABLE_BOOK.parseMaterial());
    }
    
    private boolean itemIsAir(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.AIR.parseMaterial());
    }
    
    private boolean itemIsBanner(ItemStack item) {
    	return item != null && item.getType().name().contains("BANNER");
    }
    
    private boolean itemIsShield(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.SHIELD.parseMaterial());
    }
    
    private boolean itemIsPotion(ItemStack item) {
		return item != null && item.getType().name().contains("POTION");
	}
    
    private boolean itemIsFilledMap(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.FILLED_MAP.parseMaterial());
    }
    
    private boolean itemIsShulkerBox(ItemStack item) {
    	return item != null && item.getType().name().contains("SHULKER_BOX");
    }
    
    private boolean itemIsEnchantedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.ENCHANTED_BOOK.parseMaterial());
    }
    
    private boolean itemIsTippedArrow(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.TIPPED_ARROW.parseMaterial());
    }
    
    private String getPotionName(ItemStack potion) {
    	PotionMeta meta = (PotionMeta) potion.getItemMeta();
    	String name = prettyPrint(meta.getBasePotionData().getType().name());
    	
    	if(meta.getBasePotionData().isUpgraded()) {
    		name += " II";
    	}
    	
    	if(meta.getBasePotionData().isExtended()) {
    		name += " (Extended)";
    	}
    	
    	return name;
    }
}