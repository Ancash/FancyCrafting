package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
	private Integer[] craftingSlots = new Integer[9];
	private int shapedSlot;
	private int saveSlot;
	private static final ItemStack shapeless = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
	private static final ItemStack shaped = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
	private static final ItemStack save = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
	
	static {
		ItemMeta im = shaped .getItemMeta();
		im.setDisplayName("§aShaped Recipe");
		shaped .setItemMeta(im);
		im = shapeless.getItemMeta();
		im.setDisplayName("§bShapeless Recipe");
		shapeless.setItemMeta(im);
		im = save.getItemMeta();
		im.setDisplayName("§aSave Recipe");
		save.setItemMeta(im);
	}
	
	public RecipeCreateGUI(FancyCrafting plugin) throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		super(null, null, null, -1);
		this.plugin = plugin;
		File file = new File("plugins/FancyCrafting/config.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		this.template = new ItemStack[45];
		this.name = fc.getString("recipe-create-gui.title");
		this.shapedSlot = fc.getInt("recipe-create-gui.shaped");
		this.saveSlot = fc.getInt("recipe-create-gui.save");
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
		template[shapedSlot] = shaped;
		template[saveSlot] = save;
		fc.save(file);
	}
	
	private RecipeCreateGUI(HumanEntity owner, FancyCrafting plugin) {
		super(owner, plugin.getRecipeCreateGUI().template, plugin.getRecipeCreateGUI().name, 45);
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	public void onClick(InventoryClickEvent event) throws FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException, InvalidConfigurationException {
		if(event.getInventory().equals(event.getClickedInventory())) {
			int slot = event.getSlot();
			boolean isShaped = event.getInventory().getItem(shapedSlot).getData().getData() == 5;
			if(!isCraftingSlot(slot)) event.setCancelled(true);
			if(slot == shapedSlot) {
				if(!isShaped) {
					event.getInventory().setItem(shapedSlot, shaped );
				} else {
					event.getInventory().setItem(shapedSlot, shapeless);
				}
			}
			
			if(slot == saveSlot) {
				ItemStack result = event.getInventory().getItem(resultSlot);
				if(result == null) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				CompactMap<Integer, ItemStack> ingredients = plugin.getWorkbenchGUI().getIngredientsFromInventory(event.getInventory(), craftingSlots);
				if(ingredients.size() == 0) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				
				plugin.getRecipeManager().createRecipe(result, ingredients, isShaped);
				event.getWhoClicked().sendMessage("§aCreated new recipe!");
				event.getWhoClicked().closeInventory();
			}	
		}
		
	}
	
	private boolean isCraftingSlot(int a) {
		for(int i : craftingSlots) if(i == a) return true;
		if(a == resultSlot) return true;
		return false;
	}
	
	public void close(HumanEntity owner, boolean event) {
		if(!event) owner.closeInventory();
		onClose(owner, openGUIs.get(owner.getUniqueId()).getInventory());
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
    		owner.getInventory().addItem(is);
    	}
    	ItemStack result = inventory.getItem(resultSlot);
    	if(result != null) owner.getInventory().addItem(result);
    }

	public void onDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}	
}
