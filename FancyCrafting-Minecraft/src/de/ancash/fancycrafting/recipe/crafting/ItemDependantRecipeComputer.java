package de.ancash.fancycrafting.recipe.crafting;

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

public class ItemDependantRecipeComputer implements IRecipeComputer {

	protected final UUID player;
	protected final FancyCrafting plugin;

	public ItemDependantRecipeComputer(FancyCrafting plugin, UUID player) {
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

			if (!MapAccessUtil.exists(item.getMap(), NBTKeys.FANCY_CRAFTING_NBT_COMPOUND_TAG))
				continue;

			if (!MapAccessUtil.exists(item.getMap(), NBTKeys.AUTO_RECIPES_TAG))
				continue;
			List<Map<String, Object>> serializedResults = MapAccessUtil.getList(item.getMap(),
					NBTKeys.AUTO_RECIPES_TAG);
			for (Map<String, Object> serializedResult : serializedResults) {
				recipes.addAll(plugin.getRecipeManager().getRecipeByHash(SerializedItem.of(serializedResult)));
			}
		}
		System.out.println("contained recipes: " + recipes);
		return recipes;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

}
