package de.ancash.fancycrafting.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.fancycrafting.utils.MiscUtils;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class WorkbenchGUI{

	private final FancyCrafting plugin;
	private final ItemStack[] template;
	private final Integer[] craftingSlots = new Integer[9];
	private final List<Integer> craftStateSlots = new ArrayList<Integer>();
	
	private final int size;
	private final int resultSlot;
	private final int closeSlot;
	private final String name;
	
	private final CompactMap<UUID, IGUI> openGuis = new CompactMap<>();
	private final ItemStack red = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
	private final ItemStack green = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
	
	public WorkbenchGUI(FancyCrafting plugin) throws InvalidConfigurationException, IOException {
		this.plugin = plugin;
		YamlFile configuration = new YamlFile(new File("plugins/FancyCrafting/config.yml"));
		ItemStack background = null;
		ItemStack close = null;
		configuration.load();
		background = MiscUtils.get(configuration, "background");
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
		close = MiscUtils.get(configuration, "close");
		configuration.save();
		
		template = new ItemStack[size];
		for(int i = 0; i<size; i++) template[i] = background.clone();
		for(int i : craftingSlots) template[i] = null;
		craftStateSlots.forEach(slot -> template[slot] = red);
		template[closeSlot] = close;
		template[resultSlot] = red;
	}
	
	public void open(Player owner) {
		openGuis.put(owner.getUniqueId(), new IGUI(owner, template, name, size));
	}
	
	public void onWorkbenchDrag(InventoryDragEvent event) {
		if(event.getInventorySlots().contains(resultSlot)) {
			event.setCancelled(true);
			return;
		}
		checkRecipe(event.getInventory());
	}
	
	private void checkRecipe(final Inventory inventory) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				CompactMap<Integer, ItemStack> ingredients = getIngredientsFromInventory(inventory, craftingSlots);
				if(ingredients == null || ingredients.size() == 0) {
					inventory.setItem(resultSlot, red);
					craftStateSlots.forEach(slot -> inventory.setItem(slot, red));
					return;
				}
				IRecipe recipe = plugin.getRecipeManager().match(ingredients);
				if(recipe == null) {
					inventory.setItem(resultSlot, red);
					craftStateSlots.forEach(slot -> inventory.setItem(slot, red));
				} else {
					inventory.setItem(resultSlot, recipe.getResult());
					craftStateSlots.forEach(slot -> inventory.setItem(slot, green));
				}
			}
		}.runTask(plugin);
	}
	
	public CompactMap<Integer, ItemStack> getIngredientsFromInventory(Inventory inventory, Integer[] slots) {
		CompactMap<Integer, ItemStack> ingredientsMap = new CompactMap<>();
		for(int i = 0; i<slots.length; i++) {
			ItemStack is = inventory.getItem(slots[i]);
			if(is != null) ingredientsMap.put(i + 1, inventory.getItem(slots[i]));
		}
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
		if(workbenchInv && isCraftingSlot(slot)) {
			event.setCancelled(false);
		} else if(workbenchInv && slot == closeSlot) {
			close(event.getWhoClicked(), false);
		} else if(workbenchInv && slot == resultSlot) {
			event.setCancelled(true);
			if(event.getInventory().getItem(resultSlot) != null) {
				CompactMap<Integer, ItemStack> ingredients = getIngredientsFromInventory(event.getInventory(), craftingSlots);
				Duplet<Integer, Integer> moves = IRecipe.optimize(ingredients);
				IRecipe recipe = plugin.getRecipeManager().match(ingredients, 
						plugin.getRecipeManager().getByResult(event.getInventory().getItem(resultSlot)));
				
				craftItem(event, recipe, moves);
				return;
			}
		} else if(workbenchInv) {
			event.setCancelled(true);
		}
		checkRecipe(event.getInventory());
	}
	
	@SuppressWarnings("deprecation")
	private void craftItem(InventoryClickEvent event, IRecipe recipe, Duplet<Integer, Integer> moves) {
		if(recipe == null) {
			if(event.getInventory().getItem(45).getData().getData() != 14)
				System.err.println("§cCould not find recipe although there is a result!" + event.getInventory().getItem(resultSlot));
			checkRecipe(event.getInventory());
			return;
		}
		if(event.getWhoClicked().getInventory().firstEmpty() == -1) {
			event.getWhoClicked().sendMessage("§cYour inventory is full!");
			return;
		}
		if(event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			event.getWhoClicked().getInventory().addItem(recipe.getResult());
			collectIngredients(event.getInventory(), recipe, moves);
		}
		if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			int toAdd = shiftCollectIngredients(event.getInventory(), recipe, event.getWhoClicked(), moves);
			ItemStack results = recipe.getResult().clone();
			results.setAmount(toAdd * recipe.getResult().getAmount());
			event.getWhoClicked().getInventory().addItem(results);
		}
		
		checkRecipe(event.getInventory());
	}
	
	private int shiftCollectIngredients(Inventory inventory, IRecipe recipe, HumanEntity owner, Duplet<Integer, Integer> moves) {
		int freeSlots = 0;
		for(int i = 0; i<36; i++) {
			if(owner.getInventory().getItem(i) == null) freeSlots++;
		}
		
		int shiftSize = freeSlots * recipe.getResult().getMaxStackSize() / recipe.getResult().getAmount();
		if(recipe instanceof IShapedRecipe) {
			CompactMap<Integer, ItemStack> ingredients = getIngredientsFromInventory(inventory, craftingSlots);
			IRecipe.optimize(ingredients);
			for (int i = 1; i < 10; ++i) {
	            ItemStack ingredient = ((IShapedRecipe)recipe).getIngredientsMap().get(i);
	            ItemStack currentItem = ingredients.get(i);
	            if (ingredient == null || ingredient.getType() == Material.AIR || currentItem == null || currentItem.getType() == Material.AIR) continue;
	            shiftSize = Math.min((int)shiftSize, (int)(currentItem.getAmount() / ingredient.getAmount()));
			}
		}
		if(recipe instanceof IShapelessRecipe) {
			IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
			Collection<ItemStack> currentItems = getIngredientsFromInventory(inventory, craftingSlots).values();
			for(ItemStack ingredient : shapeless.getIngredientsList()) {
				for(ItemStack currentItem : currentItems) {
					if(ingredient == null || currentItem == null) continue;
					if(IRecipe.isSimilar(currentItem, ingredient, true)) 
						shiftSize = Math.min((int)shiftSize, (int)(currentItem.getAmount() / ingredient.getAmount()));
				}
			}
		}
		int finalShiftSize = shiftSize;		
		
		if(recipe instanceof IShapedRecipe) {
			collectShaped(inventory, (IShapedRecipe) recipe, moves, finalShiftSize);
		} else if(recipe instanceof IShapelessRecipe) {
			collectShapeless(inventory, (IShapelessRecipe) recipe, moves, finalShiftSize);
		}
		
		return finalShiftSize;
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
		for(ItemStack b : shapeless.getIngredientsList()) {
			for(int craftSlot : craftingSlots) {
				if(done.contains(craftSlot)) continue;
				ItemStack a = inventory.getItem(craftSlot);
				if(a == null) continue;
				if(!IRecipe.isSimilar(a, b, true)) continue;
				setAmount(a.getAmount(), b.getAmount() * multiplicator, craftSlot, inventory);
				done.add(craftSlot);
			}
		}
	}
	
	private void collectShaped(Inventory inventory, IShapedRecipe shaped, Duplet<Integer, Integer> moves, int multiplicator) {
		for(int key : shaped.getIngredientsMap().keySet()) {
			int slot = key + moves.getFirst() + moves.getSecond() * 3;
			ItemStack a = inventory.getItem(craftingSlots[slot - 1]);
			ItemStack b = shaped.getIngredientsMap().get(key);
			setAmount(a.getAmount(), b.getAmount() * multiplicator, craftingSlots[slot - 1], inventory);
		}
	}
	
	private void collectIngredients(Inventory inventory, IRecipe recipe, Duplet<Integer, Integer> moves) {
		if(recipe instanceof IShapedRecipe) 
			collectShaped(inventory, (IShapedRecipe) recipe, moves, 1);
		
		if(recipe instanceof IShapelessRecipe) 
			collectShapeless(inventory, (IShapelessRecipe) recipe, moves, 1);
	}
	
	public void close(HumanEntity owner, boolean event) {
		onClose(owner, openGuis.get(owner.getUniqueId()).getInventory());
		if(!event) owner.closeInventory();
		openGuis.remove(owner.getUniqueId());
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
		openGuis.forEach((id, gui) ->{
			close(gui.getOwner(), false);
		});
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
