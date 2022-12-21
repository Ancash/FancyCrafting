package de.ancash.fancycrafting.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.minecraft.IItemStack;

public class AutoRecipeMatcher {

	private final Player player;
	private Set<IRecipe> matchedRecipes = new HashSet<>();
	private final Set<IRecipe> possibleRecipes;
	private List<IItemStack> ingredients;

	public AutoRecipeMatcher(Player player, Set<IRecipe> recipes) {
		this.player = player;
		this.possibleRecipes = recipes.stream().filter(r -> AbstractFancyCrafting.canCraftRecipe(r, player))
				.collect(Collectors.toSet());
	}

	public Set<IRecipe> getMatchedRecipes() {
		return matchedRecipes;
	}

	public void compute() {
		ingredients = getInventoryContentsAsIItemStack();
		Map<Integer, Integer> mappedIngredientHashCodes = mapHashCodes(ingredients);
		matchedRecipes.clear();
		for (IRecipe possible : possibleRecipes) {
			if (match(possible, mappedIngredientHashCodes))
				matchedRecipes.add(possible);
		}
	}

	public boolean match(IRecipe recipe, Map<Integer, Integer> map) {
		if (!map.keySet().containsAll(recipe.getIngredientsHashCodes().keySet()))
			return false;
		for (Entry<Integer, Integer> entry : recipe.getIngredientsHashCodes().entrySet())
			if (entry.getValue() > map.get(entry.getKey()))
				return false;
		return true;
	}

	private List<IItemStack> getInventoryContentsAsIItemStack() {
		return Stream.of(player.getInventory().getContents()).filter(i -> i != null).map(IItemStack::new)
				.collect(Collectors.toList());
	}

	private Map<Integer, Integer> mapHashCodes(List<IItemStack> from) {
		return from.stream()
				.collect(Collectors.toMap(e -> e.hashCode(), e -> e.getOriginal().getAmount(), (s, a) -> s + a));
	}
}