package de.ancash.fancycrafting.gui.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import de.ancash.ILibrary;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.crafting.AutoRecipeMatcher;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.nbtnexus.serde.SerializedItem;

public class AutoRecipeMatcherHandler {

	private Set<Integer> quickCraftingResultHashCodes = new HashSet<>();
	private int quickCraftingPage;
	private List<IRecipe> quickCraftingRecipes = new ArrayList<>();
	private final AbstractCraftingWorkspace workspace;
	private final FancyCrafting pl;
	private final AutoRecipeMatcher matcher;

	public AutoRecipeMatcherHandler(FancyCrafting pl, AbstractCraftingWorkspace workspace, AutoRecipeMatcher matcher) {
		this.workspace = workspace;
		this.pl = pl;
		this.matcher = matcher;
	}

	public void autoMatch() {
		matcher.compute();
		onAutoMatchFinish();
	}

	public Set<Integer> getResutlHashCodes() {
		return quickCraftingResultHashCodes;
	}

	public void onAutoRecipesChangePage(InventoryClickEvent e) {
		if (!workspace.enableQuickCrafting())
			return;
		if (e.isRightClick()) {
			if ((quickCraftingPage + 1)
					* workspace.getTemplate().getSlots().getAutoCraftingSlots().length < quickCraftingRecipes.size())
				quickCraftingPage++;
		} else {
			if (quickCraftingPage > 0)
				quickCraftingPage--;
		}
		if (quickCraftingPage < quickCraftingRecipes.size()) {
			int i;
			for (i = quickCraftingPage
					* workspace.getTemplate().getSlots().getAutoCraftingSlots().length; i < (quickCraftingPage + 1)
							* workspace.getTemplate().getSlots().getAutoCraftingSlots().length
							&& i < quickCraftingRecipes.size(); i++)
				addQuickCraftingItem(quickCraftingRecipes.get(i),
						i - (quickCraftingPage * workspace.getTemplate().getSlots().getAutoCraftingSlots().length));

			while (i < workspace.getTemplate().getSlots().getAutoCraftingSlots().length
					+ (quickCraftingPage * workspace.getTemplate().getSlots().getAutoCraftingSlots().length)) {
				workspace.removeInventoryItem(workspace.getTemplate().getSlots().getAutoCraftingSlots()[i
						- (quickCraftingPage * workspace.getTemplate().getSlots().getAutoCraftingSlots().length)]);
				workspace.setItem(pl.getWorkspaceObjects().getQuickCraftingItem().getOriginal(),
						workspace.getTemplate().getSlots().getAutoCraftingSlots()[i - (quickCraftingPage
								* workspace.getTemplate().getSlots().getAutoCraftingSlots().length)]);
				i++;
			}
		}
	}

	private void addQuickCraftingItem(IRecipe recipe, int pos) {
		workspace.addInventoryItem(new InventoryItem(workspace, recipe.getResult(),
				workspace.getTemplate().getSlots().getAutoCraftingSlots()[pos], (a, shift, c, top) -> {
					if (top) {
						onQuickCraft(recipe, shift);
						workspace.updateQuickCrafting();
					}
				}));
	}

	public void onAutoMatchFinish() {
		if (Bukkit.isPrimaryThread())
			onAutoMatchFinish0();
		else
			Bukkit.getScheduler().runTask(pl, () -> onAutoMatchFinish0());
	}

	private void onAutoMatchFinish0() {
		quickCraftingPage = 0;
		quickCraftingRecipes = new ArrayList<>(matcher.getMatchedRecipes());
		Set<Integer> temp = new HashSet<>();
		Collections.sort(quickCraftingRecipes, (a, b) -> a.getResultName().compareToIgnoreCase(b.getResultName()));
		int i = 0;
		for (i = 0; i < workspace.getTemplate().getSlots().getAutoCraftingSlots().length
				&& i < quickCraftingRecipes.size(); i++) {
			IRecipe recipe = quickCraftingRecipes.get(i);
			temp.add(recipe.getResultAsSerializedItem().hashCode());
			addQuickCraftingItem(recipe, i);
		}
		while (i < workspace.getTemplate().getSlots().getAutoCraftingSlots().length) {
			workspace.removeInventoryItem(workspace.getTemplate().getSlots().getAutoCraftingSlots()[i]);
			workspace.setItem(pl.getWorkspaceObjects().getQuickCraftingItem().getOriginal(),
					workspace.getTemplate().getSlots().getAutoCraftingSlots()[i]);
			i++;
		}
		quickCraftingResultHashCodes = Collections.unmodifiableSet(temp);
	}

	private void onQuickCraft(IRecipe recipe, boolean shift) {
		if (ILibrary.getTick() - pl.getCraftingCooldown() <= workspace.getLastCraftTick()) {
			workspace.getPlayer().sendMessage(pl.getResponse().CRAFTING_COOLDOWN_MESSAGE);
			return;
		}
		if (workspace.getPlayer().getInventory().firstEmpty() == -1) {
			return;
		}
		// shift ignored, only single click
		Map<Integer, Duplet<SerializedItem, Integer>> map = new HashMap<>();
		for (ItemStack is : recipe.getIngredients()) {
			if (is == null || is.getType() == Material.AIR)
				continue;
			int amt = is.getAmount();
			is.setAmount(1);
			SerializedItem iis = SerializedItem.of(is);
			map.merge(iis.hashCode(), Tuple.of(iis, amt),
					(a, b) -> Tuple.of(a.getFirst(), a.getSecond() + b.getSecond()));
		}
		map.values().forEach(
				d -> InventoryUtils.removeItemStack(workspace.getPlayer(), d.getFirst().toItem(), d.getSecond()));
		workspace.getPlayer().getInventory().addItem(
				workspace.getRecipeMatchCompletionHandler().getSingleRecipeCraft(recipe, workspace.getPlayer()));
		workspace.updateLastCraftTick();
	}
}
