package de.ancash.fancycrafting.recipe.crafting;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.serde.SerializedItem;

public class RecipeMatcherCallable implements Callable<IRecipe> {

	private IMatrix<SerializedItem> matrix;
	private final FancyCrafting pl;
	private final Player player;
	private final VanillaRecipeMatcher vanillaMatcher;

	public RecipeMatcherCallable(FancyCrafting pl, Player player) {
		this.pl = pl;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
	}

	public void setMatrix(IMatrix<SerializedItem> matrix) {
		this.matrix = matrix;
	}

	@Override
	public synchronized IRecipe call() {
		return match();
	}

	protected IRecipe match() {
		if (matrix.getArray().length == 0)
			return null;
		if (pl.getRecipeManager().isBlacklisted(
				Stream.of(matrix.getArray()).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList())))
			return null;
		IRecipe match = pl.getRecipeManager().matchRecipe(matrix);

		if (match != null)
			if (FancyCrafting.canCraftRecipe(match, player))
				return match;

		if (FancyCrafting.vanillaRecipesAcceptPlainItemsOnly() && doIngredientsHaveMeta(matrix))
			return null;
		match = vanillaMatcher.matchVanillaRecipe(matrix);
		if (match != null)
			if (FancyCrafting.canCraftRecipe(match, player))
				return match;
		return null;
	}

	public static boolean doIngredientsHaveMeta(IMatrix<SerializedItem> matrix) {
		return Stream.of(matrix.getArray()).filter(i -> i != null).map(SerializedItem::toItem)
				.filter(ItemStack::hasItemMeta).filter(item -> {
					ItemMeta meta = item.getItemMeta();
					if (meta.hasLocalizedName() || meta.hasDisplayName() || meta.hasLore()
							|| !meta.getItemFlags().isEmpty() || meta.hasCustomModelData())
						return true;
					SerializedItem si = SerializedItem.of(item);
					if (si.getMap().keySet().stream().filter(s -> s.contains(NBTNexus.SPLITTER)).findAny().isPresent())
						return true;
					return false;
				}).findAny().isPresent();
	}

	public IMatrix<SerializedItem> getMatrix() {
		return matrix;
	}
}