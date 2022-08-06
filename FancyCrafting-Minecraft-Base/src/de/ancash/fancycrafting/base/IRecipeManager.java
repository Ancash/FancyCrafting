package de.ancash.fancycrafting.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.misc.MathsUtils;

public abstract class IRecipeManager {

	protected final Map<List<Integer>, IRecipe> cachedRecipes = new ConcurrentHashMap<>();
	protected final Set<IRecipe> customRecipes = new HashSet<>();
	protected final Set<IRecipe> autoMatchingRecipes = new HashSet<>();
	protected final Map<String, Set<IRecipe>> recipesByName = new ConcurrentHashMap<>();
	protected final Map<Integer, Set<IRecipe>> recipesByHash = new ConcurrentHashMap<>();
	protected final AbstractFancyCrafting plugin;

	public IRecipeManager(AbstractFancyCrafting pl) {
		this.plugin = pl;
	}

	public void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> {
			IRecipe recipe = IRecipe.fromVanillaRecipe(plugin, r);
			if (recipe == null)
				return;
			registerRecipe(recipe);
		});
	}

	public void cacheRecipe(IRecipe recipe) {
		if (recipe.isVanilla())
			return;
		if (recipe instanceof IShapedRecipe)
			cachedRecipes.put(Stream.of(((IShapedRecipe) recipe).getIngredientsArray())
					.map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()), recipe);
		else
			cachedRecipes.put(recipe.getIIngredients().stream().filter(i -> i != null).map(IItemStack::hashCode)
					.sorted().collect(Collectors.toList()), recipe);
	}

	public Set<IRecipe> getRecipeByHash(ItemStack itemStack) {
		return getRecipeByHash(new IItemStack(itemStack));
	}

	public Set<IRecipe> getRecipeByHash(IItemStack iItemStack) {
		if (recipesByHash.get(iItemStack.hashCode()) == null)
			return null;
		return Collections.unmodifiableSet(recipesByHash.get(iItemStack.hashCode()));
	}

	public boolean registerRecipe(IRecipe recipe) {
		if (recipe == null)
			return false;
		if (recipe.getResult() == null || recipe.getResult().getType().equals(Material.AIR)) {
			plugin.getLogger().fine("Invalid recipe '" + recipe); //$NON-NLS-1$
			return false;
		}
		int hash = new IItemStack(recipe.getResult()).hashCode();
		String name = recipe.getRecipeName().replace(" ", "-"); //$NON-NLS-1$//$NON-NLS-2$

		recipesByName.computeIfAbsent(name, k -> new HashSet<>());
		recipesByName.get(name).add(recipe);
		recipesByHash.computeIfAbsent(hash, k -> new HashSet<>());
		recipesByHash.get(hash).add(recipe);

		if (recipe.isSuitableForAutoMatching())
			autoMatchingRecipes.add(recipe);
		else if (recipe.isVanilla())
			plugin.getLogger()
					.fine(String.format(
							"'%s' is not included in auto recipe matching (no unique ingredient identification)", //$NON-NLS-1$
							recipe.getRecipeName()));
		if (!recipe.isVanilla()) {
			customRecipes.add(recipe);
			cacheRecipe(recipe);
		}
		return true;
	}

	public IRecipe matchRecipe(IMatrix<IItemStack> matrix) {
		return cachedRecipes.get(
				Stream.of(matrix.getArray()).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()));
	}

	public void clear() {
		customRecipes.clear();
		autoMatchingRecipes.clear();
		recipesByName.clear();
		recipesByHash.clear();
	}

	public void reloadRecipes() {
		new BukkitRunnable() {

			@Override
			public void run() {
				plugin.getLogger().info("Reloading Recipes..."); //$NON-NLS-1$
				long now = System.nanoTime();
				customRecipes.clear();
				recipesByName.clear();
				recipesByHash.clear();
				autoMatchingRecipes.clear();
				loadBukkitRecipes();
				loadCustomRecipes();
				plugin.getLogger()
						.info("Reloaded! " + (MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3)) + "s"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}.runTaskAsynchronously(plugin);
	}

	public Set<IRecipe> getAutoMatchingRecipes() {
		return Collections.unmodifiableSet(autoMatchingRecipes);
	}

	public Set<IRecipe> getCustomRecipes() {
		return Collections.unmodifiableSet(customRecipes);
	}

	public Set<IRecipe> getRecipeByName(String name) {
		if (recipesByName.get(name) == null)
			return null;
		return Collections.unmodifiableSet(recipesByName.get(name));
	}

	public abstract void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid,
			int width, int height) throws InvalidRecipeException;

	public abstract void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid,
			int width, int height) throws InvalidRecipeException;

	public abstract void saveRandomRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name,
			UUID uuid, int width, int height, Map<ItemStack, Integer> rngMap) throws InvalidRecipeException;

	public abstract void loadCustomRecipes();

	public abstract boolean isVanillaRecipeIncluded(IRecipe vanilla);

	public abstract void delete(String recipeName) throws RecipeDeleteException;
}