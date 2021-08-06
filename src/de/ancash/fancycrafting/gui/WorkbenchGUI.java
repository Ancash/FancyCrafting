package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.SerializableItemStack;
import de.ancash.minecraft.XMaterial;

public class WorkbenchGUI{

	private final FancyCrafting plugin;
	private final ItemStack[] template;
	private final Integer[] craftingSlots = new Integer[9];
	private final List<Integer> craftStateSlots = new ArrayList<Integer>();
	
	private final int size;
	private final int resultSlot;
	private final int closeSlot;
	private final String name;
	
	private final Map<UUID, Duplet<IGUI, IRecipe>> openGuis = new HashMap<>();
	private final ItemStack red = XMaterial.RED_STAINED_GLASS_PANE.parseItem().clone();
	private final ItemStack green = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem().clone();
	private final boolean permsUsingRecipes;
	
	public WorkbenchGUI(FancyCrafting plugin) throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		this.plugin = plugin;
		File file = new File("plugins/FancyCrafting/config.yml");
		FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		ItemStack background = null;
		ItemStack close = null;
		configuration.load(file);
		background = ItemStackUtils.get(configuration, "background");
		size = configuration.getInt("workbench.size");
		int cnt = 0;
		for(String slot : configuration.getStringList("workbench.crafting-slots")) {
			craftingSlots[cnt] = Integer.valueOf(slot);
			cnt++;
		}
		for(String slot : configuration.getStringList("workbench.craft-state-slots")) 
			craftStateSlots.add(Integer.valueOf(slot));
		
		resultSlot = configuration.getInt("workbench.result-slot");
		closeSlot = configuration.getInt("workbench.close-slot");
		name = configuration.getString("workbench.title");
		close = ItemStackUtils.get(configuration, "close");
		permsUsingRecipes = configuration.getBoolean("perms-using-recipe");
		configuration.save(file);
		
		template = new ItemStack[size];
		for(int i = 0; i<size; i++) template[i] = background.clone();
		for(int i : craftingSlots) template[i] = null;
		craftStateSlots.forEach(slot -> template[slot] = red);
		template[closeSlot] = close;
		template[resultSlot] = red;
	}
	
	public void open(Player owner) {
		openGuis.put(owner.getUniqueId(), Tuple.of(new IGUI(owner, template, name, size), null));
	}
	
	public void onWorkbenchDrag(InventoryDragEvent event) {
		if(event.getInventorySlots().contains(resultSlot)) {
			event.setCancelled(true);
			return;
		}
		checkRecipe(event.getInventory(), event.getWhoClicked());
	}
	
	private void checkRecipe(final Inventory inventory, HumanEntity owner) {
		if(!openGuis.containsKey(owner.getUniqueId())) return;
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Map<Integer, SerializableItemStack> ingredients = getIngredientsFromInventory(inventory, craftingSlots);
				if(ingredients.isEmpty()) {
					inventory.setItem(resultSlot, red);
					craftStateSlots.forEach(slot -> inventory.setItem(slot, red));
					return;
				}
				IRecipe.optimize(ingredients);
				IRecipe recipe = plugin.getRecipeManager().match(ingredients);
				if(recipe == null || (recipe != null && !canCraftRecipe(owner, recipe))) {
					inventory.setItem(resultSlot, red);
					craftStateSlots.forEach(slot -> inventory.setItem(slot, red));
				} else {
					inventory.setItem(resultSlot, recipe.getResult());
					craftStateSlots.forEach(slot -> inventory.setItem(slot, green));
					openGuis.get(owner.getUniqueId()).setSecond(recipe);
				}
			}
		}.runTask(plugin);
	}
	
	private boolean canCraftRecipe(HumanEntity p, IRecipe recipe) {
		if(!permsUsingRecipes) return true;
		return permsUsingRecipes && (p.isOp() || (recipe.getName() != null && p.hasPermission("fancycrafting.craft." + recipe.getName().replace(" ", "-"))));
	}
	
	public Map<Integer, SerializableItemStack> getIngredientsFromInventory(Inventory inventory, Integer[] slots) {
		Map<Integer, SerializableItemStack> ingredientsMap = new HashMap<>();
		for(int i = 0; i<slots.length; i++) 
			if(inventory.getItem(slots[i]) != null)
				ingredientsMap.put(i + 1, new SerializableItemStack(inventory.getItem(slots[i])));
		return ingredientsMap;
	}
	
	public void onWorkbenchClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null) event.setCancelled(true);
		boolean workbenchInv = event.getClickedInventory() != null && event.getClickedInventory().equals(event.getInventory());
		if(event.getClickedInventory() != null && event.getClickedInventory().equals(event.getWhoClicked().getInventory())) event.setCancelled(false);
		
		int slot = event.getSlot();
		if(event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
			event.setCancelled(true);
			return;
		}
		
		if(!workbenchInv && event.isShiftClick()) {
			if(event.getView().getBottomInventory().getItem(event.getSlot()) == null) return;
			ItemStack is2 = event.getView().getBottomInventory().getItem(event.getSlot()).clone();
			ItemStack is1 = event.getInventory().getItem(resultSlot).clone();
			if(is2 != null && is1 != null) {
				if(SerializableItemStack.areSimilar(new SerializableItemStack(is1), new SerializableItemStack(is2))) event.setCancelled(true);
			}
		}
		
		if(workbenchInv && isCraftingSlot(slot)) {
			event.setCancelled(false);
		} else if(workbenchInv && slot == closeSlot && !event.isShiftClick()) {
			close(event.getWhoClicked(), false);
		} else if(workbenchInv && slot == resultSlot) {
			event.setCancelled(true);
			if(event.getInventory().getItem(resultSlot) != null) {
				Map<Integer, SerializableItemStack> ingredients = getIngredientsFromInventory(event.getInventory(), craftingSlots);
				Duplet<Integer, Integer> moves = IRecipe.optimize(ingredients);
				IRecipe recipe = openGuis.get(event.getWhoClicked().getUniqueId()).getSecond();
				craftItem(event, recipe, moves);
				return;
			}
		} else if(workbenchInv) {
			event.setCancelled(true);
		}
		checkRecipe(event.getInventory(), event.getWhoClicked());
	}
	
	private void craftItem(InventoryClickEvent event, IRecipe recipe, Duplet<Integer, Integer> moves) {
		if(recipe == null) {
			checkRecipe(event.getInventory(), event.getWhoClicked());
			return;
		}
		if(event.getWhoClicked().getInventory().firstEmpty() == -1) {
			event.getWhoClicked().sendMessage("§cYour inventory is full!");
			return;
		}
		if(event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			event.getWhoClicked().getInventory().addItem(recipe.getResult());
			collectIngredients(event.getInventory(), recipe, moves);
		} else if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			int toAdd = shiftCollectIngredients(event.getInventory(), recipe, event.getWhoClicked(), moves);
			ItemStack results = recipe.getResult().clone();
			for(int i = 0; i<toAdd; i++)
				event.getWhoClicked().getInventory().addItem(results);
		}
		openGuis.get(event.getWhoClicked().getUniqueId()).setSecond(null);
		checkRecipe(event.getInventory(), event.getWhoClicked());
	}
	
	private int shiftCollectIngredients(Inventory inventory, IRecipe recipe, HumanEntity owner, Duplet<Integer, Integer> moves) {
		int freeSlots = 0;
		for(int i = 0; i<36; i++) 
			if(owner.getInventory().getItem(i) == null) freeSlots++;
		int shiftSize = freeSlots * recipe.getResult().getMaxStackSize() / recipe.getResult().getAmount();
		if(recipe instanceof IShapedRecipe) {
			Map<Integer, SerializableItemStack> ingredients = getIngredientsFromInventory(inventory, craftingSlots);
			IRecipe.optimize(ingredients);
			for (int i = 1; i < 10; ++i) {
				SerializableItemStack serializedIngredient = ((IShapedRecipe)recipe).getIngredientsMap().get(i);
				SerializableItemStack serializedCompareTo = ingredients.get(i);
				if((serializedCompareTo != null) != (serializedIngredient != null) || serializedIngredient == null) continue;
				if (!SerializableItemStack.areSimilar(serializedIngredient, serializedCompareTo)) continue;
	            shiftSize = Math.min((int)shiftSize, (int)(serializedCompareTo.getAmount() / serializedIngredient.getAmount()));
			}
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			Collection<SerializableItemStack> currentItems = getIngredientsFromInventory(inventory, craftingSlots).values();
			
			for(SerializableItemStack ingredient : shapeless.getIngredients()) {
				for(SerializableItemStack currentItem : currentItems) {
					if(ingredient == null || currentItem == null) continue;
					if(SerializableItemStack.areSimilar(ingredient, currentItem)) 
						shiftSize = Math.min((int)shiftSize, (int)(currentItem.getAmount() / ingredient.getAmount()));
				}
			}
		}
		if(recipe instanceof IShapedRecipe) {
			collectShaped(inventory, (IShapedRecipe) recipe, moves, shiftSize);
		} else if(recipe instanceof IShapelessRecipe) {
			collectShapeless(inventory, (IShapelessRecipe) recipe, moves, shiftSize);
		}
		return shiftSize;
	}
	
	private void setAmount(int a, int b, int slot, Inventory inventory) {
		ItemStack is = inventory.getItem(slot);
		if(a - b == 0) {
			inventory.setItem(slot, null);
		} else {
			is.setAmount(a - b);
		}
	}
	
	private void collectShapeless(Inventory inventory, IShapelessRecipe shapeless, Duplet<Integer, Integer> moves, int multiplicator) {
		Set<Integer> done = new HashSet<>();
		for(SerializableItemStack ingredient : shapeless.getIngredients()) {
			for(int craftSlot : craftingSlots) {
				if(done.contains(craftSlot)) continue;
				if(inventory.getItem(craftSlot) == null) continue;
				SerializableItemStack compareTo = new SerializableItemStack(inventory.getItem(craftSlot));
				if(!SerializableItemStack.areSimilar(ingredient, compareTo)) continue;
				setAmount(compareTo.getAmount(), ingredient.getAmount() * multiplicator, craftSlot, inventory);
				done.add(craftSlot);
			}
		}
	}
	
	private void collectShaped(Inventory inventory, IShapedRecipe shaped, Duplet<Integer, Integer> moves, int multiplicator) {
		for(Entry<Integer, SerializableItemStack> entry : shaped.getIngredientsMap().entrySet()) {
			int slot = entry.getKey() + moves.getFirst() + moves.getSecond() * 3;
			ItemStack a = inventory.getItem(craftingSlots[slot - 1]);
			SerializableItemStack b = entry.getValue();
			setAmount(a.getAmount(), b.getAmount() * multiplicator, craftingSlots[slot - 1], inventory);
		}
	}
	
	private void collectIngredients(Inventory inventory, IRecipe recipe, Duplet<Integer, Integer> moves) {
		if(recipe instanceof IShapedRecipe) 
			collectShaped(inventory, (IShapedRecipe) recipe, moves, 1);
		if(recipe instanceof IShapelessRecipe) 
			collectShapeless(inventory, (IShapelessRecipe) recipe, moves, 1);
	}
	
	private boolean onDisable = false;
	
	public void close(HumanEntity owner, boolean event) {
		if(event && onDisable) return;
		if(openGuis.containsKey(owner.getUniqueId())) {
			onClose(owner, openGuis.get(owner.getUniqueId()).getFirst().getInventory());
			openGuis.remove(owner.getUniqueId());
			owner.closeInventory();
		}
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
	
	public void closeAll() {
		onDisable = true;
		openGuis.values().stream().collect(Collectors.toList()).forEach(gui -> close(gui.getFirst().getOwner(), false));
		onDisable = false;
	}
	
	public Integer[] getCraftingSlots() {
		return craftingSlots;
	}
	
	public boolean hasInventoryOpen(HumanEntity owner) {
		return openGuis.containsKey(owner.getUniqueId());
	}
	
	public boolean isCraftingSlot(int a) {
		for(int i : craftingSlots) if(a == i) return true;
		return false;
	}
}
