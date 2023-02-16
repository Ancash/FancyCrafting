package de.ancash.fancycrafting.recipe;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.minecraft.IItemStack;

public class RecipeMatcherCallable implements Callable<IRecipe> {

	private IMatrix<IItemStack> matrix;
	private final FancyCrafting pl;
	private final Player player;
	private final VanillaRecipeMatcher vanillaMatcher;

	public RecipeMatcherCallable(FancyCrafting pl, Player player) {
		this.pl = pl;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
	}

	public void setMatrix(IMatrix<IItemStack> matrix) {
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
		match = vanillaMatcher.matchVanillaRecipe(matrix);
		if (match != null)
			if (FancyCrafting.canCraftRecipe(match, player))
				return match;
		return null;
	}

	public IMatrix<IItemStack> getMatrix() {
		return matrix;
	}
}