package de.ancash.fancycrafting.autocrafter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.lambda.Lambda;
import de.ancash.nbtnexus.InventoryUtils;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public class AutoCrafter implements Listener {

	protected final UUID player;
	protected final IRecipeComputer recipeComputer;
	protected final FancyCrafting plugin;
	protected final AtomicBoolean computing = new AtomicBoolean(false);
	protected final AtomicInteger contending = new AtomicInteger(0);

	public AutoCrafter(FancyCrafting plugin, UUID player, IRecipeComputer recipeComputer) {
		this.player = player;
		this.plugin = plugin;
		this.recipeComputer = recipeComputer;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onItemPickup(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		if (!((Player) event.getEntity()).getUniqueId().equals(player))
			return;

		if (event.isCancelled())
			return;

		Bukkit.getScheduler().runTaskLater(plugin, () -> Lambda.execIf(!event.isCancelled(), this::check), 1);
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;

		if (!((Player) event.getWhoClicked()).getUniqueId().equals(player))
			return;

		if (event.getClickedInventory() == null)
			return;

		if (event.isCancelled())
			return;

		Bukkit.getScheduler().runTaskLater(plugin, () -> Lambda.execIf(!event.isCancelled(), this::check), 1);
	}

	public void check() {
		if (contending.incrementAndGet() > 1) {
			contending.decrementAndGet();
			return;
		}
		if (!computing.compareAndSet(false, true)) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				contending.decrementAndGet();
				check();
			}, 1);
			return;
		}

		if (recipeComputer.isAsync())
			plugin.submit(this::check0);
		else
			Bukkit.getScheduler().runTask(plugin, this::check0);
	}

	@SuppressWarnings("nls")
	private void check0() {
		contending.decrementAndGet();
		try {
			List<IRecipe> recipes = check1();
			Bukkit.getScheduler().runTaskLater(plugin, () -> craft(recipes), 1);
		} catch (Throwable th) {
			plugin.getLogger().severe("Error while checking recipes: " + th.getMessage());
			th.printStackTrace();
		} finally {
			Bukkit.getScheduler().runTaskLater(plugin, () -> computing.set(false), 20);
		}
	}

	@SuppressWarnings("nls")
	private void craft(List<IRecipe> recipes) {
		Map<SerializedItem, Integer> content = mapContent();
		Iterator<IRecipe> iter = recipes.iterator();
		while (iter.hasNext()) {
			IRecipe recipe = iter.next();

			if (!canCraft(content, recipe)) {
				iter.remove();
				continue;
			}

			if (InventoryUtils.getFreeSpaceExact(Bukkit.getPlayer(player),
					recipe.getResultAsSerializedItem()) < SerializedMetaAccess.UNSPECIFIC_META_ACCESS
							.getAmount(recipe.getResultAsSerializedItem().getMap())) {
				continue;
			}
			for (Entry<SerializedItem, Integer> entry : recipe.mapIngredients().entrySet()) {
				content.put(entry.getKey(), content.get(entry.getKey()) - entry.getValue());
				int removed = InventoryUtils.removeItemStack(Bukkit.getPlayer(player), entry.getKey().toItem(),
						entry.getValue());
				if (removed != 0)
					throw new IllegalStateException("tried to remove " + entry.getValue() + " but only removed "
							+ removed + " of " + entry.getKey().getMap());
			}
			InventoryUtils.addItemStack(Bukkit.getPlayer(player), recipe.getResultAsSerializedItem(),
					SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(recipe.getResultAsSerializedItem().getMap()));
			iter = recipes.iterator();
		}
	}

	private List<IRecipe> check1() {
		Map<SerializedItem, Integer> content = mapContent();

		List<IRecipe> crafting = new ArrayList<>();

		if (content.isEmpty())
			return crafting;

		Iterator<IRecipe> recipeIter = recipeComputer.computeRecipes().iterator();
		while (recipeIter.hasNext()) {
			IRecipe r = recipeIter.next();

			if (!canCraft(content, r))
				continue;

			for (Entry<SerializedItem, Integer> entry : r.mapIngredients().entrySet())
				content.put(entry.getKey(), content.get(entry.getKey()) - entry.getValue());

			crafting.add(r);
		}
		return crafting;
	}

	public PlayerInventory getInventory() {
		return Bukkit.getPlayer(player).getInventory();
	}

	public Map<SerializedItem, Integer> mapContent() {
		Map<SerializedItem, Integer> content = new HashMap<>();
		PlayerInventory inv = getInventory();
		for (int i = 0; i < inv.getContents().length; i++) {
			if (inv.getContents()[i] == null || inv.getContents()[i].getType() == Material.AIR)
				continue;
			SerializedItem ii = SerializedItem.of(inv.getContents()[i]);
			content.computeIfAbsent(ii, k -> 0);
			content.put(ii, content.get(ii) + SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(ii.getMap()));
		}
		return content;
	}

	public boolean canCraft(Map<SerializedItem, Integer> content, IRecipe recipe) {
		Map<SerializedItem, Integer> immutableIngs = Collections.unmodifiableMap(
				recipe.getSerializedIngredients().stream().filter(Lambda.notNull()).collect(Collectors.toMap(i -> i,
						i -> SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(i.getMap()), (a, b) -> a + b)));
		for (Entry<SerializedItem, Integer> entry : immutableIngs.entrySet())
			if (!content.containsKey(entry.getKey()) || content.get(entry.getKey()) < entry.getValue())
				return false;
		return true;
	}
}
