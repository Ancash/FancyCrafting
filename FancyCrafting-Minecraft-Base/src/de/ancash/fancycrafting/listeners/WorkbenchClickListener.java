package de.ancash.fancycrafting.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.WorkspaceTemplate;

public class WorkbenchClickListener implements Listener {

	private final AbstractFancyCrafting plugin;

	public WorkbenchClickListener(AbstractFancyCrafting plugin) {
		this.plugin = plugin;
	}

	/*@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;
		if (event.getClickedBlock() == null)
			return;
		if (!event.getClickedBlock().getType().equals(XMaterial.CRAFTING_TABLE.parseMaterial()))
			return;
		event.setCancelled(true);
		plugin.openCraftingWorkspace(event.getPlayer(), WorkspaceTemplate
				.get(plugin.getDefaultDimension().getWidth(), plugin.getDefaultDimension().getHeight()));
	}*/
	
	@EventHandler
	public void onOpen(InventoryOpenEvent e) {
		if(e.getInventory().getType() == InventoryType.WORKBENCH) {
			e.setCancelled(true);
			plugin.openCraftingWorkspace((Player) e.getPlayer(), WorkspaceTemplate
					.get(plugin.getDefaultDimension().getWidth(), plugin.getDefaultDimension().getHeight()));
		}
	}
}