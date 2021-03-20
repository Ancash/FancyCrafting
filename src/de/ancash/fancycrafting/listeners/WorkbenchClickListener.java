package de.ancash.fancycrafting.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.ancash.fancycrafting.FancyCrafting;

public class WorkbenchClickListener implements Listener{

	private final FancyCrafting plugin;
	
	public WorkbenchClickListener(FancyCrafting plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if(event.getClickedBlock() == null) return;
		if(!event.getClickedBlock().getType().equals(Material.WORKBENCH)) return;
		event.setCancelled(true);
		plugin.getWorkbenchGUI().open(event.getPlayer());
	}
	
}
