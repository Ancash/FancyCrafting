package de.ancash.fancycrafting.gui.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.minecraft.inventory.input.ItemInputIGUI;
import de.ancash.minecraft.inventory.input.ItemInputSlots;
import de.ancash.minecraft.inventory.input.NumberInputGUI;

public class ManageProbabilitiesGUI extends ItemInputIGUI {

	private static final int[] inputSlotsArrays = IntStream.range(0, 54).filter(i -> (i + 1) % 9 != 0).toArray();
	private static final ItemInputSlots inputSlots = new ItemInputSlots(
			IntStream.of(inputSlotsArrays).boxed().collect(Collectors.toSet()));

	private final AbstractFancyCrafting pl;
	private Consumer<Map<ItemStack, Integer>> onComplete;
	private final Map<ItemStack, Integer> probMap;

	public ManageProbabilitiesGUI(AbstractFancyCrafting pl, UUID id, Map<ItemStack, Integer> probMap) {
		super(inputSlots, id, 54, pl.getWorkspaceObjects().getManageProbabilitiesTitle());
		this.pl = pl;
		this.probMap = probMap;
		super.setOnInput(in -> {
			onComplete.accept(probMap);
		});
	}

	@Override
	public void open() {
		int i = 0;
		for (int s : inputSlotsArrays) {
			setItem(null, s);
			removeInventoryItem(s);
		}
		Map<String, String> placeholder = new HashMap<>();
		List<String> lore = new ArrayList<>();
		for (Entry<ItemStack, Integer> entry : probMap.entrySet()) {
			ItemStack item = entry.getKey().clone();
			ItemMeta meta = item.getItemMeta();
			lore.clear();
			if (pl.getWorkspaceObjects().getManageProbabilityHeader() != null)
				lore.addAll(pl.getWorkspaceObjects().getManageProbabilityHeader());
			if (meta.getLore() != null)
				lore.addAll(meta.getLore());
			if (pl.getWorkspaceObjects().getManageProbabilityFooter() != null)
				lore.addAll(pl.getWorkspaceObjects().getManageProbabilityFooter());

			placeholder.put("%probability%", String.valueOf(entry.getValue())); //$NON-NLS-1$
			InventoryItem invItem = new InventoryItem(this,
					ItemStackUtils.setLore(ItemStackUtils.replacePlaceholder(lore, placeholder), item),
					inputSlotsArrays[i],
					(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(self -> handleAnvilInput(a, c)));
			invItem.getAsNBT().setInteger("fc-random-recipe-probability", entry.getValue()); //$NON-NLS-1$
			invItem.getAsNBT().setItemStack("original", entry.getKey()); //$NON-NLS-1$
			addInventoryItem(invItem);
			i++;
		}
		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getCloseItem().getOriginal(), 53,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(ManageProbabilitiesGUI::closeAll)));
		IGUIManager.register(this, getId());
		super.open();
	}

	private void handleAnvilInput(int slot, InventoryAction action) {
		if (action == InventoryAction.PICKUP_ALL) {
			InventoryItem ii = getInventoryItem(slot);
			ItemStack original = ii.getAsNBT().getItemStack("original"); //$NON-NLS-1$
			manageProbability(original, ii.getAsNBT().getInteger("fc-random-recipe-probability")); //$NON-NLS-1$
		} else if (action == InventoryAction.PICKUP_HALF) {
			ItemStack toRemove = getItem(slot);
			if (toRemove == null || toRemove.getType() == Material.AIR)
				return;
			probMap.remove(getInventoryItem(slot).getAsNBT().getItemStack("original")); //$NON-NLS-1$
			open();
		}
	}

	public void onComplete(Consumer<Map<ItemStack, Integer>> onComplete) {
		this.onComplete = onComplete;
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		super.onInventoryClick(event);
		event.setCancelled(true);
		if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getInventory())
				&& probMap.size() < 48) {
			ItemStack original = event.getClickedInventory().getItem(event.getSlot());
			if (original == null || original.getType() == Material.AIR)
				return;
			manageProbability(original, 0);
		}
	}

	private void manageProbability(ItemStack item, int curProb) {
		NumberInputGUI<Integer> numberIn = new NumberInputGUI<Integer>(pl, Bukkit.getPlayer(getId()), Integer.class,
				i -> {
					probMap.put(item.clone(), Math.abs(i));
					Bukkit.getScheduler().runTaskLater(pl, () -> open(), 1);
				});
		numberIn.setLeft(item.clone());
		numberIn.setText(String.valueOf(curProb));
		numberIn.setTitle(pl.getWorkspaceObjects().getManageProbabilitiesTitle());
		IGUIManager.remove(getId());
		numberIn.open();
	}

	@Override
	public void setOnInput(Consumer<ItemStack[]> onInput) {
		throw new UnsupportedOperationException();
	}
}