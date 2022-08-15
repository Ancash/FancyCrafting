package de.ancash.fancycrafting.base;

import java.util.concurrent.Callable;

import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.IItemStack;

public interface IDefaultRecipeMatcherCallable extends Callable<IRecipe>{

	@Override
	public IRecipe call();
	
	public void setMatrix(IMatrix<IItemStack> matrix);
	
	public IMatrix<IItemStack> getMatrix();
}