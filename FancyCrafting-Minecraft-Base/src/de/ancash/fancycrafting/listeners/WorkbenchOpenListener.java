package de.ancash.fancycrafting.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.gui.WorkspaceTemplate;

public class WorkbenchOpenListener implements Listener {

	private final AbstractFancyCrafting plugin;

	public WorkbenchOpenListener(AbstractFancyCrafting plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent e) {
		if(e.getInventory().getType() == InventoryType.WORKBENCH) {
			e.setCancelled(true);
			plugin.openCraftingWorkspace((Player) e.getPlayer(), WorkspaceTemplate
					.get(plugin.getDefaultDimension().getWidth(), plugin.getDefaultDimension().getHeight()));
		}
	}
}