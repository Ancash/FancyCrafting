package de.ancash.fancycrafting.sockets.packets;

import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class FancyCraftingServerConnectPacket extends FancyCraftingPacket {

	private static final long serialVersionUID = 745815847038186177L;

	private final String name;
	private final UUID id;

	public FancyCraftingServerConnectPacket(UUID id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public UUID getId() {
		return id;
	}

	@Override
	public Packet toPacket() {
		Packet packet = new Packet(HEADER);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		packet.setAwaitResponse(true);
		return packet;
	}
}