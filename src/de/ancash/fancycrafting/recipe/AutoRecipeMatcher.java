package de.ancash.fancycrafting.recipe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.minecraft.IItemStack;

public class AutoRecipeMatcher {

	private final Player player;
	private Set<IRecipe> matchedRecipes = new HashSet<>();
	private final Set<IRecipe> possibleRecipes;
	private Map<Integer, IItemStack> ingredients;

	public AutoRecipeMatcher(Player player, Set<IRecipe> recipes) {
		this.player = player;
		this.possibleRecipes = recipes;
	}

	public Set<IRecipe> getMatchedRecipes() {
		return matchedRecipes;
	}

	public void compute() {
		ingredients = getInventoryContentsAsIItemStack();
		Map<Integer, Integer> mappedIngredientHashCodes = mapHashCodes(ingredients);
		matchedRecipes = Collections.unmodifiableSet(possibleRecipes.stream().filter(r -> r.isSuitableForAutoMatching()
				&& FancyCrafting.canCraftRecipe(r, player) && match(r, mappedIngredientHashCodes))
				.collect(Collectors.toSet()));
	}

	private boolean match(IRecipe recipe, Map<Integer, Integer> map) {
		for (Entry<Integer, Integer> entry : recipe.getIngredientsHashCodes().entrySet())
			if (!map.containsKey(entry.getKey()) || map.get(entry.getKey()) < entry.getValue())
				return false;
		return true;
	}

	private Map<Integer, IItemStack> getInventoryContentsAsIItemStack() {
		return IntStream.range(0, player.getInventory().getSize())
				.filter(i -> player.getInventory().getContents()[i] != null).boxed()
				.collect(Collectors.toMap(i -> i, i -> new IItemStack(player.getInventory().getContents()[i])));
	}

	private Map<Integer, Integer> mapHashCodes(Map<Integer, IItemStack> from) {
		return from.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().hashCode(),
				e -> e.getValue().getOriginal().getAmount(), (s, a) -> s + a));
	}
}