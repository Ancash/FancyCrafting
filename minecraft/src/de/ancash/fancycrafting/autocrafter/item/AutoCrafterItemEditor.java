package de.ancash.fancycrafting.autocrafter.item;

import static de.ancash.fancycrafting.NBTKeys.*;
import static de.ancash.nbtnexus.serde.access.MapAccessUtil.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerializedItem;

public class AutoCrafterItemEditor extends IGUI {

	private final List<SerializedItem> items = new ArrayList<>();
	private final FancyCrafting pl;
	private final SerializedItem editing;
	private int slots;

	@SuppressWarnings("nls")
	public AutoCrafterItemEditor(FancyCrafting pl, UUID id, ItemStack editing) {
		super(id, 54, pl.getWorkspaceObjects().getAutoCrafterEditorTitle());
		this.editing = SerializedItem.of(ItemSerializer.INSTANCE.serializeItemStack(editing), false);
		this.pl = pl;
		if (!isValid(this.editing))
			throw new IllegalArgumentException("invalid item " + this.editing.getMap());
		loadFromNBT();
		setItems();
		IGUIManager.register(this, getId());
		open();
	}

	@SuppressWarnings("nls")
	private void setItems() {
		for (int i = 0; i < inv.getSize(); i++) {
			if (i < slots)
				setItem(pl.getWorkspaceObjects().getAutoCrafterVacantSlotItem().toItem(), i);
			else
				setItem(pl.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
		}
		for (int i = 0; i < items.size(); i++) {
			List<IRecipe> recipes = new ArrayList<>(pl.getRecipeManager().getRecipeByResult(items.get(i)));
			ItemStack result = items.get(i).toItem();
			Iterator<List<String>> iter = recipes.stream()
					.map(r -> IRecipe.ingredientsToList(pl,
							r.getIngredients().toArray(new ItemStack[r.getIngredients().size()]), r.getWidth(),
							r.getHeight(), pl.getWorkspaceObjects().getViewIngredientsIdFormat()))
					.iterator();
			List<String> lore = new ArrayList<>();
			int pos = 0;
			while (iter.hasNext()) {
				IRecipe r = recipes.get(pos++);
				if (!FancyCrafting.canCraftRecipe(r, Bukkit.getPlayer(id)))
					continue;
				lore.add(pl.getWorkspaceObjects().getAutoCrafterEditorRecipeNameFormat().replace("%recipe%",
						r.getRecipeName()));
				lore.add(pl.getWorkspaceObjects().getAutoCrafterEditorRecipeCategoryFormat().replace("%category%",
						r.getCategory().getName()));
				lore.addAll(iter.next());
				if (iter.hasNext())
					lore.add(pl.getWorkspaceObjects().getAutoCrafterEditorSeperator());
			}
			ItemStackUtils.setLore(lore, result);
			result.setAmount(1);
			setItem(result, i);
		}
	}

	public static boolean isValid(SerializedItem editing) {
		return exists(editing.getMap(), AUTO_RECIPES_SLOTS_PATH);
	}

	private void loadFromNBT() {
		slots = getInt(editing.getMap(), AUTO_RECIPES_SLOTS_PATH);
		List<Map<String, Object>> list = getList(editing.getMap(), AUTO_RECIPES_RESULTS_PATH);
		if (list == null)
			list = new ArrayList<>();
		for (int i = 0; i < list.size() && i < slots; i++)
			items.add(SerializedItem.of(list.get(i)));
		newInventory(title, Math.min((slots / 9 + 1 - (slots % 9 == 0 ? 1 : 0)) * 9, 54));
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() == null)
			return;
		if (event.getClickedInventory().equals(inv)) {
			if (event.getSlot() >= items.size())
				return;
			items.remove(event.getSlot());
			setItems();
			return;
		}

		ItemStack item = event.getClickedInventory().getItem(event.getSlot());
		if (item == null || item.getType() == Material.AIR)
			return;
		if (items.size() >= slots)
			return;
		SerializedItem si = SerializedItem.of(item);

		if (items.contains(si))
			return;

		Set<IRecipe> recipes = pl.getRecipeManager().getRecipeByResult(si);
		if (recipes == null || recipes.isEmpty())
			return;

		items.add(si);
		setItems();
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public void onInventoryClose(InventoryCloseEvent arg0) {
		IGUIManager.remove(id);
		editing.getMap(AUTO_RECIPES_COMPOUND_PATH).put(AUTO_RECIPES_RESULTS_TAG,
				items.stream().map(SerializedItem::getMap).collect(Collectors.toList()));
		Bukkit.getPlayer(id).getInventory().setItemInHand(editing.toItem());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent arg0) {
		arg0.setCancelled(true);
	}
}
