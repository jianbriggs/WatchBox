package com.ruinscraft.watchbox;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SelectedSignController {
	private HashMap<Player, Sign> selectionMap;
	
	public SelectedSignController() {
		this.selectionMap = new HashMap<Player, Sign>();
	}
	
	public void addSelection(Player player, Sign lsign) {
		selectionMap.put(player, lsign);
	}
	
	public void removeSelection(Player player) {
		if(playerHasSelection(player)) {
			selectionMap.remove(player);
		}
	}
	
	public void removeShop(Sign lsign) {
		for(Entry<Player, Sign> e : selectionMap.entrySet()) {
			if(e.getValue().equals(lsign)) {
				selectionMap.remove(e.getKey());
			}
		}
	}
	
	public Sign getSelection(Player player) {
		if(playerHasSelection(player)) {
			return selectionMap.get(player);
		}
		return null;
	}
	
	public boolean playerHasSelection(Player player) {
		return this.selectionMap.containsKey(player);
	}
}
