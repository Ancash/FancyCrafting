package de.ancash.fancycrafting.gui.base;

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
import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.WorkspaceTemplate;
import de.ancash.fancycrafting.gui.base.handler.IAutoRecipeMatcherHandler;
import de.ancash.fancycrafting.gui.base.handler.IRecipeMatchHandler;
import de.ancash.fancycrafting.gui.base.handler.IRecipePermissionHandler;
import de.ancash.fancycrafting.recipe.AutoRecipeMatcher;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.inventory.IGUI;

public abstract class AbstractCraftingWorkspace extends IGUI {

	protected final AbstractFancyCrafting pl;
	protected final WorkspaceTemplate template;
	protected IMatrix<IItemStack> matrix = new IMatrix<>(new IItemStack[0], 0, 0);
	protected IRecipe currentRecipe;
	protected boolean includeVanillaRecipes;
	protected final Player player;
	protected final Object lock = new Object();
	protected int lastCraftTick = ILibrary.getTick();

	protected final AutoRecipeMatcher matcher;
	protected final VanillaRecipeMatcher vanillaMatcher;
	private IRecipeMatchHandler matchHandler;
	private IRecipePermissionHandler permissionHandler;
	private IAutoRecipeMatcherHandler autoRecipeHandler;

	public AbstractCraftingWorkspace(AbstractFancyCrafting pl, Player player, WorkspaceTemplate template) {
		this(pl, player, template, true);
	}

	public AbstractCraftingWorkspace(AbstractFancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes) {
		this(pl, player, template, includeVanillaRecipes,
				new AutoRecipeMatcher(player, pl.getRecipeManager().getAutoMatchingRecipes()));
	}

	public AbstractCraftingWorkspace(AbstractFancyCrafting pl, Player player, WorkspaceTemplate template,
			boolean includeVanillaRecipes, AutoRecipeMatcher matcher) {
		super(player.getUniqueId(), template.getDimension().getSize(), template.getTitle());
		this.matcher = matcher;
		this.pl = pl;
		this.template = template;
		this.includeVanillaRecipes = includeVanillaRecipes;
		this.player = player;
		this.vanillaMatcher = new VanillaRecipeMatcher(pl, player);
	}

	public void setAmount(int original, int subtract, int slot) {
		ItemStack is = getItem(slot);
		if (original - subtract <= 0)
			setItem(null, slot);
		else
			is.setAmount(original - subtract);
	}

	public boolean enableQuickCrafting() {
		return getTemplate().getSlots().enableQuickCrafting();
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
		return new RecipeMatcherCallable().call();
	}

	public Future<IRecipe> matchRecipeAsync() {
		return pl.submit(new RecipeMatcherCallable());
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
			
			if(pl.getRecipeManager().isBlacklisted(Stream.of(matrix.getArray()).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()))) {
				matchHandler.onNoRecipeMatch();
				return currentRecipe = null;
			}
			
			if ((match = pl.getRecipeManager().matchRecipe(matrix)) != null)
				if (permissionHandler.canCraftRecipe(match, player))
					return currentRecipe = match;
				else
					permissionHandler.onNoPermission(match, player);

			if (includeVanillaRecipes && (match = vanillaMatcher.matchVanillaRecipe(matrix)) != null)
				if (permissionHandler.canCraftRecipe(match, player))
					return currentRecipe = match;
				else
					permissionHandler.onNoPermission(match, player);
			matchHandler.onNoRecipeMatch();
			return currentRecipe = null;
		}
	}
}