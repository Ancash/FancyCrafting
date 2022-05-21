package de.ancash.fancycrafting.recipe;


import java.lang.reflect.Array;
import java.util.Arrays;

public class IMatrix<E> implements Cloneable{
	
	private final Class<E> clazz;
	private E[] array;
	private int width;
	private int height;
	private int leftMoves = 0;
	private int upMoves = 0;
	
	@SuppressWarnings("unchecked")
	public IMatrix(E[] array, int width, int height) {
		this.clazz = (Class<E>) array.getClass().getComponentType();
		this.array = array;
		this.width = width;
		this.height = height;
	}
	
	@Override
	protected IMatrix<E> clone() {
		IMatrix<E> clone = new IMatrix<>(array, width, height);
		clone.leftMoves = leftMoves;
		clone.upMoves = upMoves;
		return clone;
	}
	
	public boolean cut(int newWidth, int newHeight) {
		optimize();
		if(height > newHeight || width > newWidth) return false;
		E[] newArray = newArray(newHeight * newWidth);
		for(int a = 0; a<height; a++)
			for(int b = 0; b<width; b++)
				newArray[a * newWidth + b] = array[a * width + b];
		this.array = newArray;
		this.leftMoves = newWidth - width;
		this.upMoves = newHeight - height;
		this.height = newHeight;
		this.width = newWidth;
		return true;
	}
	
	public void print() {
		for(int a = 0; a<height; a++) {
			for(int b = 0; b<width; b++)
				System.out.print(array[a * width + b] == null ? "0 " : "1 ");
			System.out.println();
		}
	}
	
	public void optimize() {
		while(moveLeft()) {}	
		while(moveUp()) {} 
		while(cutRight()) {}
		while(cutDown()) {}
	}
	
	public boolean cutRight() {
		if(width == 0 || !canMoveRight()) 
			return false;
		E[] temp = newArray(height * (width - 1));
		for(int a = 0; a<height; a++)
			for(int b = 0; b<width - 1; b++)
				temp[a * (width - 1) + b] = array[a * width + b];
		array = temp;
		width--;
		return true;
	}
	
	public boolean cutDown() {
		if(height == 0 || !canMoveDown())
			return false;
		E[] temp = newArray(width * (height - 1));
		for(int a = 0; a<height - 1; a++)
			for(int b = 0; b<width; b++)
				temp[a * width + b] = array[a * width + b];
		array = temp;
		height--;
		return true;
	}
	
	public boolean canMoveDown() {
		for(int i = 0; i<width; i++)
			if(array[width * height - 1 - i] != null)
				return false;
		return true;
	}
	
	public boolean moveUp() {
		if(!canMoveUp() || height == 1) 
			return false; 
		array = Arrays.copyOfRange(array, width, array.length);
		height--;
		upMoves++;
		return true;
	}
	
	public boolean moveLeft() {
		if(!canMoveLeft() || width == 1) 
			return false;
		E[] temp = newArray(array.length - height);
		for(int a = 0; a<height; a++)
			for(int b = 1; b<width; b++)
				temp[a * (width - 1) + b - 1] = array[a * width + b];
		this.array = temp;
		width--;
		leftMoves++;
		return true;
	}
	
	public boolean canMoveRight() {
		for(int i = 1; i<=height; i++)
			if(array[i * width - 1] != null)
				return false;
		return true;
	}
	
	public boolean canMoveUp() {
		return canMoveUpNTimes(1);
	}
	
	public boolean canMoveUpNTimes(int n) {
		for(int a = 0; a<n; a++)
			for(int i = 0; i<width; i++)
				if(array[i + a * width] != null) 
					return false;
		return true;
	}
	
	public boolean canMoveLeft() {
		return canMoveLeftNTimes(1);
	}
	
	public boolean canMoveLeftNTimes(int n) {
		for(int a = 0; a<n; a++)
			for(int i = 0; i<height; i++)
				if(array[i * width + a] != null)
					return false;
		return true;
	}
	
	public E[] getArray() {
		return array;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	@SuppressWarnings("unchecked")
	private E[] newArray(int cap) {
		return (E[]) Array.newInstance(clazz, cap);
	}

	public int getLeftMoves() {
		return leftMoves;
	}

	public int getUpMoves() {
		return upMoves;
	}
}