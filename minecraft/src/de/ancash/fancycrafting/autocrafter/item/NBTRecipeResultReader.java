package de.ancash.fancycrafting.autocrafter.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.NBTKeys;
import de.ancash.fancycrafting.autocrafter.IRecipeComputer;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.MapAccessUtil;

public class NBTRecipeResultReader implements IRecipeComputer, Listener {

	protected final UUID player;
	protected final FancyCrafting plugin;

	public NBTRecipeResultReader(FancyCrafting plugin, UUID player) {
		this.player = player;
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(!event.getPlayer().getUniqueId().equals(player))
			return;
		ItemStack item = event.getPlayer().getItemInHand();
		if(item == null || item.getType() == Material.AIR)
			return;
		if(!AutoCrafterItemEditor.isValid(SerializedItem.of(item)))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(!event.getPlayer().hasPermission(FancyCrafting.AUTO_CRAFTER_EDITOR_PERM) && !event.getPlayer().isOp())
			return;
		
		new AutoCrafterItemEditor(plugin, player, item);
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}

}
