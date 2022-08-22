package de.ancash.fancycrafting;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.IDefaultRecipeMatcherCallable;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.IItemStack;

public class DefaultRecipeMatcherCallable implements IDefaultRecipeMatcherCallable{

	private IMatrix<IItemStack> matrix;
	private final FancyCrafting pl;
	private final Player player;
	private final VanillaRecipeMatcher vanillaMatcher;
	
	public DefaultRecipeMatcherCallable(FancyCrafting pl, Player player) {
		this.pl = pl;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
	}
	
	@Override
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
		if (pl.getRecipeManager().isBlacklisted(Stream.of(matrix.getArray())
				.map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList())))
			return null;
		IRecipe match = pl.getRecipeManager().matchRecipe(matrix);

		if (match != null)
			if (AbstractFancyCrafting.canCraftRecipe(match, player))
				return match;
		match = vanillaMatcher.matchVanillaRecipe(matrix);
		if (match != null)
			if (AbstractFancyCrafting.canCraftRecipe(match, player))
				return match;
		return null;
	}

	@Override
	public IMatrix<IItemStack> getMatrix() {
		return matrix;
	}

}