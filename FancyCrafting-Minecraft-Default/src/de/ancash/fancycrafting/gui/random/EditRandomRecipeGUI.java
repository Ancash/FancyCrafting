package de.ancash.fancycrafting.gui.random;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.gui.base.AbstractEditRecipeGUI;
import de.ancash.fancycrafting.gui.base.ManageProbabilitiesGUI;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.misc.MathsUtils;

public class EditRandomRecipeGUI extends AbstractEditRecipeGUI {

	private final IRandomRecipe random;

	private Map<ItemStack, Integer> probabilityMap;

	public EditRandomRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getEditRecipeTitle());
	}

	public EditRandomRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
		this.random = (IRandomRecipe) recipe;
		this.probabilityMap = random.getProbabilityMap();
	}

	private void addManageProbabilities() {
		ItemStack item = plugin.getWorkspaceObjects().getManageRandomResultsItem().getOriginal();
		StringBuilder builder = new StringBuilder();
		int probSum = probabilityMap.values().stream().mapToInt(i -> Integer.valueOf(i)).sum();
		Iterator<String> iter = random.getProbabilityMap().entrySet().stream()
				.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
				.map(entry -> plugin.getWorkspaceObjects().getManageRandomResultsFormat()
						.replace("%prob%", String.valueOf(MathsUtils.round(100D * entry.getValue() / probSum, 2))) //$NON-NLS-1$
						.replace("%item%", ItemStackUtils.getDisplayName(entry.getKey()))) //$NON-NLS-1$
				.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (iter.hasNext())
				builder.append('\n');
		}
		Map<String, String> placeholder = new HashMap<>();
		placeholder.put("%probability_map%", builder.toString()); //$NON-NLS-1$
		ItemStackUtils.setLore(ItemStackUtils.replacePlaceholder(item.getItemMeta().getLore(), placeholder), item);
		addInventoryItem(new InventoryItem(this, item, 16, (a, b, c, top) -> Optional.ofNullable(top ? this : null)
				.ifPresent(EditRandomRecipeGUI::openManageProbabilities)));
	}

	private void openManageProbabilities() {
		ManageProbabilitiesGUI probGui = new ManageProbabilitiesGUI(plugin, id, probabilityMap);
		probGui.onComplete(map -> {
			probabilityMap = map;
			Bukkit.getScheduler().runTaskLater(plugin, () -> openMainMenu(), 1);
		});
		probGui.open();
	}

	@Override
	protected void onMainMenuOpen() {
		addManageProbabilities();
	}

	@SuppressWarnings("nls")
	@Override
	protected void onRecipeSave() {
		closeAll();
		try {
			plugin.getRecipeManager().saveRandomRecipe(result, ingredients, shaped, recipeName, recipe.getUUID(), 8, 6,
					probabilityMap);
			player.sendMessage(plugin.getResponse().RECIPE_SAVED);
			plugin.getRecipeManager().reloadRecipes();
		} catch (InvalidRecipeException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save recipe: " + result + ";" + Arrays.asList(ingredients)
					+ ";" + shaped + ";" + recipeName + ";" + recipe.getUUID());
		}
	}

	@SuppressWarnings("nls")
	@Override
	protected void onRecipeDelete() {
		closeAll();
		try {
			plugin.getRecipeManager().delete(recipe.getUUID().toString());
			player.sendMessage(plugin.getResponse().RECIPE_DELETED);
		} catch (RecipeDeleteException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not delete recipe: " + recipe, e);
		}
	}

	@Override
	public boolean isRecipeValid() {
		return !plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().isSimilar(result)
				&& Arrays.asList(ingredients).stream().filter(i -> i != null).findAny().isPresent()
				&& probabilityMap.size() > 0;
	}
}