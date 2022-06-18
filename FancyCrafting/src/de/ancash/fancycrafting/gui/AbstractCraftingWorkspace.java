package de.ancash.fancycrafting.gui;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.AutoRecipeMatcher;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.inventory.IGUI;

public abstract class AbstractCraftingWorkspace extends IGUI {

	protected final FancyCrafting pl;
	protected final WorkspaceTemplate template;
	protected IMatrix<IItemStack> matrix = new IMatrix<>(new IItemStack[0], 0, 0);
	protected Set<IRecipe> recipes;
	protected IRecipe currentRecipe;
	protected boolean includeVanillaRecipes;
	protected final Player player;
	protected final AutoRecipeMatcher matcher;
	protected final Object lock = new Object();
	protected final VanillaRecipeMatcher vanillaMatcher;

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template) {
		this(pl, player, template, true);
	}

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes) {
		this(pl, player, template, includeVanillaRecipes, pl.getRecipeManager().getCustomRecipes(),
				new AutoRecipeMatcher(player,
						includeVanillaRecipes ? pl.getRecipeManager().getAutoMatchingRecipes()
								: pl.getRecipeManager().getCustomRecipes()));
	}

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes, Set<IRecipe> recipes, AutoRecipeMatcher matcher) {
		super(player.getUniqueId(), template.getDimension().getSize(), template.getTitle());
		this.matcher = matcher;
		this.pl = pl;
		this.template = template;
		this.recipes = recipes;
		this.includeVanillaRecipes = includeVanillaRecipes;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
	}

	public void setIncludeVanillaRecipes(boolean b) {
		this.includeVanillaRecipes = b;
	}

	public void setRecipes(Set<IRecipe> recipes) {
		this.recipes = recipes;
	}

	public void updateMatrix() {
		synchronized (lock) {
			matrix = new IMatrix<>(getIngredients(), template.getDimension().getWidth(),
					template.getDimension().getHeight());
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

	public void autoMatch() {
		matcher.compute();
		onAutoMatchFinish();
	}

	public void autoMatchAsync() {
		pl.submit(new Runnable() {

			@Override
			public void run() {
				matcher.compute();
				onAutoMatchFinish();
			}
		});
	}

	public boolean hasMatchingRecipe() {
		return currentRecipe != null;
	}

	public abstract IItemStack[] getIngredients();

	public abstract void onRecipeMatch();

	public abstract void onNoRecipeMatch();

	public abstract void onNoPermission(IRecipe recipe, Player p);

	public abstract void onAutoMatchFinish();

	class RecipeMatcherCallable implements Callable<IRecipe> {

		@Override
		public IRecipe call() {
			synchronized (lock) {
				currentRecipe = match();
				if (currentRecipe == null)
					onNoRecipeMatch();
				else
					onRecipeMatch();
			}
			return currentRecipe;
		}

		private IRecipe match() {
			IRecipe match = null;
			if (matrix.getArray().length == 0)
				return match;

			Set<IRecipe> customRecipes;

			if (recipes != null && !(customRecipes = recipes).isEmpty())
				if ((match = pl.getRecipeManager().matchRecipe(matrix, customRecipes)) != null)
					if (FancyCrafting.canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						onNoPermission(match, player);

			if (includeVanillaRecipes)
				if ((match = vanillaMatcher.matchVanillaRecipe(matrix)) != null)
					if (FancyCrafting.canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						onNoPermission(match, player);

			onNoRecipeMatch();
			return currentRecipe = null;
		}
	}
}