package de.ancash.fancycrafting.sockets.listeners;

import de.ancash.fancycrafting.sockets.FancyCraftingClientConnection;
import de.ancash.fancycrafting.sockets.FancyCraftingSocket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.sockets.events.ClientDisconnectEvent;

public class FancyCraftingConnectionListener implements Listener {

	private final FancyCraftingSocket pl;

	public FancyCraftingConnectionListener(FancyCraftingSocket pl) {
		this.pl = pl;
	}

	@SuppressWarnings("nls")
	@EventHandler
	public void onDisconnect(ClientDisconnectEvent event) {
		FancyCraftingClientConnection discon = pl.getConnectionByClient(event.getClient());
		if (discon == null)
			return;
		pl.removeConnection(event.getClient());
		pl.warn(String.format("%s (%s) disconnected!", discon.getName(), discon.getId()));
	}
}