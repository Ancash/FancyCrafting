package de.ancash.fancycrafting.gui.handler.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.gui.handler.IRecipeMatchHandler;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IRandomRecipe;

public class DefaultRecipeMatchHandler implements IRecipeMatchHandler {

	private final AbstractCraftingWorkspace workspace;
	private final FancyCrafting pl;

	public DefaultRecipeMatchHandler(FancyCrafting pl, AbstractCraftingWorkspace workspace) {
		this.workspace = workspace;
		this.pl = pl;
	}

	@Override
	public void onRecipeMatch() {
		if (Bukkit.isPrimaryThread())
			onRecipeMatch0();
		else
			new BukkitRunnable() {

				@Override
				public void run() {
					onRecipeMatch0();
				}
			}.runTask(pl);
	}

	private void onRecipeMatch0() {
		synchronized (workspace.getLock()) {
			if (workspace.getCurrentRecipe() == null)
				return;
			workspace.setItem(workspace.getCurrentRecipe().getResult(),
					workspace.getTemplate().getSlots().getResultSlot());
			for (int i : workspace.getTemplate().getSlots().getCraftStateSlots())
				workspace.setItem(pl.getWorkspaceObjects().getValidItem().getOriginal(), i);
			workspace.getPlayer().updateInventory();
		}
	}

	@Override
	public void onNoRecipeMatch() {
		if (Bukkit.isPrimaryThread())
			onNoRecipeMatch0();
		else
			new BukkitRunnable() {

				@Override
				public void run() {
					onNoRecipeMatch0();
				}
			}.runTask(pl);
	}

	private void onNoRecipeMatch0() {
		synchronized (workspace.getLock()) {
			workspace.setItem(pl.getWorkspaceObjects().getInvalidItem().getOriginal(),
					workspace.getTemplate().getSlots().getResultSlot());
			for (int i : workspace.getTemplate().getSlots().getCraftStateSlots())
				workspace.setItem(pl.getWorkspaceObjects().getInvalidItem().getOriginal(), i);
			workspace.getPlayer().updateInventory();
		}
	}

	@Override
	public ItemStack getSingleRecipeCraft(IRecipe recipe, Player player) {
		if (recipe instanceof IRandomRecipe) {
			return ((IRandomRecipe) recipe).getRandom().getOriginal();
		} else if (recipe instanceof IShapedRecipe) {
			return ((IShapedRecipe) recipe).getResult();
		} else if (recipe instanceof IShapelessRecipe) {
			return ((IShapelessRecipe) recipe).getResult();
		}
		throw new IllegalArgumentException("Unknown recipe impl: " + recipe.getClass()); //$NON-NLS-1$
	}
}