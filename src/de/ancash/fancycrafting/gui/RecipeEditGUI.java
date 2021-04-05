package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.fancycrafting.utils.MiscUtils;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;

public class RecipeEditGUI extends IGUI{

	private FancyCrafting plugin;
	private CompactMap<UUID, RecipeEditGUI> openGUIs = new CompactMap<>();
	
	private int size = 45;
	private String name = "Edit Recipe";
	private ItemStack[] editTemplate = new ItemStack[45];
	private Integer[] craftingSlots = new Integer[9];
	private int saveSlot = 34;
	private int deleteSlot = 25;
	private int shapedSlot = 16;
	private int resultSlot = 23;
	
	private RecipeEditPages recipeEditPages;
	
	private static final ItemStack shapeless = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
	private static final ItemStack shaped = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
	private static final ItemStack save = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
	private static final ItemStack delete = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
	
	static {
		ItemMeta im = shapeless.getItemMeta();
		im.setDisplayName("§aShapeless Recipe");
		shapeless.setItemMeta(im);
		im = shaped.getItemMeta();
		im.setDisplayName("§aShaped Recipe");
		shaped.setItemMeta(im);
		im = save.getItemMeta();
		im.setDisplayName("§aSave Recipe");
		save.setItemMeta(im);
		im = delete.getItemMeta();
		im.setDisplayName("§cDelete Recipe");
		delete.setItemMeta(im);
	}
	
	public RecipeEditGUI(FancyCrafting plugin) throws FileNotFoundException, IOException, InvalidConfigurationException {
		super(null, null, null, -1);
		this.plugin = plugin;
		File file = new File("plugins/FancyCrafting/config.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		int cnt = 0;
		for(String slot : fc.getStringList("workbench.crafting-slots")) {
			craftingSlots[cnt] = Integer.valueOf(slot);
			cnt++;
		}
		ItemStack background = MiscUtils.get(fc, "background");
		for(int i = 0; i<45; i++) editTemplate[i] = background;
		for(int i : craftingSlots) editTemplate[i] = null;
		editTemplate[resultSlot] = null;
		editTemplate[saveSlot] = save;
		editTemplate[deleteSlot] = delete;
		editTemplate[shapedSlot]= null;
		fc.save(file);
	}
	
	
	
	private RecipeEditGUI(FancyCrafting plugin, HumanEntity owner, RecipeEditPages pages, String name, int size) {
		super(owner, pages.getFirstPage(), name, size);
		this.recipeEditPages = pages;
		this.plugin = plugin;
	}
	
	public void open(HumanEntity owner) {
		openGUIs.put(owner.getUniqueId(), new RecipeEditGUI(plugin, owner, new RecipeEditPages(plugin.getRecipeManager().getCustomRecipes().toArray(new IRecipe[plugin.getRecipeManager().getCustomRecipes().size()]), size),name, size));
	}
	
	public boolean hasInventoryOpen(HumanEntity owner) {
		return openGUIs.containsKey(owner.getUniqueId());
	}
	
	@SuppressWarnings("deprecation")
	public void onClick(InventoryClickEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException, de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException {
		if(event.getClickedInventory() == null) {
			event.setCancelled(true);
			return;
		}
		if(event.getClickedInventory().equals(event.getInventory())) {
			int slot = event.getSlot();
			ItemStack clicked = event.getInventory().getItem(slot);
			if(clicked != null) {
				if(event.getClickedInventory().getItem(5).getType().equals(Material.ARROW)) {
					if(slot < 9 && clicked.getType().equals(Material.ARROW)) {
						event.setCancelled(true);
						if(slot == 3) {
							RecipeEditGUI edit = openGUIs.get(event.getWhoClicked().getUniqueId());
							edit.setContent(edit.recipeEditPages.getPreviousPage());
							return;
						}
						if(slot == 5) {
							RecipeEditGUI edit = openGUIs.get(event.getWhoClicked().getUniqueId());
							edit.setContent(edit.recipeEditPages.getNextPage());
							return;
						}
					}
					NBTItem nbt = new NBTItem(clicked);
					if(nbt.hasKey("fancycrafting.id")) {
						event.setCancelled(true);
						edit(event.getWhoClicked(), nbt.getString("fancycrafting.id").replace(" ", "-"));
						return;
					}
				}
			}
			if(event.getInventory().getItem(shapedSlot) == null || 
					(event.getInventory().getItem(shapedSlot).getData().getData() != 5 &&
							event.getInventory().getItem(shapedSlot).getData().getData() != 14)) {
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			for(int i : craftingSlots) {
				if(i == slot) {
					event.setCancelled(false); 
					break;
				}
			}
			if(slot == resultSlot) event.setCancelled(false);
			boolean isShaped = event.getInventory().getItem(shapedSlot).getData().getData() == 5;
			if(slot == saveSlot || slot == deleteSlot) event.setCancelled(true);
			if(slot == saveSlot) {
				ItemStack result = event.getInventory().getItem(resultSlot);
				CompactMap<Integer, ItemStack> ingredients = plugin.getWorkbenchGUI().getIngredientsFromInventory(event.getInventory(), craftingSlots);
				if(result == null || ingredients.size() == 0) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				plugin.getRecipeManager().updateRecipe(result, ingredients, isShaped, new NBTItem(event.getInventory().getItem(saveSlot)).getString("fancycrafting.editid"));
				event.getWhoClicked().sendMessage("§aThe recipe has been saved!");
				close(event.getWhoClicked(), false);
				return;
			}
			if(slot == deleteSlot) {
				plugin.getRecipeManager().delete(new NBTItem(event.getInventory().getItem(saveSlot)).getString("fancycrafting.editid"));
				close(event.getWhoClicked(), false);
				event.getWhoClicked().sendMessage("§aThe recipe has been deleted!");
				return;
			}
			if(slot == shapedSlot) {
				if(isShaped) {
					event.getInventory().setItem(shapedSlot, shapeless);
				} else {
					event.getInventory().setItem(shapedSlot, shaped);
				}
				return;
			}
		}
	}
	
	private void edit(HumanEntity owner, String recipeId) throws FileNotFoundException, IOException, InvalidConfigurationException {
		File file = new File("plugins/FancyCrafting/recipes.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		IRecipe recipe = plugin.getRecipeManager().getRecipeFromFile(fc, recipeId);
		fc.save(file);
		if(recipe == null) {
			System.err.println("A player is trying to edit a non-existing recipe! Id: " + recipeId);
			return;
		}
		ItemStack[] template = editTemplate.clone();
		template[resultSlot] = recipe.getResultWithId();
		NBTItem temp = new NBTItem(template[saveSlot]);
		temp.setString("fancycrafting.editid", recipeId);
		template[saveSlot] = temp.getItem();
		if(recipe instanceof IShapedRecipe) {
			IShapedRecipe shapedRecipe = (IShapedRecipe) recipe;
			for(int slot : shapedRecipe.getIngredientsMap().keySet()) {
				template[craftingSlots[slot - 1]] = shapedRecipe.getIngredientsMap().get(slot);
			}
			template[shapedSlot] = shaped;
			owner.getOpenInventory().getTopInventory().setContents(template);
			return;
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapelessRecipe = (IShapelessRecipe) recipe;
			int row = 1;
			int slot = 1;
			ItemStack[] ingredients = shapelessRecipe.getIngredientsList().toArray(new ItemStack[shapelessRecipe.getIngredientsSize()]);
			for(int i = 0; i<ingredients.length; i++) {
				template[row * 9 + slot] = ingredients[i];
				slot++;
				if(slot > 3) {
					slot = 1;
					row++;
				}
			}
			template[shapedSlot] = shapeless;
			owner.getOpenInventory().getTopInventory().setContents(template);
			return;
		}
	}
	
	public void close(HumanEntity owner, boolean event) throws FileNotFoundException, IOException, InvalidConfigurationException {
		if(!event) owner.closeInventory();
		openGUIs.remove(owner.getUniqueId());
	}
	
	public void onDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}
