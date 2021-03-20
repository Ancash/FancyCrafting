package de.ancash.fancycrafting.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IGUI{
    
	private final HumanEntity owner;
	private final Inventory openInventory;
	
	public IGUI(Player owner, ItemStack[] template, String name, int size) {
		this.owner = owner;
		openInventory = Bukkit.createInventory(null, size, name);
		openInventory.setContents(template);
		owner.openInventory(openInventory);
	}

    public void onClose(Integer[] craftingSlots) {
    	for(int i : craftingSlots) {
    		ItemStack is = openInventory.getItem(i);
    		if(is == null) continue;
    		if(owner.getInventory().firstEmpty() != -1) {
    			owner.getInventory().addItem(is);
    		} else {
    			owner.getWorld().dropItem(owner.getLocation(), is);
    		}
    	}
    }

	public HumanEntity getOwner() {
		return owner;
	}
}