package de.ancash.fancycrafting.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemBuilder;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
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
		gui.addInventoryItem(
				new InventoryItem(gui, pl.getNextItem(), template.getSlots().getNextSlot(), new Clickable() {

					@Override
					public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
						if (topInventory) {
							openRecipe(increment());
						}
					}
				}));
		gui.addInventoryItem(
				new InventoryItem(gui, pl.getPrevItem(), template.getSlots().getPrevSlot(), new Clickable() {

					@Override
					public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
						if (topInventory) {
							openRecipe(decrement());
						}
					}
				}));

		if (player.hasPermission("fancycrafting.admin.edit") && !recipes.get(i).isVanilla())
			gui.addInventoryItem(new InventoryItem(gui,
					new ItemBuilder(XMaterial.WRITABLE_BOOK).setDisplayname("§aClick to edit recipe").build(),
					template.getSlots().getEditSlot(), new Clickable() {

						@Override
						public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
							if (topInventory) {
								player.closeInventory();
								new RecipeEditGUI(pl, player, recipes.get(i));
							}
						}
					}));
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