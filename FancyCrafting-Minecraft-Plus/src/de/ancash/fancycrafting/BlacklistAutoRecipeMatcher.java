package de.ancash.fancycrafting;

import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.recipe.AutoRecipeMatcher;
import de.ancash.fancycrafting.recipe.IRecipe;

public class BlacklistAutoRecipeMatcher extends AutoRecipeMatcher{

	private final FancyCrafting pl;
	
	public BlacklistAutoRecipeMatcher(FancyCrafting pl, Player player, Set<IRecipe> recipes) {
		super(player, recipes);
		this.pl = pl;
	}

	@Override
	public boolean match(IRecipe recipe, Map<Integer, Integer> map) {
		return !super.match(recipe, map) ? false : !pl.getRecipeManager().isBlacklisted(recipe.getHashMatrix());
	}
}