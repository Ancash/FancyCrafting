package de.ancash.fancycrafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.ilibrary.datastructures.maps.CompactMap;

import static de.ancash.fancycrafting.utils.IShapedRecipe.*;

public class RecipeManager {
	
	
	@SuppressWarnings("unused")
	private final FancyCrafting plugin;
	private final List<IRecipe> recipes = new ArrayList<IRecipe>();
	private final CompactMap<Integer, List<IRecipe>> recipesSortedBySize = new CompactMap<Integer, List<IRecipe>>();
	private final CompactMap<String, List<IRecipe>> recipesSortedByResult = new CompactMap<>();
	
	public RecipeManager(FancyCrafting plugin) {
		this.plugin = plugin;
		for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
		Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
		Recipe recipe = recipeIterator.next();
		while (recipeIterator.hasNext() && recipe != null){
			IRecipe irecipe = IRecipe.toIRecipe(recipe);
			if(irecipe != null) {
				recipes.add(irecipe);
				recipesSortedBySize.get(irecipe.getIngredientsSize()).add(irecipe);
				String byResult = irecipe.toString().split("\\{")[1].split(" x")[0];
				if(!recipesSortedByResult.containsKey(byResult))
					recipesSortedByResult.put(byResult, new ArrayList<>());
				recipesSortedByResult.get(byResult).add(irecipe);
			}
			if(!recipeIterator.hasNext()) {
				recipe = null;
			} else {
				recipe = recipeIterator.next();
			}
		}
		Collections.shuffle(recipes);
	}	
	
	public IRecipe match(CompactMap<Integer, ItemStack> ingredientsMap, List<IRecipe> possibleRecipes) {
		optimize(ingredientsMap);
		if(ingredientsMap.isEmpty()) return null;

		if(possibleRecipes == null) return null;
		if(possibleRecipes.size() == 0) return null;
		if(possibleRecipes.size() == 1) return possibleRecipes.iterator().next();
		for(IRecipe recipe : possibleRecipes) {
			if(recipe instanceof IShapedRecipe) {
				if(IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) recipe).getIngredientsMap(),
						possibleRecipes.size() == 1)) {
					return recipe;
				}
			}
			if(recipe instanceof IShapelessRecipe) {
				if(IRecipe.matchesShapeless(((IShapelessRecipe) recipe).getIngredientsList(), ingredientsMap.values(),
						possibleRecipes.size() == 1 )) {
					return recipe;
				}
			}
		}
		return null;
	}
	
	public IRecipe match(CompactMap<Integer, ItemStack> ingredientsMap) {
		optimize(ingredientsMap);
		if(ingredientsMap.isEmpty()) return null;

		return match(ingredientsMap, getRecipes(ingredientsMap));
	}
	
	public List<IRecipe> getRecipes(CompactMap<Integer, ItemStack> ingredienstMap) {
		List<IRecipe> recipes = new ArrayList<>();
		
		for(IRecipe recipe : recipesSortedBySize.get(ingredienstMap.size())) {
			if(recipe instanceof IShapedRecipe) {
				IShapedRecipe shaped = (IShapedRecipe) recipe;
				if(shaped.getIngredientsMap().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShaped(ingredienstMap, shaped.getIngredientsMap(), true)) {
					recipes.add(recipe);
				}
			}
			if(recipe instanceof IShapelessRecipe) {
				IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
				if(shapeless.getIngredientsList().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShapeless(shapeless.getIngredientsList(), ingredienstMap.values(), true)) {
					recipes.add(recipe);
				}
			}
		}
		return recipes;
	}
	
	public List<IRecipe> getByResult(ItemStack result) {
		return recipesSortedByResult.get(result.toString().split("\\{")[1].split(" x")[0]);
	}
}
