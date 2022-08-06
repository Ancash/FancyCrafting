package de.ancash.fancycrafting.gui.base;

import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.inventory.InventoryItem;

public abstract class AbstractRecipeCollectionViewGUI {

	protected final List<IRecipe> recipes;
	protected IRecipe cur;
	protected int pos;
	protected final AbstractFancyCrafting pl;
	protected final Player player;

	public AbstractRecipeCollectionViewGUI(AbstractFancyCrafting pl, Player player, List<IRecipe> recipes) {
		this.recipes = recipes;
		this.player = player;
		this.pl = pl;
		openRecipe(pos);
	}

	public void openRecipe(int i) {
		cur = recipes.get(i);
		AbstractRecipeViewGUI gui = viewRecipe(cur);
		gui.onOpen(this::addItems);
		gui.open();
	}
	
	public abstract AbstractRecipeViewGUI viewRecipe(IRecipe recipe);
	
	protected void addItems(AbstractRecipeViewGUI gui) {
		gui.addInventoryItem(new InventoryItem(gui, pl.getWorkspaceObjects().getNextItem().getOriginal(),
				pl.getViewSlots().getNextSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> openRecipe(increment()))));
		gui.addInventoryItem(new InventoryItem(gui, pl.getWorkspaceObjects().getPrevItem().getOriginal(),
				pl.getViewSlots().getPrevSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> openRecipe(decrement()))));							
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