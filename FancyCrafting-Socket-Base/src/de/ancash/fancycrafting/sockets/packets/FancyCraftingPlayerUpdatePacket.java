package de.ancash.fancycrafting.sockets.packets;

import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class FancyCraftingPlayerUpdatePacket extends FancyCraftingPacket {

	private static final long serialVersionUID = -6819530834740176740L;

	private final String name;
	private final UUID uuid;
	private final UUID serverId;

	public FancyCraftingPlayerUpdatePacket(UUID uuid, String name, UUID serverId) {
		this.name = name;
		this.uuid = uuid;
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public UUID getServerId() {
		return serverId;
	}

	@Override
	public Packet toPacket() {
		Packet packet = new Packet(HEADER);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		packet.setAwaitResponse(false);
		return packet;
	}
}