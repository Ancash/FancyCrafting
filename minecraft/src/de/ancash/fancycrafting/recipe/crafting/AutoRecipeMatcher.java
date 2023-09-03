package de.ancash.fancycrafting.recipe.crafting;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public class AutoRecipeMatcher {

	private final Player player;
	private Set<IRecipe> matchedRecipes = new HashSet<>();
	private final Set<IRecipe> possibleRecipes;
	private List<SerializedItem> ingredients;
	private final FancyCrafting pl;

	public AutoRecipeMatcher(FancyCrafting pl, Player player, Set<IRecipe> recipes) {
		this.player = player;
		this.possibleRecipes = recipes.stream().filter(r -> FancyCrafting.canCraftRecipe(r, player))
				.collect(Collectors.toSet());
		this.pl = pl;
	}

	public Set<IRecipe> getMatchedRecipes() {
		return matchedRecipes;
	}

	public void compute() {
		ingredients = getInventoryContents();
		Map<Integer, Integer> mappedIngredientHashCodes = mapHashCodes(ingredients);
		matchedRecipes.clear();
		for (IRecipe possible : possibleRecipes) {
			if (match(possible, mappedIngredientHashCodes))
				matchedRecipes.add(possible);
		}
	}

	public boolean match(IRecipe recipe, Map<Integer, Integer> map) {
		return !match0(recipe, map) ? false : !pl.getRecipeManager().isBlacklisted(recipe.getHashMatrix());
	}

	protected boolean match0(IRecipe recipe, Map<Integer, Integer> map) {
		if (!map.keySet().containsAll(recipe.getIngredientsHashCodes().keySet()))
			return false;
		for (Entry<Integer, Integer> entry : recipe.getIngredientsHashCodes().entrySet())
			if (entry.getValue() > map.get(entry.getKey()))
				return false;
		return true;
	}

	private List<SerializedItem> getInventoryContents() {
		return Stream.of(player.getInventory().getContents()).filter(i -> i != null).map(SerializedItem::of)
				.collect(Collectors.toList());
	}

	private Map<Integer, Integer> mapHashCodes(List<SerializedItem> from) {
		return from.stream().collect(Collectors.toMap(e -> e.hashCode(),
				e -> SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(e.getMap()), (s, a) -> s + a));
	}
}