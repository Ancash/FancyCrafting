package de.ancash.fancycrafting.gui;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class RecipeEditGUI extends IGUI {

	private final IRecipe edit;
	private final FancyCrafting plugin;
	private final CraftingTemplate template = CraftingTemplate.get(8, 6);

	public RecipeEditGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		super(player.getUniqueId(), CraftingTemplate.get(8, 6).getSize(),
				pl.getEditRecipeTitle().replace("%r", recipe.getName()));
		if (recipe.isVanilla())
			throw new IllegalArgumentException("Cannot edit vanilla recipe!");
		this.edit = recipe;
		this.plugin = pl;
		for (int i = 0; i < getSize(); i++)
			setItem(pl.getBackgroundItem(), i);
		for (int i : template.getCraftingSlots())
			setItem(null, i);
		if (recipe instanceof IShapedRecipe)
			setItem(pl.getShapedItem(), template.getRecipeTypeSlot());
		else
			setItem(pl.getShapelessItem(), template.getRecipeTypeSlot());
		setItem(recipe.getResult(), template.getResultSlot());
		setItem(pl.getSaveItem(), template.getSaveSlot());
		if (recipe instanceof IShapedRecipe) {
			IShapedRecipe shaped = (IShapedRecipe) recipe;
			IItemStack[] ings = shaped.getInMatrix(8, 6);
			for (int i = 0; i < ings.length; i++)
				if (ings[i] != null)
					setItem(ings[i] == null ? null : ings[i].getOriginal(), template.getCraftingSlots()[i]);
		} else {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			int i = 0;
			for (ItemStack ingredient : shapeless.getIngredients()) {
				setItem(ingredient, template.getCraftingSlots()[i]);
				i++;
			}
		}
		addInventoryItem(new InventoryItem(this, pl.getDeleteItem(), template.getDeleteSlot(), new Clickable() {

			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if (topInventory) {
					player.closeInventory();
					try {
						plugin.getRecipeManager().delete(recipe.getUUID().toString());
						player.sendMessage("§aRecipe deleted");
					} catch (IOException | InvalidConfigurationException e) {
						e.printStackTrace();
					}
				}
			}
		}));
		IGUIManager.register(this, getId());
		player.openInventory(getInventory());
	}

	private boolean isCraftingSlot(int a) {
		for (int i : template.getCraftingSlots())
			if (i == a)
				return true;
		return a == template.getResultSlot();
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		InventoryAction a = event.getAction();
		if (event.getClick().equals(ClickType.DOUBLE_CLICK) || (a.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
				&& !event.getInventory().equals(event.getClickedInventory()))) {
			event.setCancelled(true);
			return;
		}
		if (event.getInventory().equals(event.getClickedInventory())) {
			int slot = event.getSlot();
			if (isCraftingSlot(slot)) {
				return;
			} else {
				event.setCancelled(true);
			}
			boolean isShaped = event.getInventory().getItem(template.getRecipeTypeSlot()).getItemMeta().getDisplayName()
					.contains("Shaped");
			if (slot == template.getRecipeTypeSlot()) {
				if (!isShaped)
					event.getInventory().setItem(template.getRecipeTypeSlot(), plugin.getShapedItem());
				else
					event.getInventory().setItem(template.getRecipeTypeSlot(), plugin.getShapelessItem());
			}

			if (slot == template.getSaveSlot()) {
				ItemStack result = event.getInventory().getItem(template.getResultSlot());
				if (result == null) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				ItemStack[] ings = new ItemStack[template.getCraftingSlots().length];
				for (int i = 0; i < ings.length; i++)
					ings[i] = getItem(template.getCraftingSlots()[i]);
				if (!Arrays.asList(ings).stream()
						.filter(s -> s != null && !s.getType().equals(XMaterial.AIR.parseMaterial())).findAny()
						.isPresent()) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				try {
					plugin.getRecipeManager().saveRecipe(result, ings, isShaped, edit.getName(), edit.getUUID(), 8, 6);
					event.getWhoClicked().sendMessage("§aEdited §r" + edit.getName());
					plugin.getRecipeManager().reloadRecipes();
				} catch (IOException | InvalidConfigurationException e) {
					event.getWhoClicked().sendMessage("§cSomething went wrong while saving: " + e);
				} finally {
					event.getWhoClicked().closeInventory();
				}
			}
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		for (int i : event.getInventorySlots())
			if (!isCraftingSlot(i)) {
				event.setCancelled(true);
				return;
			}
	}
}