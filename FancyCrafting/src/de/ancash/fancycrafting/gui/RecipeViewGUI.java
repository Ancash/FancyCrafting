package de.ancash.fancycrafting.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.gui.random.EditRandomRecipeGUI;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class RecipeViewGUI extends IGUI {

	public static void viewRecipe(FancyCrafting pl, IRecipe r, Player p) {
		viewRecipe(pl, new HashSet<>(Arrays.asList(r)), p);
	}

	public static void viewRecipe(FancyCrafting pl, Set<IRecipe> recipes, Player p) {
		if (recipes.size() == 1) {
			IRecipe recipe = recipes.stream().findFirst().get();
			new RecipeViewGUI(pl, p, recipe);
			return;
		} else if (recipes.size() > 1) {
			new RecipesCollectionViewGUI(pl, p, new ArrayList<>(recipes));
		}
	}

	private final FancyCrafting plugin;
	private final Player player;

	public RecipeViewGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		super(player.getUniqueId(),
				WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight()).getDimension().getSize(),
				recipe.getRecipeName());
		this.plugin = pl;
		this.player = player;
		for (int i = 0; i < getSize(); i++)
			setItem(pl.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
		for (int i : WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight()).getSlots().getCraftingSlots())
			setItem(null, i);
		setItem(null, WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight()).getSlots().getResultSlot());
		IGUIManager.register(this, getId());
		openRecipe(recipe);
		open();
	}

	public void openRecipe(IRecipe recipe) {
		WorkspaceTemplate template = WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight());
		if (getSize() != template.getDimension().getSize()) {
			newInventory(recipe.getRecipeName(), template.getDimension().getSize());
			for (int i = 0; i < getSize(); i++)
				setItem(plugin.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
		}
		clearInventoryItems();
		for (int i : template.getSlots().getCraftingSlots())
			setItem(null, i);

		setItem(recipe.getResult(), template.getSlots().getResultSlot());
		if (recipe instanceof IShapedRecipe) {
			IShapedRecipe shaped = (IShapedRecipe) recipe;
			IItemStack[] ings = shaped.getInMatrix(template.getDimension().getWidth(),
					template.getDimension().getHeight());
			for (int i = 0; i < ings.length; i++)
				setItem(ings[i] == null ? null : ings[i].getOriginal(), template.getSlots().getCraftingSlots()[i]);
		}
		if (recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			for (int i = 0; i < shapeless.getIngredients().size(); i++)
				setItem((ItemStack) shapeless.getIngredients().toArray()[i], template.getSlots().getCraftingSlots()[i]);
		}
		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getCloseItem().getOriginal(),
				template.getSlots().getCloseSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(RecipeViewGUI::closeAll)));
		addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getBackItem().getOriginal(),
				template.getSlots().getBackSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(
						self -> plugin.getWorkspaceObjects().getBackCommands().forEach(cmd -> Bukkit.getServer()
								.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())))))); //$NON-NLS-1$
		if (player.hasPermission(FancyCrafting.EDIT_PERM) && !recipe.isVanilla())
			addInventoryItem(new InventoryItem(this, plugin.getWorkspaceObjects().getEditItem().getOriginal(),
					template.getSlots().getEditSlot(),
					(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> {
						if (recipe instanceof IRandomRecipe)
							new EditRandomRecipeGUI(plugin, player, recipe).open();
						else
							new EditNormalRecipeGUI(plugin, player, recipe).open();
					})));
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() == null)
			return;
		boolean topInv = event.getInventory().equals(event.getClickedInventory());
		if (!topInv) {
			if (getInventory().getItem(event.getSlot()) == null
					|| getInventory().getItem(event.getSlot()).getType().equals(XMaterial.AIR.parseMaterial()))
				return;
			Set<IRecipe> recipes = plugin.getRecipeManager()
					.getRecipeByResult(new IItemStack(event.getView().getBottomInventory().getItem(event.getSlot())));
			if (recipes != null && !recipes.isEmpty()) {
				closeAll();
				RecipeViewGUI.viewRecipe(plugin, recipes, player);
				return;
			}
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}
