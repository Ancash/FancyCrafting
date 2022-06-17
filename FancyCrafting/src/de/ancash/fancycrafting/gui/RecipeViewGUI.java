
package de.ancash.fancycrafting.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemBuilder;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
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
				recipe.getName());
		this.plugin = pl;
		this.player = player;
		for (int i = 0; i < getSize(); i++)
			setItem(pl.getBackgroundItem().getOriginal(), i);
		for (int i : WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight()).getSlots().getCraftingSlots())
			setItem(null, i);
		setItem(null, WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight()).getSlots().getResultSlot());
		IGUIManager.register(this, getId());
		openRecipe(recipe);
		player.openInventory(getInventory());
	}

	public void openRecipe(IRecipe recipe) {
		WorkspaceTemplate template = WorkspaceTemplate.get(recipe.getWidth(), recipe.getHeight());
		if (getSize() != template.getDimension().getSize()) {
			newInventory(recipe.getName(), template.getDimension().getSize());
			for (int i = 0; i < getSize(); i++)
				setItem(plugin.getBackgroundItem().getOriginal(), i);
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
		addInventoryItem(new InventoryItem(this, plugin.getCloseItem().getOriginal(),
				template.getSlots().getCloseSlot(), new Clickable() {

					@Override
					public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
						if (topInventory)
							player.closeInventory();
					}
				}));
		addInventoryItem(
				new InventoryItem(this, plugin.getBackItem(), template.getSlots().getBackSlot(), new Clickable() {

					@Override
					public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
						if (topInventory)
							for (String cmd : plugin.getBackCommands())
								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
										cmd.replace("%p", player.getName()));
					}
				}));

		if (player.hasPermission("fancycrafting.admin.edit") && !recipe.isVanilla())
			addInventoryItem(new InventoryItem(this,
					new ItemBuilder(XMaterial.WRITABLE_BOOK).setDisplayname("§aClick to edit recipe").build(),
					template.getSlots().getEditSlot(), new Clickable() {

						@Override
						public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
							if (topInventory) {
								player.closeInventory();
								new RecipeEditGUI(plugin, player, recipe);
							}
						}
					}));
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
