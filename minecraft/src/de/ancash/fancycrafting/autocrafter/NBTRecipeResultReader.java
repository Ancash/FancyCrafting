package de.ancash.fancycrafting.autocrafter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.NBTKeys;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.MapAccessUtil;

public class NBTRecipeResultReader implements IRecipeComputer {

	protected final UUID player;
	protected final FancyCrafting plugin;

	public NBTRecipeResultReader(FancyCrafting plugin, UUID player) {
		this.player = player;
		this.plugin = plugin;
	}

	@Override
	public List<IRecipe> computeRecipes() {
		List<IRecipe> recipes = new ArrayList<>();
		PlayerInventory inv = Bukkit.getPlayer(player).getInventory();

		for (SerializedItem item : Arrays.asList(inv.getContents()).stream()
				.filter(i -> i != null && i.getType() != Material.AIR).map(SerializedItem::of)
				.collect(Collectors.toList())) {

			if (!MapAccessUtil.exists(item.getMap(), NBTKeys.AUTO_RECIPES_RESULTS_PATH))
				continue;

			List<Map<String, Object>> serializedResults = MapAccessUtil.getList(item.getMap(),
					NBTKeys.AUTO_RECIPES_RESULTS_PATH);
			for (Map<String, Object> serializedResult : serializedResults)
				recipes.addAll(plugin.getRecipeManager().getRecipeByResult(SerializedItem.of(serializedResult)));
		}
		return recipes;
	}

	@Override
	public boolean isAsync() {
		return true;
	}

}
