package de.ancash.fancycrafting.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IGUI{
    
	private final HumanEntity owner;
	private Inventory openInventory;
	
	public IGUI(HumanEntity owner, ItemStack[] template, String name, int size) {
		this.owner = owner;
		if(owner == null) return;
		openInventory = Bukkit.createInventory(null, size, name);
		openInventory.setContents(template);
		owner.openInventory(openInventory);
	}

	public Inventory getInventory() {
		return openInventory;
	}
	
	public HumanEntity getOwner() {
		return owner;
	}
}