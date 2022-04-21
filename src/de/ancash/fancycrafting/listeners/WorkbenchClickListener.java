package de.ancash.fancycrafting.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.ICraftingGUI;
import de.ancash.minecraft.XMaterial;

public class WorkbenchClickListener implements Listener{

	private final FancyCrafting plugin;
	
	public WorkbenchClickListener(FancyCrafting plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if(event.getClickedBlock() == null) return;
		if(!event.getClickedBlock().getType().equals(XMaterial.CRAFTING_TABLE.parseMaterial())) return;
		event.setCancelled(true);
		new ICraftingGUI(plugin, event.getPlayer(), CraftingTemplate.get(plugin.getDefaultTemplateWidth(), plugin.getDefaultTemplateHeight()));
	}
}