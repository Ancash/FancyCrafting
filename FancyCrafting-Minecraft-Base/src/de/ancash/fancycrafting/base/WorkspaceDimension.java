package de.ancash.fancycrafting.base;

public class WorkspaceDimension {

	private final int width, height, size;

	public WorkspaceDimension(int width, int height) {
		this(width, height, -1);
	}

	public WorkspaceDimension(int width, int height, int size) {
		this.width = width;
		this.height = height;
		this.size = size;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getSize() {
		return size;
	}
}