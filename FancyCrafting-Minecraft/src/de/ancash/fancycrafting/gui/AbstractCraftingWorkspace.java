package de.ancash.fancycrafting.gui;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.ancash.ILibrary;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.handler.AutoRecipeMatcherHandler;
import de.ancash.fancycrafting.gui.handler.RecipeMatchHandler;
import de.ancash.fancycrafting.gui.handler.RecipePermissionHandler;
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
	protected IRecipe currentRecipe;
	protected boolean includeVanillaRecipes;
	protected final Player player;
	protected final Object lock = new Object();
	protected int lastCraftTick = ILibrary.getTick();

	protected AutoRecipeMatcher matcher;
	protected final VanillaRecipeMatcher vanillaMatcher;
	private RecipeMatchHandler matchHandler;
	private RecipePermissionHandler permissionHandler;
	private AutoRecipeMatcherHandler autoRecipeHandler;
	private RecipeMatcherCallable recipeMatcherCallable = new RecipeMatcherCallable(this);

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template) {
		this(pl, player, template, true);
	}

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes) {
		this(pl, player, template, includeVanillaRecipes,
				new AutoRecipeMatcher(pl, player, pl.getRecipeManager().getAutoMatchingRecipes()));
	}

	public AbstractCraftingWorkspace(FancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes, AutoRecipeMatcher matcher) {
		super(player.getUniqueId(), template.getDimension().getSize(), template.getTitle());
		if (template.getSlots().enableQuickCrafting()
				&& (!FancyCrafting.permsForQuickCrafting() || player.hasPermission(FancyCrafting.QUICK_CRAFTING_PERM)))
			this.matcher = matcher;
		this.pl = pl;
		this.template = template;
		this.includeVanillaRecipes = includeVanillaRecipes;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
		this.recipeMatcherCallable = recipeMatcherCallable == null ? new RecipeMatcherCallable(this)
				: recipeMatcherCallable;
	}

	public void setAutoRecipeMatcher(AutoRecipeMatcher a) {
		this.matcher = a;
	}

	public void setRecipeMatcherCallable(RecipeMatcherCallable r) {
		this.recipeMatcherCallable = r;
	}

	public IMatrix<IItemStack> getMatrix() {
		return matrix;
	}

	public void setAmount(int original, int subtract, int slot) {
		ItemStack is = getItem(slot);
		if (original - subtract <= 0)
			setItem(null, slot);
		else
			is.setAmount(original - subtract);
	}

	public void setCurrentRecipe(IRecipe r) {
		this.currentRecipe = r;
	}

	public boolean enableQuickCrafting() {
		return matcher != null;
	}

	public int getLastCraftTick() {
		return lastCraftTick;
	}

	public void updateLastCraftTick() {
		this.lastCraftTick = ILibrary.getTick();
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
		return recipeMatcherCallable.call();
	}

	public Future<IRecipe> matchRecipeAsync() {
		return pl.submit(recipeMatcherCallable);
	}

	public void updateQuickCrafting() {
		if (!enableQuickCrafting())
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
				ings[i] = new IItemStack(item.clone());
		}
		return ings;
	}

	public RecipeMatchHandler getRecipeMatchCompletionHandler() {
		return matchHandler;
	}

	public void setRecipeMatchCompletionHandler(RecipeMatchHandler recipeMatchHandler) {
		this.matchHandler = recipeMatchHandler;
	}

	public RecipePermissionHandler getPermissionHandler() {
		return permissionHandler;
	}

	public void setPermissionHandler(RecipePermissionHandler permissionHandler) {
		this.permissionHandler = permissionHandler;
	}

	public AutoRecipeMatcherHandler getAutoRecipeHandler() {
		return autoRecipeHandler;
	}

	public void setAutoRecipeMatcherHandler(AutoRecipeMatcherHandler autoRecipeHandler) {
		this.autoRecipeHandler = autoRecipeHandler;
	}

	public class RecipeMatcherCallable implements Callable<IRecipe> {

		protected final AbstractCraftingWorkspace workspace;

		public RecipeMatcherCallable(AbstractCraftingWorkspace workspace) {
			this.workspace = workspace;
		}

		@Override
		public synchronized IRecipe call() {

			workspace.currentRecipe = match();

			if (workspace.currentRecipe == null)
				workspace.matchHandler.onNoRecipeMatch();
			else
				workspace.matchHandler.onRecipeMatch();

			return workspace.currentRecipe;
		}

		protected IRecipe match() {
			IRecipe match = null;
			if (workspace.getMatrix().getArray().length == 0)
				return match;

			if (pl.getRecipeManager().isBlacklisted(Stream.of(workspace.getMatrix().getArray())
					.map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()))) {
				workspace.getRecipeMatchCompletionHandler().onNoRecipeMatch();
				workspace.setCurrentRecipe(null);
				return null;
			}

			return match0();
		}

		protected IRecipe match0() {
			IRecipe match = null;
			if (workspace.matrix.getArray().length == 0)
				return match;

			if ((match = workspace.pl.getRecipeManager().matchRecipe(workspace.matrix)) != null)
				if (workspace.permissionHandler.canCraftRecipe(match, workspace.player))
					return workspace.currentRecipe = match;
				else
					workspace.permissionHandler.onNoPermission(match, workspace.player);

			if ((!FancyCrafting.vanillaRecipesAcceptPlainItemsOnly()
					|| (FancyCrafting.vanillaRecipesAcceptPlainItemsOnly() && !doIngredientsHaveMeta()))
					&& workspace.includeVanillaRecipes
					&& (match = workspace.vanillaMatcher.matchVanillaRecipe(workspace.matrix)) != null)
				if (workspace.permissionHandler.canCraftRecipe(match, workspace.player))
					return workspace.currentRecipe = match;
				else
					workspace.permissionHandler.onNoPermission(match, workspace.player);
			workspace.matchHandler.onNoRecipeMatch();
			return workspace.currentRecipe = null;
		}

		protected boolean doIngredientsHaveMeta() {
			return Stream.of(workspace.matrix.getArray()).filter(i -> i != null).map(IItemStack::getOriginal)
					.filter(ItemStack::hasItemMeta).findAny().isPresent();
		}
	}
}