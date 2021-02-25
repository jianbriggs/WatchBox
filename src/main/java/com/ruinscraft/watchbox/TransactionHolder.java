package com.ruinscraft.watchbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TransactionHolder {
	private final UUID playerUUID;
	private final Chest chest;
	private HashMap<String, Integer> itemMap;
	
	public TransactionHolder(UUID playerUUID, Chest chest) {
		this.playerUUID = playerUUID;
		this.chest = chest;
		this.itemMap = new HashMap<String, Integer>();
	}
	
	public void process(String itemName, int amount) {
		if(!itemMap.containsKey(itemName)) {
			itemMap.put(itemName, amount);
		}
		else {
			int oldAmount = itemMap.get(itemName);
			itemMap.put(itemName, oldAmount + amount);
		}
	}
	
	public ArrayList<String> formattedList(){
		ArrayList<String> output = new ArrayList<String>();
		for(String item : itemMap.keySet()) {
			int quantity = itemMap.getOrDefault(item, 0);
			
			if(quantity < 0) {
				output.add(String.format("%s removed %dx %s", Bukkit.getOfflinePlayer(playerUUID).getName(), -1*(quantity), item));
			}
			else if(quantity > 0) {
				output.add(String.format("%s placed %dx %s", Bukkit.getOfflinePlayer(playerUUID).getName(), quantity, item));
			}
		}
		
		return output;
	}
	
	public Chest getChest() {
		return this.chest;
	}
	
	public UUID getPlayerUUID() {
		return this.playerUUID;
	}
}
