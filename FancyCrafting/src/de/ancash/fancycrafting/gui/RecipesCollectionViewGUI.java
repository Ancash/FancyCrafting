package de.ancash.fancycrafting.gui;

import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.inventory.InventoryItem;

public class RecipesCollectionViewGUI {

	protected final List<IRecipe> recipes;
	protected int pos;
	protected final RecipeViewGUI gui;
	protected final FancyCrafting pl;
	protected final Player player;

	public RecipesCollectionViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		this.recipes = recipes;
		this.player = player;
		this.pl = pl;
		this.gui = new RecipeViewGUI(pl, player, recipes.get(pos));
		openRecipe(pos);
	}

	public void openRecipe(int i) {
		IRecipe recipe = recipes.get(i);
		WorkspaceTemplate template = WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight());
		gui.openRecipe(recipe);
		gui.addInventoryItem(new InventoryItem(gui, pl.getWorkspaceObjects().getNextItem().getOriginal(),
				template.getSlots().getNextSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> openRecipe(increment()))));
		gui.addInventoryItem(new InventoryItem(gui, pl.getWorkspaceObjects().getPrevItem().getOriginal(),
				template.getSlots().getPrevSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> openRecipe(decrement()))));

		if (player.hasPermission(FancyCrafting.EDIT_PERM) && !recipes.get(i).isVanilla())
			gui.addInventoryItem(new InventoryItem(gui, pl.getWorkspaceObjects().getEditItem().getOriginal(),
					template.getSlots().getEditSlot(), (a, b, c, top) -> Optional.ofNullable(top ? this : null)
							.ifPresent(self -> new EditNormalRecipeGUI(pl, player, recipes.get(i)).open())));
	}

	protected int decrement() {
		return pos = prev();
	}

	protected int increment() {
		return pos = next();
	}

	private int next() {
		return pos + 1 >= recipes.size() ? 0 : pos + 1;
	}

	private int prev() {
		return pos - 1 < 0 ? recipes.size() - 1 : pos - 1;
	}
}