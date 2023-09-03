package de.ancash.fancycrafting.autocrafter;

import java.util.List;

import de.ancash.fancycrafting.recipe.IRecipe;

public interface IRecipeComputer {

	public List<IRecipe> computeRecipes();

	public boolean isAsync();
}
