package de.ancash.fancycrafting.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import de.ancash.fancycrafting.FancyCrafting;

public class InventoryClickListener implements Listener{

	private final FancyCrafting plugin;
	
	public InventoryClickListener(FancyCrafting plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if(!plugin.getWorkbenchGUI().hasInventoryOpen(event.getWhoClicked())) return;
		plugin.getWorkbenchGUI().onWorkbenchClick(event);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!plugin.getWorkbenchGUI().hasInventoryOpen(event.getPlayer())) return;
		plugin.getWorkbenchGUI().close(event.getPlayer(), true);
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if(!plugin.getWorkbenchGUI().hasInventoryOpen(event.getWhoClicked())) return;
		plugin.getWorkbenchGUI().onWorkbenchDrag(event);
	}
}
