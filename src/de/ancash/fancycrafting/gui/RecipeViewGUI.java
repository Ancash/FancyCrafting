package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.fancycrafting.utils.MiscUtils;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class RecipeViewGUI{

	@SuppressWarnings("unused")
	private FancyCrafting plugin;
	
	private final Set<UUID> openGUIs = new HashSet<>();
	private Integer[] craftingSlots = new Integer[9];
	private int resultSlot;
	private ItemStack[] template = new ItemStack[45];
	
	public RecipeViewGUI(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		this.plugin = plugin;
		YamlFile configuration = new YamlFile(new File("plugins/FancyCrafting/config.yml"));
		ItemStack background = null;
		configuration.load();
		background = MiscUtils.get(configuration, "background");
		int cnt = 0;
		for(String slot : configuration.getStringList("workbench.crafting-slots")) {
			craftingSlots[cnt] = Integer.valueOf(slot);
			cnt++;
		}		
		resultSlot = configuration.getInt("workbench.result-slot");
		configuration.save();
		
		for(int i = 0; i<45; i++) template[i] = background.clone();
		for(int i : craftingSlots) template[i] = null;
		template[resultSlot] = null;
	}
	
	public void open(Player player, IRecipe recipe) {
		ItemStack[] clone = template.clone();
		clone[resultSlot] = recipe.getResult();
		if(recipe instanceof IShapedRecipe) {
			IShapedRecipe shaped = (IShapedRecipe) recipe;
			shaped.getIngredientsMap().entrySet().forEach(entry -> clone[craftingSlots[entry.getKey() - 1]] = entry.getValue());
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			for(int i = 0; i<shapeless.getIngredientsList().size(); i++)
				clone[i] = shapeless.getIngredientsList().toArray(new ItemStack[shapeless.getIngredientsList().size()])[i];
		}
		Inventory inv = Bukkit.createInventory(null, 45, "View Recipe: " + recipe.getName());
		inv.setContents(clone);
		player.openInventory(inv);
		openGUIs.add(player.getUniqueId());
	}
	
	public boolean hasInventoryOpen(HumanEntity player) {
		return openGUIs.contains(player.getUniqueId());
	}
	
	public void onClick(InventoryClickEvent event) {
		if(openGUIs.contains(event.getWhoClicked().getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	public void onDrag(InventoryDragEvent event) {
		if(openGUIs.contains(event.getWhoClicked().getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	public void close(InventoryCloseEvent event) {
		openGUIs.remove(event.getPlayer().getUniqueId());
	}
}
