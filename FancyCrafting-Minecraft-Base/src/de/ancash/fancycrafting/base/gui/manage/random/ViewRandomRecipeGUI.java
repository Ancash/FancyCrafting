package de.ancash.fancycrafting.base.gui.manage.random;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.gui.AbstractRecipeViewGUI;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.MathsUtils;

public class ViewRandomRecipeGUI extends AbstractRecipeViewGUI {

	private final IRandomRecipe random;

	private Map<ItemStack, Integer> probabilityMap;

	public ViewRandomRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getViewRecipeTitle());
	}

	public ViewRandomRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
		this.random = (IRandomRecipe) recipe;
		this.probabilityMap = random.getProbabilityMap();
	}

	private void addProbabilities() {
		ItemStack item = plugin.getWorkspaceObjects().getViewRandomResultsItem().getOriginal();
		StringBuilder builder = new StringBuilder();
		int probSum = probabilityMap.values().stream().mapToInt(i -> Integer.valueOf(i)).sum();
		Iterator<String> iter = random.getProbabilityMap().entrySet().stream()
				.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
				.map(entry -> plugin.getWorkspaceObjects().getViewRandomResultsFormat()
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
		setItem(ItemStackUtils.setLore(ItemStackUtils.replacePlaceholder(item.getItemMeta().getLore(), placeholder),
				item), plugin.getViewSlots().getProbabilitySlot());
	}

	@Override
	protected void onMainMenuOpen() {
		addProbabilities();
	}

	@Override
	protected void editRecipe(Player player, IRecipe recipe) {
		new EditRandomRecipeGUI(plugin, player, recipe).open();
	}
}