package de.ancash.fancycrafting.gui.handler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractCraftingWorkspace;

public class RecipeMatchHandler extends RecipeResultSupplier {

	private final AbstractCraftingWorkspace workspace;
	private final FancyCrafting pl;

	public RecipeMatchHandler(FancyCrafting pl, AbstractCraftingWorkspace workspace) {
		this.workspace = workspace;
		this.pl = pl;
	}

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
				workspace.setItem(pl.getWorkspaceObjects().getValidItem(), i);
			workspace.getPlayer().updateInventory();
		}
	}

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
			workspace.setItem(pl.getWorkspaceObjects().getInvalidItem(),
					workspace.getTemplate().getSlots().getResultSlot());
			for (int i : workspace.getTemplate().getSlots().getCraftStateSlots())
				workspace.setItem(pl.getWorkspaceObjects().getInvalidItem(), i);
			workspace.getPlayer().updateInventory();
		}
	}
}