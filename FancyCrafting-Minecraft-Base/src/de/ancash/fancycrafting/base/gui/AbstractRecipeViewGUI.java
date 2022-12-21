package de.ancash.fancycrafting.base.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public abstract class AbstractRecipeViewGUI extends IGUI {

	protected final IRecipe recipe;
	protected final AbstractFancyCrafting plugin;
	protected final ItemStack[] ingredients;
	protected final WorkspaceTemplate template;
	protected final Map<String, String> placeholder = new HashMap<>();
	protected Consumer<AbstractRecipeViewGUI> on;

	protected final String title;
	protected final Player player;
	protected boolean viewingIngredients;

	public AbstractRecipeViewGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getViewRecipeTitle());
	}

	@SuppressWarnings("nls")
	public AbstractRecipeViewGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(player.getUniqueId(), pl.getViewSlots().getSize(), title.replace("%recipe%", recipe.getRecipeName())); //$NON-NLS-1$
		this.title = title.replace("%recipe%", recipe.getRecipeName()); //$NON-NLS-1$
		this.recipe = recipe;
		this.player = player;
		this.plugin = pl;
		this.template = WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight());
		if (recipe instanceof IShapedRecipe) {
			ingredients = Arrays.asList(((IShapedRecipe) recipe).getIngredientsArray()).stream()
					.map(i -> i == null ? null : i.getOriginal()).toArray(ItemStack[]::new);
		} else {
			ingredients = new ItemStack[template.getDimension().getWidth() * template.getDimension().getHeight()];
			for (int i = 0; i < recipe.getIngredients().size(); i++)
				ingredients[i] = recipe.getIngredients().get(i);
		}
		placeholder.put("%rtype%", (recipe instanceof IShapedRecipe ? "Shaped" : "Shapeless")
				+ (recipe instanceof IRandomRecipe ? " Random" : "") + " Recipe");
		placeholder.put("%ingredients%", String.join("\n", IRecipe.ingredientsToList(plugin, ingredients,
				recipe.getWidth(), recipe.getHeight(), plugin.getWorkspaceObjects().getViewIngredientsIdFormat())));
	}

	protected abstract void onMainMenuOpen();

	public void onOpen(Consumer<AbstractRecipeViewGUI> on) {
		this.on = on;
	}

	@Override
	public void open() {
		openMainMenu();
	}

	protected final void openMainMenu() {
		newInventory(title, plugin.getViewSlots().getSize());
		for (int i = 0; i < getSize(); i++) {
			removeInventoryItem(i);
			setItem(plugin.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
		}

		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getCloseItem().getOriginal(),
				plugin.getViewSlots().getCloseSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(AbstractRecipeViewGUI::closeAll)));
		setItem(recipe.getResult(), plugin.getViewSlots().getResultSlot());
		addIngredients();
		addEdit();
		IGUIManager.register(this, getId());
		onMainMenuOpen();
		super.open();
		if (on != null)
			on.accept(this);
	}

	private void addEdit() {
		if (player.hasPermission(AbstractFancyCrafting.EDIT_PERM) && !recipe.isVanilla())
			addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getEditItem().getOriginal(),
					plugin.getViewSlots().getEditSlot(), (a, b, c, top) -> Optional.ofNullable(top ? this : null)
							.ifPresent(self -> editRecipe(player, recipe))));
	}

	protected abstract void editRecipe(Player player, IRecipe recipe);

	private void addIngredients() {
		addInventoryItem(new InventoryItem(this,
				ItemStackUtils.replacePlaceholder(plugin.getWorkspaceObjects().getViewIngredientsItem().getOriginal(),
						placeholder),
				plugin.getViewSlots().getIngredientsSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> {
					newInventory(getTitle(), template.getDimension().getSize());
					for (int i = 0; i < getSize(); i++)
						setItem(plugin.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
					for (int i = 0; i < ingredients.length; i++)
						setItem(ingredients[i], template.getSlots().getCraftingSlots()[i]);
					super.open();
					viewingIngredients = true;
				})));
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() == null || event.getClickedInventory().equals(event.getInventory()))
			return;
		ItemStack clicked = event.getClickedInventory().getItem(event.getSlot());
		if (clicked == null)
			return;
		IItemStack ii = new IItemStack(clicked);
		Set<IRecipe> r = plugin.getRecipeManager().getRecipeByHash(ii);
		if (r == null)
			return;
		r = r.stream().filter(i -> AbstractFancyCrafting.canCraftRecipe(i, player)).collect(Collectors.toSet());
		if (r.isEmpty())
			return;
		plugin.viewRecipeSingle(player, r);
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!viewingIngredients)
			IGUIManager.remove(getId());
		else {
			viewingIngredients = false;
			Bukkit.getScheduler().runTaskLater(plugin, () -> open(), 1);
		}
	}

	@Override
	public void onInventoryDrag(final InventoryDragEvent event) {
		event.setCancelled(true);
	}
}