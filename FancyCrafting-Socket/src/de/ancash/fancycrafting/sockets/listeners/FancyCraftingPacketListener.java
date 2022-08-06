package de.ancash.fancycrafting.sockets.listeners;

import java.io.IOException;

import de.ancash.fancycrafting.sockets.FancyCraftingSocket;
import de.ancash.fancycrafting.sockets.packets.FancyCraftingPacket;
import de.ancash.fancycrafting.sockets.packets.FancyCraftingServerConnectPacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.sockets.async.client.AbstractAsyncClient;
import de.ancash.sockets.events.ServerPacketReceiveEvent;
import de.ancash.sockets.packet.Packet;

public class FancyCraftingPacketListener implements Listener{

	private final FancyCraftingSocket pl;
	
	public FancyCraftingPacketListener(FancyCraftingSocket pl) {
		this.pl = pl;
	}
	
	@EventHandler
	public void onPacket(ServerPacketReceiveEvent event) throws ClassNotFoundException, IOException {
		Packet packet = event.getPacket();
		
		if(packet.getSerializable() instanceof FancyCraftingPacket) {
			FancyCraftingPacket FancyCraftingPacket = (FancyCraftingPacket) packet.getSerializable();
			if(FancyCraftingPacket instanceof FancyCraftingServerConnectPacket ) {
				onServerConnectPacket(event, event.getClient());
			} 
		}
	}	
	
	@SuppressWarnings("nls")
	private void onServerConnectPacket(ServerPacketReceiveEvent event, AbstractAsyncClient key) {
		FancyCraftingServerConnectPacket connect = (FancyCraftingServerConnectPacket) event.getPacket().getSerializable();
		if(!pl.addConnection(key, connect.getId(), connect.getName())) {
			pl.severe(String.format("%s is a already registered server! Refusing...", connect.getId()));
			event.getPacket().setSerializable(false);
		} else {
			event.getPacket().setSerializable(true);
		}
		event.getClient().putWrite(event.getPacket().toBytes());
	}
}