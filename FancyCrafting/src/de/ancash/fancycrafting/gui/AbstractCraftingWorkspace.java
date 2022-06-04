package de.ancash.fancycrafting.gui;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.inventory.IGUI;

public abstract class AbstractCraftingWorkspace extends IGUI {

	protected final FancyCrafting pl;
	protected final CraftingTemplate template;
	protected IMatrix<ItemStack> matrix = new IMatrix<>(new ItemStack[0], 0, 0);
	protected Supplier<Set<IRecipe>> recipes = null;
	protected IRecipe currentRecipe;
	protected boolean includeVanillaRecipes;
	protected final Player player;
	protected final Object lock = new Object();
	
	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, CraftingTemplate template) {
		this(pl, player, template, true, () -> pl.getRecipeManager().getCustomRecipes());
	}
	
	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, CraftingTemplate template, boolean includeVanillaRecipes) {
		this(pl, player, template, includeVanillaRecipes, () -> pl.getRecipeManager().getCustomRecipes());
	}
	
	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, CraftingTemplate template, boolean includeVanillaRecipes, Supplier<Set<IRecipe>> recipes) {
		super(player.getUniqueId(), template.getSize(), template.getTitle());
		this.pl = pl;
		this.template = template;
		this.recipes = recipes;
		this.includeVanillaRecipes = includeVanillaRecipes;
		this.player = player;
	}
	
	public void setIncludeVanillaRecipes(boolean b) {
		this.includeVanillaRecipes = b;
	}
	
	public void setRecipes(Supplier<Set<IRecipe>> recipes) {
		this.recipes = recipes;
	}
	
	public void updateMatrix() {
		synchronized (lock) {
			matrix = new IMatrix<>(getIngredients(), template.getWidth(), template.getHeight());
			matrix.optimize();
		}
	}
		
	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}
	
	public IRecipe matchRecipe() {
		return new RecipeMatcherCallable().call();
	}
	
	public Future<IRecipe> matchRecipeAsync() {
		return pl.submit(new RecipeMatcherCallable());
	}
	
	public boolean hasMatchingRecipe() {
		return currentRecipe != null;
	}
	
	public abstract boolean canCraftRecipe(IRecipe recipe, Player p);
	
	public abstract ItemStack[] getIngredients();
	
	public abstract void onRecipeMatch();
	
	public abstract void onNoRecipeMatch();

	public abstract void onNoPermission(IRecipe recipe, Player p);
	
	class RecipeMatcherCallable implements Callable<IRecipe> {
		
		@Override
		public IRecipe call() {
			synchronized (lock) {
				currentRecipe = match();
				if(currentRecipe == null)
					onNoRecipeMatch();
				else
					onRecipeMatch();
			}
			return currentRecipe;
		}
		
		private IRecipe match() {
			IRecipe match = null;
			if(matrix.getArray().length == 0)
				return match;
			
			Set<IRecipe> customRecipes;
			
			if(recipes != null && !(customRecipes = recipes.get()).isEmpty()) 
				if((match = pl.getRecipeManager().matchRecipe(matrix, customRecipes)) != null)
					if(canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						onNoPermission(match, player);
			
			if(includeVanillaRecipes)
				if((match = FancyCrafting.getVanillaRecipeMatcher(player).matchVanillaRecipe(matrix)) != null) 
					if(canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						onNoPermission(match, player);
			
			onNoRecipeMatch();
			return currentRecipe = null;
		}
	}
}