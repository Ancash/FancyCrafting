package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.SerializableItemStack;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.nbt.NBTItem;

public class RecipeEditGUI extends IGUI{

	private FancyCrafting plugin;
	private Map<UUID, RecipeEditGUI> openGUIs = new HashMap<>();
	
	private int size = 45;
	private String name = "Edit Recipe";
	private ItemStack[] editTemplate = new ItemStack[45];
	private Integer[] craftingSlots = new Integer[9];
	private int saveSlot = 34;
	private int deleteSlot = 25;
	private int shapedSlot = 16;
	private int resultSlot = 23;
	
	private RecipeEditPages recipeEditPages;
	
	private static final ItemStack shapeless = new ItemStack(XMaterial.BLUE_STAINED_GLASS_PANE.parseMaterial(), 1, XMaterial.BLUE_STAINED_GLASS_PANE.getData());
	private static final ItemStack shaped = new ItemStack(XMaterial.LIME_STAINED_GLASS_PANE.parseMaterial(), 1, XMaterial.LIME_STAINED_GLASS_PANE.getData());
	private static final ItemStack save = new ItemStack(XMaterial.LIME_STAINED_GLASS_PANE.parseMaterial(), 1, XMaterial.LIME_STAINED_GLASS_PANE.getData());
	private static final ItemStack delete = new ItemStack(XMaterial.RED_STAINED_GLASS_PANE.parseMaterial(), 1, XMaterial.RED_STAINED_GLASS_PANE.getData());
	
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
		ItemStack background = ItemStackUtils.get(fc, "background");
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
	public void onClick(InventoryClickEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException, org.simpleyaml.exceptions.InvalidConfigurationException, ClassNotFoundException {
		if(event.getClickedInventory() == null) {
			event.setCancelled(true);
			return;
		}
		if(event.getClickedInventory().equals(event.getInventory())) {
			int slot = event.getSlot();
			ItemStack clicked = event.getInventory().getItem(slot);
			if(clicked != null) {
				if(event.getClickedInventory().getItem(5).getType().equals(XMaterial.ARROW.parseMaterial())) {
					if(slot < 9 && clicked.getType().equals(XMaterial.ARROW.parseMaterial())) {
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
				Map<Integer, SerializableItemStack> ingredients = plugin.getWorkbenchGUI().getIngredientsFromInventory(event.getInventory(), craftingSlots);
				if(result == null || ingredients.size() == 0) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				plugin.getRecipeManager().updateRecipe(new SerializableItemStack(result), ingredients, isShaped, new NBTItem(event.getInventory().getItem(saveSlot)).getString("fancycrafting.editid"));
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
	
	private void edit(HumanEntity owner, String recipeId) throws FileNotFoundException, IOException, InvalidConfigurationException, ClassNotFoundException {
		IRecipe recipe = plugin.getRecipeManager().getCustomRecipe(recipeId);
		if(recipe == null) {
			plugin.severe("A player is trying to edit a non-existing recipe! Id: " + recipeId);
			owner.sendMessage("Something went wrong!");
			return;
		}
		ItemStack[] template = editTemplate.clone();
		template[resultSlot] = recipe.getResult();
		NBTItem temp = new NBTItem(template[saveSlot]);
		temp.setString("fancycrafting.editid", recipeId);
		template[saveSlot] = temp.getItem();
		if(recipe instanceof IShapedRecipe) {
			IShapedRecipe shapedRecipe = (IShapedRecipe) recipe;
			for(int slot : shapedRecipe.getIngredientsMap().keySet()) {
				template[craftingSlots[slot - 1]] = shapedRecipe.getIngredientsMap().get(slot).restore();
			}
			template[shapedSlot] = shaped;
			owner.getOpenInventory().getTopInventory().setContents(template);
			return;
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapelessRecipe = (IShapelessRecipe) recipe;
			int row = 1;
			int slot = 1;
			SerializableItemStack[] ingredients = shapelessRecipe.getIngredients().toArray(new SerializableItemStack[shapelessRecipe.getIngredientsSize()]);
			for(int i = 0; i<ingredients.length; i++) {
				template[row * 9 + slot] = ingredients[i].restore();
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
