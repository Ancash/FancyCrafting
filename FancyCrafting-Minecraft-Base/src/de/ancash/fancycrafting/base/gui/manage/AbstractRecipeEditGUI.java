package de.ancash.fancycrafting.base.gui.manage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Tuple;
import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.minecraft.inventory.input.StringInputGUI;

public abstract class AbstractRecipeEditGUI extends IGUI {

	protected final IRecipe recipe;
	protected final AbstractFancyCrafting plugin;

	protected final String title;
	protected final Player player;

	protected String recipeName;
	protected ItemStack[] ingredients;
	protected ItemStack result;
	protected boolean shaped;

	public AbstractRecipeEditGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getEditRecipeTitle());
	}

	public AbstractRecipeEditGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(player.getUniqueId(), 36, title.replace("%recipe%", recipe.getRecipeName())); //$NON-NLS-1$
		this.title = title.replace("%recipe%", recipe.getRecipeName()); //$NON-NLS-1$
		if (recipe.isVanilla())
			throw new IllegalArgumentException("Cannot edit vanilla recipe!"); //$NON-NLS-1$
		this.recipe = recipe;
		this.player = player;
		this.plugin = pl;
		this.recipeName = recipe.getRecipeName();
		this.result = recipe.getResult() == null || recipe.getResult().getType() == Material.AIR
				? plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().getOriginal()
				: recipe.getResult();
		if (recipe instanceof IShapedRecipe) {
			IShapedRecipe shapedRandom = (IShapedRecipe) recipe;
			ingredients = Arrays.asList(shapedRandom.getInMatrix(8, 6)).stream()
					.map(i -> i != null ? i.getOriginal() : null).toArray(ItemStack[]::new);
			this.shaped = true;
		} else {
			IShapelessRecipe shapelessRandom = (IShapelessRecipe) recipe;
			ingredients = new ItemStack[8 * 6];
			for (int i = 0; i < shapelessRandom.getIngredients().size(); i++)
				ingredients[i] = shapelessRandom.getIngredients().get(i);
			this.shaped = false;
		}
	}

	protected abstract void onMainMenuOpen();

	protected abstract void onRecipeSave();

	protected abstract void onRecipeDelete();

	protected abstract boolean isRecipeValid();

	@Override
	public void open() {
		openMainMenu();
	}

	protected final void openMainMenu() {
		IMatrix<ItemStack> matrix = new IMatrix<>(ingredients, 8, 6);
		matrix.optimize();
		matrix.cut(8, 6);
		ingredients = matrix.getArray();
		newInventory(title, 36);
		for (int i = 0; i < getSize(); i++)
			setItem(plugin.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);

		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getDeleteItem().getOriginal(), 27, (a, b,
				c, top) -> Optional.ofNullable(top ? this : null).ifPresent(AbstractRecipeEditGUI::onRecipeDelete)));
		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getSaveItem().getOriginal(), 35,
				(a, b, c, top) -> Optional.ofNullable(top && isRecipeValid() ? this : null)
						.ifPresent(AbstractRecipeEditGUI::onRecipeSave)));
		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getCloseItem().getOriginal(), 31,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(AbstractRecipeEditGUI::closeAll)));
		addManageResult();
		addManageIngredients();
		addManageRecipeName();
		IGUIManager.register(this, getId());
		onMainMenuOpen();
		super.open();
	}

	private void addManageRecipeName() {
		Map<String, String> placeholder = new HashMap<>();
		placeholder.put("%recipe%", recipeName); //$NON-NLS-1$
		addInventoryItem(
				new InventoryItem(this,
						ItemStackUtils.replacePlaceholder(
								plugin.getWorkspaceObjects().getManageRecipeNameItem().getOriginal(), placeholder),
						14, (a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> {
							IGUIManager.remove(getId());
							StringInputGUI sig = new StringInputGUI(plugin, player, (str) -> {
								this.recipeName = str;
								Bukkit.getScheduler().runTaskLater(plugin, () -> openMainMenu(), 1);
							}, (str) -> Tuple.of(str != null && !str.isEmpty(),
									str == null || str.isEmpty() ? plugin.getResponse().INVALID_RECIPE_NAME : null));
							sig.setLeft(plugin.getWorkspaceObjects().getInputRecipeNameLeftItem().getOriginal());
							sig.setRight(plugin.getWorkspaceObjects().getInputRecipeNameRightItem().getOriginal());
							sig.setText(recipeName);
							sig.setTitle(plugin.getWorkspaceObjects().getInputRecipeNameTitle());
							sig.open();
						})));
	}

	private void addManageResult() {
		addInventoryItem(new InventoryItem(this, result, 12, (slot, b, c, top) -> Optional.ofNullable(
				top && !plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().isSimilar(getItem(slot)) ? this
						: null)
				.ifPresent(self -> {
					if (player.getInventory().firstEmpty() != -1) {
						player.getInventory().addItem(result);
						result = plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().getOriginal();
						setItem(result, 12);
					}
				})));
	}

	private void addManageIngredients() {
		addInventoryItem(new InventoryItem(this,

				IngredientsInputGUI.getManageIngredientsItem(plugin, recipe.getIngredients(), recipe.getWidth(),
						recipe.getHeight(), shaped, recipe instanceof IRandomRecipe),
				10, (a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(AbstractRecipeEditGUI::openManageIngredients)));
	}

	private void openManageIngredients() {
		IngredientsInputGUI ingsIn = new IngredientsInputGUI(plugin, id, ingredients, shaped);
		ingsIn.onInput((ings, shaped) -> {
			this.shaped = shaped;
			this.ingredients = ings;
			Bukkit.getScheduler().runTaskLater(plugin, () -> openMainMenu(), 1);
		});
		ingsIn.open();
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getInventory())) {
			ItemStack clicked = event.getClickedInventory().getItem(event.getSlot());
			if (clicked == null || clicked.getType() == Material.AIR)
				return;
			if (!plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().isSimilar(result))
				event.getClickedInventory().setItem(event.getSlot(), result);
			else
				event.getClickedInventory().setItem(event.getSlot(), null);
			result = clicked;
			setItem(result, 12);
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(final InventoryDragEvent event) {
		event.setCancelled(true);
	}
}