package de.ancash.fancycrafting.sockets.packets;

import java.io.Serializable;

import de.ancash.sockets.packet.Packet;

public abstract class FancyCraftingPacket implements Serializable{

	private static final long serialVersionUID = 4423064563183441017L;

	protected static final short HEADER = (short) 6000;
	
	public abstract Packet toPacket();
}