package com.ruinscraft.watchbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class WatchBoxCommandExecutor implements CommandExecutor, TabCompleter{
	private final WatchBox plugin;
	
	private final String PLUGIN_BANNER = "" + ChatColor.GOLD + ".;o'--------------[ WatchBox ]--------------'o;.";

	private final List<String> tabOptions;
	private final List<String> adminTabOptions;
	
	public WatchBoxCommandExecutor(WatchBox plugin) {
		this.plugin = plugin;
		this.tabOptions = new ArrayList<String>();
		this.adminTabOptions = new ArrayList<String>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length >= 1) {
			switch(args[0].toLowerCase()){
				case "confirm":
					confirmWatchSignCreation(player);
					break;
				case "encode":
					itemEncode(player);
					break;
				case "decode":
					itemDecode(player);
					break;
				default:
					showHelp(player);
					break;
			}
		}
		else {
			showHelp(player);
		}
		
		return true;
	}
	

	private void confirmWatchSignCreation(Player player) {
		if(this.plugin.getSelectedSignController().playerHasSelection(player)) {
			if(player.isOnline()) {
				ItemStack creationCost = XMaterial.GOLD_INGOT.parseItem();
				PlayerInventory playerInventory = player.getInventory();
				if(playerInventory.containsAtLeast(creationCost, this.plugin.getConfig().getInt("watchbox.chest-cost"))) {
					creationCost.setAmount(this.plugin.getConfig().getInt("watchbox.chest-cost"));
					playerInventory.removeItem(creationCost);
					
					Sign sign = this.plugin.getSelectedSignController().getSelection(player);
					
					String[] lines = sign.getLines();
					
					lines[0] = this.plugin.getWatchBoxListener().WATCH_SIGN_FULL_IDENTIFIER;
					lines[1] = this.plugin.getWatchBoxListener().WATCH_SIGN_OWNER_COLOR + player.getName();
					sign.update();

					player.sendMessage(ChatColor.GREEN + "WatchBox successfully created!");
					this.plugin.getSelectedSignController().removeSelection(player);
				}
				else {
					player.sendMessage(ChatColor.RED + "Sorry, but you do not have enough money.");
				}
			}
		}
		else {
			if(player.isOnline()) {
				player.sendMessage(ChatColor.RED + "You need to create a new WatchBox sign in order to confirm!");
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String option : tabOptions) {
                if (option.startsWith(args[0].toLowerCase())) {
                    completions.add(option);
                }
            }

        }
        
        return completions;
	}
	
	private void showHelp(Player player) {
		String[] commandHelpBase = {
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " withdraw ($)" + ChatColor.GRAY + ": Removes money from your ledger",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance" + ChatColor.GRAY + ": Check your ledger balance",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " top" + ChatColor.GRAY + ": View top 10 earners"
		};
		
		String[] commandHelpAdmin = {
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " view recent" + ChatColor.GRAY + ": View ten most recent transactions for a shop",
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " history (player)" + ChatColor.GRAY + ": View ten most recent transactions made by a player",
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance (player)" + ChatColor.GRAY + ": Check a player's ledger balance"    
		};
			
		if(player.isOnline()) {
			player.sendMessage("Showing WatchBox help");
		}
	}
	
	/**
	 * Experimental - encodes/serializes an ItemStack in player's hand
	 * @param player
	 */
	private void itemEncode(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack heldItem = inventory.getItemInMainHand();
		List<String> data = CryptoSecure.encodeItemStack(heldItem);
		
		ItemStack holder = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) holder.getItemMeta();
		meta.setAuthor("WATCHBOX");
		meta.setTitle("(Placeholder)");
		meta.setPages(data);
		holder.setItemMeta(meta);
		
		inventory.addItem(holder);
	}
	
	/**
	 * Experimental - decodes/deserializes an ItemStack from Book string
	 * @param player
	 */
	private void itemDecode(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack heldItem = inventory.getItemInMainHand();

		if(heldItem.getItemMeta() instanceof BookMeta) {
			BookMeta meta = (BookMeta) heldItem.getItemMeta();
			if(meta.hasPages()) {
				List<String> data = meta.getPages();
				ItemStack decode = CryptoSecure.decodeItemStack(data);
				
				if(decode != null) {
					inventory.addItem(decode);
				}
			}
		}
		else {
			return;
		}
	}
	/**
	 * Checks if a given string is a number
	 * 
	 * @param str - String to check
	 * @return True if the string consists of only numbers, False otherwise
	 */
	private boolean stringIsNumeric(String str) {
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Safely converts a string consisting of numeric values
	 * into an integer. If the value of the number is greater than
	 * an integer's max value, it will truncate the value to it.
	 * 
	 * @param str - String to check for numeric value
	 * @return int value of the string, or -1 on error
	 */
	private int safeStringToInt(String str) {
		if(stringIsNumeric(str)) {
			if(str.length() > 10) {
				str = str.substring(0, 10);
			}

			if(Double.parseDouble(str) > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE - 1;
			}
			else {
				return Integer.parseInt(str);
			}
		}
		return -1;
	}
}