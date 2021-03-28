package de.ancash.fancycrafting.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IGUI{
    
	private HumanEntity owner;
	private Inventory openInventory;
	
	public IGUI(HumanEntity owner, ItemStack[] template, String name, int size) {
		if(owner == null) return;
		this.owner = owner;
		openInventory = Bukkit.createInventory(null, size, name);
		openInventory.setContents(template);
		owner.openInventory(openInventory);
	}

	public Inventory getInventory() {
		return openInventory;
	}
	
	public void setContent(ItemStack[] contents) {
		this.openInventory.setContents(contents);
	}
	
	public HumanEntity getOwner() {
		return owner;
	}
}