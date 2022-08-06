package de.ancash.fancycrafting.sockets;

import java.util.UUID;

import de.ancash.sockets.async.client.AbstractAsyncClient;
import de.ancash.sockets.packet.Packet;

public class FancyCraftingClientConnection {

	private final AbstractAsyncClient cl;
	private final UUID serverId;
	private final String name;
	
	FancyCraftingClientConnection(AbstractAsyncClient cl, UUID id, String name) {
		this.cl = cl;
		this.name = name;
		this.serverId = id;
	}
	
	public AbstractAsyncClient getClient() {
		return cl;
	}
	
	public boolean putWrite(Packet packet) {
		return cl.putWrite(packet.toBytes());
	}
	
	public boolean offerWrite(Packet packet) {
		return cl.offerWrite(packet.toBytes());
	}

	public UUID getId() {
		return serverId;
	}

	public String getName() {
		return name;
	}
}