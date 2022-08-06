package de.ancash.fancycrafting.gui.base.handler;

import java.util.Set;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface IAutoRecipeMatcherHandler {

	public void autoMatch();

	public void onAutoMatchFinish();

	public void onAutoRecipesChangePage(InventoryClickEvent e);

	public Set<Integer> getResutlHashCodes();
}