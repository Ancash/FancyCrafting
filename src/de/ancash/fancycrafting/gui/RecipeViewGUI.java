package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.SerializableItemStack;

public class RecipeViewGUI{

	private FancyCrafting plugin;
	
	private final Set<UUID> openGUIs = new HashSet<>();
	private Integer[] craftingSlots = new Integer[9];
	private int resultSlot;
	private ItemStack[] template = new ItemStack[45];
	private int closeSlot;
	private int backSlot;
	private ItemStack close;
	private ItemStack back;
	private List<String> backCmdToExec;
	private String title;
	
	public RecipeViewGUI(FancyCrafting plugin) throws IOException, InvalidConfigurationException, org.bukkit.configuration.InvalidConfigurationException {
		this.plugin = plugin;
		File file = new File("plugins/FancyCrafting/config.yml");
		FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		ItemStack background = null;
		configuration.load(file);
		background = ItemStackUtils.get(configuration, "background");
		int cnt = 0;
		for(String slot : configuration.getStringList("workbench.crafting-slots")) {
			craftingSlots[cnt] = Integer.valueOf(slot);
			cnt++;
		}		
		closeSlot = configuration.getInt("recipe-view-gui.close-slot");
		backSlot = configuration.getInt("recipe-view-gui.back-slot");
		resultSlot = configuration.getInt("workbench.result-slot");
		back = ItemStackUtils.get(configuration, "recipe-view-gui.back");
		close = ItemStackUtils.get(configuration, "close");
		backCmdToExec = configuration.getStringList("recipe-view-gui.back.commands");
		title = configuration.getString("recipe-view-gui.title");
		configuration.save(file);
		
		for(int i = 0; i<45; i++) template[i] = background.clone();
		for(int i : craftingSlots) template[i] = null;
		template[closeSlot] = close;
		template[backSlot] = back;
		template[resultSlot] = null;
	}
	
	public void open(Player player, IRecipe recipe) {
		ItemStack[] clone = template.clone();
		clone[resultSlot] = recipe.getResult();
		if(recipe instanceof IShapedRecipe) {
			IShapedRecipe shaped = (IShapedRecipe) recipe;
			shaped.getIngredientsMap().entrySet().forEach(entry -> clone[craftingSlots[entry.getKey() - 1]] = entry.getValue().restore());
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			List<SerializableItemStack> ingredients = shapeless.getIngredients().stream().collect(Collectors.toList());
			for(int i = 0; i<ingredients.size(); i++)
				clone[craftingSlots[i]] = ingredients.get(i).restore();
		}
		Inventory inv = Bukkit.createInventory(null, 45, title.replace("%r", recipe.getName().replace("-", " ")));
		inv.setContents(clone);
		player.openInventory(inv);
		openGUIs.add(player.getUniqueId());
	}
	
	public boolean hasInventoryOpen(HumanEntity player) {
		return openGUIs.contains(player.getUniqueId());
	}
	
	public void onClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if(event.getClickedInventory() == null) return;
		boolean topInv = event.getInventory().equals(event.getClickedInventory());
		
		if(!topInv) {
			IRecipe recipe = plugin.getRecipeManager().getRecipeByResult(new SerializableItemStack(event.getView().getBottomInventory().getItem(event.getSlot())));
			if(recipe != null) {
				event.getWhoClicked().closeInventory();
				openGUIs.remove(event.getWhoClicked().getUniqueId());
				open((Player) event.getWhoClicked(), recipe);
				return;
			}
		}
		
		if(event.getSlot() == closeSlot && !event.isShiftClick() && topInv) {
			event.getWhoClicked().closeInventory();
			return;
		}
		
		if(event.getSlot() == backSlot && topInv) {
			for(String cmd : backCmdToExec) {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%p", event.getWhoClicked().getName()));
			}
			return;
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
