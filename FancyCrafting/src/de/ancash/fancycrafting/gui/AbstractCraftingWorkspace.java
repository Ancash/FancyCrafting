package de.ancash.fancycrafting.gui;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.handler.IAutoRecipeMatcherHandler;
import de.ancash.fancycrafting.gui.handler.IRecipeMatchHandler;
import de.ancash.fancycrafting.gui.handler.IRecipePermissionHandler;
import de.ancash.fancycrafting.gui.handler.impl.DefaultAutoRecipeMatcherHandler;
import de.ancash.fancycrafting.gui.handler.impl.DefaultRecipeMatchHandler;
import de.ancash.fancycrafting.gui.handler.impl.DefaultRecipePermissionHandler;
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
	protected final Object lock = new Object();

	protected final AutoRecipeMatcher matcher;
	protected final VanillaRecipeMatcher vanillaMatcher;
	private IRecipeMatchHandler matchHandler;
	private IRecipePermissionHandler permissionHandler;
	private IAutoRecipeMatcherHandler autoRecipeHandler;

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template) {
		this(pl, player, template, true);
	}

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes) {
		this(pl, player, template, includeVanillaRecipes, pl.getRecipeManager().getCustomRecipes(),
				new AutoRecipeMatcher(player, includeVanillaRecipes ? pl.getRecipeManager().getAutoMatchingRecipes()
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
		setRecipeMatchCompletionHandler(new DefaultRecipeMatchHandler(pl, this));
		setPermissionHandler(new DefaultRecipePermissionHandler(this));
		setAutoRecipeMatcherHandler(new DefaultAutoRecipeMatcherHandler(pl, this, matcher));
	}

	public void setAmount(int original, int subtract, int slot, Inventory inventory) {
		ItemStack is = inventory.getItem(slot);
		if (original - subtract <= 0)
			inventory.setItem(slot, null);
		else
			is.setAmount(original - subtract);
	}

	public boolean enableQuickCrafting() {
		return getTemplate().getSlots().enableQuickCrafting();
	}
	
	public Player getPlayer() {
		return player;
	}

	public WorkspaceTemplate getTemplate() {
		return template;
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

	public boolean isCraftingSlot(int s) {
		for (int i = 0; i < template.getSlots().getCraftingSlots().length; i++)
			if (template.getSlots().getCraftingSlots()[i] == s)
				return true;
		return false;
	}

	public ItemStack[] getPlayerInventoryContents(PlayerInventory inv) {
		try {
			return player.getInventory().getStorageContents();
		} catch (NoSuchMethodError e) {
			return player.getInventory().getContents();
		}
	}

	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	public Object getLock() {
		return lock;
	}

	public void updateAll() {
		updateRecipe();
		updateQuickCrafting();
	}

	public void updateRecipe() {
		updateMatrix();
		if (pl.checkRecipesAsync())
			matchRecipeAsync();
		else
			matchRecipe();
	}

	public void checkDelayed() {
		Bukkit.getScheduler().runTaskLater(pl, () -> updateAll(), 1);
	}

	public IRecipe matchRecipe() {
		return new RecipeMatcherCallable().call();
	}

	public Future<IRecipe> matchRecipeAsync() {
		return pl.submit(new RecipeMatcherCallable());
	}

	public void updateQuickCrafting() {
		if(!enableQuickCrafting())
			return;
		if (pl.isQuickCraftingAsync())
			autoMatchAsync();
		else
			autoRecipeHandler.autoMatch();
	}

	public void autoMatchAsync() {
		pl.submit(() -> autoRecipeHandler.autoMatch());
	}

	public boolean hasMatchingRecipe() {
		return currentRecipe != null;
	}

	public IItemStack[] getIngredients() {
		IItemStack[] ings = new IItemStack[template.getSlots().getCraftingSlots().length];
		for (int i = 0; i < ings.length; i++) {
			ItemStack item = getItem(template.getSlots().getCraftingSlots()[i]);
			if (item != null && item.getType() != Material.AIR)
				ings[i] = new IItemStack(item);
		}
		return ings;
	}

	public IRecipeMatchHandler getRecipeMatchCompletionHandler() {
		return matchHandler;
	}

	public void setRecipeMatchCompletionHandler(IRecipeMatchHandler recipeMatchHandler) {
		this.matchHandler = recipeMatchHandler;
	}

	public IRecipePermissionHandler getPermissionHandler() {
		return permissionHandler;
	}

	public void setPermissionHandler(IRecipePermissionHandler permissionHandler) {
		this.permissionHandler = permissionHandler;
	}

	public IAutoRecipeMatcherHandler getAutoRecipeHandler() {
		return autoRecipeHandler;
	}

	public void setAutoRecipeMatcherHandler(IAutoRecipeMatcherHandler autoRecipeHandler) {
		this.autoRecipeHandler = autoRecipeHandler;
	}

	class RecipeMatcherCallable implements Callable<IRecipe> {

		@Override
		public IRecipe call() {
			synchronized (lock) {
				currentRecipe = match();
				if (currentRecipe == null)
					matchHandler.onNoRecipeMatch();
				else
					matchHandler.onRecipeMatch();
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
					if (permissionHandler.canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						permissionHandler.onNoPermission(match, player);

			if (includeVanillaRecipes)
				if ((match = vanillaMatcher.matchVanillaRecipe(matrix)) != null)
					if (permissionHandler.canCraftRecipe(match, player))
						return currentRecipe = match;
					else
						permissionHandler.onNoPermission(match, player);

			matchHandler.onNoRecipeMatch();
			return currentRecipe = null;
		}
	}
}