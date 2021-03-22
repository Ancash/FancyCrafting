package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.utils.MiscUtils;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class RecipeCreateGUI extends IGUI{

	private final FancyCrafting plugin;
	private ItemStack[] template = new ItemStack[45];
	private String name;
	private CompactMap<UUID, RecipeCreateGUI> openGUIs = new CompactMap<>();
	private int resultSlot;
	private Integer[] craftingSlots;
	
	public RecipeCreateGUI(FancyCrafting plugin) throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		super(null, null, null, -1);
		this.plugin = plugin;
		File file = new File("plugins/FancyCrafting/config.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		this.template = new ItemStack[45];
		this.name = fc.getString("recipe-create-gui.title");
		ItemStack background = MiscUtils.get(fc, "background");
		for(int i = 0; i<45; i++) template[i] = background.clone();
		int cnt = 0;
		for(String slot : fc.getStringList("workbench.crafting-slots")) {
			craftingSlots[cnt] = Integer.valueOf(slot);
			cnt++;
		}
		for(int i : craftingSlots) template[i] = null;
		resultSlot = fc.getInt("recipe-create-gui.result-slot");
		template[resultSlot] = null;
		fc.save(file);
	}
	
	public void onClick(InventoryClickEvent event) {
		if(event.getInventory().equals(event.getClickedInventory())) {
			if(!isCraftingSlot(event.getSlot())) event.setCancelled(true);
		}
	}
	
	private boolean isCraftingSlot(int a) {
		for(int i : craftingSlots) if(i == a) return true;
		if(a == resultSlot) return true;
		return false;
	}
	
	public void close(HumanEntity owner, boolean event) {
		if(!event) owner.closeInventory();
		ItemStack result = openGUIs.get(owner.getUniqueId()).getInventory().getItem(resultSlot);
		if(result == null) {
			owner.sendMessage("§cNo result in recipe!");
			onClose(owner, openGUIs.get(owner.getUniqueId()).getInventory());
		}
		openGUIs.remove(owner.getUniqueId());
	}
	
	public boolean hasInventoryOpen(HumanEntity owner) {
		return plugin.getRecipeCreateGUI().openGUIs.containsKey(owner.getUniqueId());
	}
	
	public void open(HumanEntity owner) {
		openGUIs.put(owner.getUniqueId(), new RecipeCreateGUI(owner, plugin));
	}
	
	public void onClose(HumanEntity owner, Inventory inventory) {
    	for(int i : craftingSlots) {
    		ItemStack is = inventory.getItem(i);
    		if(is == null) continue;
    		if(owner.getInventory().firstEmpty() != -1) {
    			owner.getInventory().addItem(is);
    		} else {
    			owner.getWorld().dropItem(owner.getLocation(), is);
    		}
    	}
    }
	
	private RecipeCreateGUI(HumanEntity owner, FancyCrafting plugin) {
		super(owner, plugin.getRecipeCreateGUI().template, plugin.getRecipeCreateGUI().name, 45);
		this.plugin = plugin;
		
	}	
}
