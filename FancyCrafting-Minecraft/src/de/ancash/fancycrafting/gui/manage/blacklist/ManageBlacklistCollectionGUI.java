package de.ancash.fancycrafting.gui.manage.blacklist;

import java.util.List;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractRecipeCollectionPagedViewGUI;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.inventory.InventoryItem;

public class ManageBlacklistCollectionGUI extends AbstractRecipeCollectionPagedViewGUI {

	private final FancyCrafting pl;

	public ManageBlacklistCollectionGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(pl, player, recipes, pl.getManageBlacklistTitle());
		this.pl = pl;
	}

	@Override
	public void onRecipeClick(IRecipe recipe) {
		new ManageBlacklistedRecipeGUI(pl, player, recipe).open();
	}

	@Override
	public void open() {
		super.open();
		addInventoryItem(new InventoryItem(this, super.pl.getAddRecipeToBlacklistItem().getOriginal(),
				49, (a, b, c, top) -> {
					if(top)
						CreateBlacklistedRecipeGUI.open(super.pl, player);
				}));
	}
}