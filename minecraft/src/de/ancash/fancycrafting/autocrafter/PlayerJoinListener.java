package de.ancash.fancycrafting.autocrafter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.autocrafter.item.NBTRecipeResultReader;

public class PlayerJoinListener implements Listener {

	private final FancyCrafting pl;
	private final Map<UUID, AutoCrafter> map = new HashMap<>();

	public PlayerJoinListener(FancyCrafting pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		map.computeIfAbsent(e.getPlayer().getUniqueId(), k -> new AutoCrafter(pl, k, new NBTRecipeResultReader(pl, k)));
	}

}
