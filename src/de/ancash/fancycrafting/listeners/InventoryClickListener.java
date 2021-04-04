package de.ancash.fancycrafting.listeners;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
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
	public void onInvClick(InventoryClickEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException, de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException {
		if(plugin.getWorkbenchGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getWorkbenchGUI().onWorkbenchClick(event);
			return;
		}
		if(plugin.getRecipeCreateGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeCreateGUI().onClick(event);
			return;
		}
		if(plugin.getRecipeEditGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeEditGUI().onClick(event);
			return;
		}
		if(plugin.getRecipeViewGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeViewGUI().onClick(event);
			return;
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException {
		if(plugin.getWorkbenchGUI().hasInventoryOpen(event.getPlayer())) {
			plugin.getWorkbenchGUI().close(event.getPlayer(), true);
			return;
		}
		if(plugin.getRecipeCreateGUI().hasInventoryOpen(event.getPlayer())) {
			plugin.getRecipeCreateGUI().close(event.getPlayer(), true);
			return;
		}
		if(plugin.getRecipeEditGUI().hasInventoryOpen(event.getPlayer())) {
			plugin.getRecipeEditGUI().close(event.getPlayer(), true);
			return;
		}
		if(plugin.getRecipeViewGUI().hasInventoryOpen(event.getPlayer())) {
			plugin.getRecipeViewGUI().close(event);
			return;
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if(plugin.getWorkbenchGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getWorkbenchGUI().onWorkbenchDrag(event);
			return;
		}
		if(plugin.getRecipeCreateGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeCreateGUI().onDrag(event);
			return;
		}
		if(plugin.getRecipeEditGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeEditGUI().onDrag(event);
			return;
		}
		if(plugin.getRecipeViewGUI().hasInventoryOpen(event.getWhoClicked())) {
			plugin.getRecipeViewGUI().onDrag(event);
			return;
		}
	}
}
