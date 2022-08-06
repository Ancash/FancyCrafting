package de.ancash.fancycrafting.gui.base;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.minecraft.inventory.IGUI;

public class AbstractManageVanillaRecipesGUI extends IGUI{

	protected final AbstractFancyCrafting pl;
	
	public AbstractManageVanillaRecipesGUI(AbstractFancyCrafting pl, Player player, int size, String title) {
		super(player.getUniqueId(), 54, pl.getWorkspaceObjects().getManageVanillaRecipesTitle());
		this.pl = pl;
	}

	@Override
	public void onInventoryClick(InventoryClickEvent arg0) {
		arg0.setCancelled(true);
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent arg0) {
		
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent arg0) {
		arg0.setCancelled(true);
	}

}