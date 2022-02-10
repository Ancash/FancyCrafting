package de.ancash.fancycrafting.recipe;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

public final class IMatrix {
	
	public static ItemStack[] cutMatrix(ItemStack[] ings, int max) {
		if(!checkPerfectSquare(ings.length))
			return ings;
		if(ings.length == max * max)
			return ings;
		if(ings.length > max * max)
			return cutDown(ings, max);
		return cutUp(ings, max);
	}
	
	private static ItemStack[] cutUp(ItemStack[] ings, int max) {
		int omx = (int) Math.sqrt(ings.length);
		int nmx = omx + 1;
		ItemStack[] i = ings;
		while(i.length != max * max) {
			ItemStack[] temp = new ItemStack[nmx * nmx];
			for(int col = 0; col<omx; col++) 
				for(int row = 0; row<omx; row++)
					temp[col * nmx + row]  = i[col * omx + row];
			i = temp;
			omx = (int) Math.sqrt(i.length);
			nmx = omx + 1;
		}
		return i;
	}
	
	private static ItemStack[] cutDown(ItemStack[] ings, int max) {
		int size;
		while(true) {
			size = (int) Math.sqrt(ings.length);
			if(size <= 1 || size - 1 < max) return ings;
			int fr = 0;
			int fd = 0;
			for(int i = 0; i<ings.length; i++) {
				if(ings[i] != null &&  i % size == size - 1)
					return ings;
				if(ings[i] != null &&  i >= size * size - size)
					return ings;
			}
			if(fr == size -1 || fd == size - 1) 
				return ings;
			ItemStack[] temp = new ItemStack[(size - 1) * (size - 1)];
			for(int h = 0; h<size - 1; h++)
				for(int w = 0; w<size - 1; w++)
					temp[h * size + w - h] = ings[h * size + w];
			ings = temp;
		}
	}
	
	public static int[] optimize(ItemStack[] ings) {
		int[] moves = new int[] {0, 0};
		if(!checkPerfectSquare(ings.length))
			return moves;
		if(!Arrays.asList(ings).stream().filter(i -> i != null).findAny().isPresent()) return moves;
		int size = (int) Math.sqrt(ings.length);
		while(canMoveToLeft(ings, size)) {
			moveToLeft(ings, size);
			moves[0]++;
		}
		while(canMoveUp(ings, size)) {
			moveUp(ings, size);
			moves[1]++;
		}
		return moves;
	}
	
	private static void moveUp(ItemStack[] ings, int size) {
		for(int i = size; i < size * size; i++) {
			ings[i - size] = ings[i];
			ings[i] = null;
		}
	}
	
	private static void moveToLeft(ItemStack[] ings, int size) {
		for(int h = 0; h<size; h++)
			for(int w = 1; w<size; w++) {
				ings[h * size + w - 1] = ings[h * size + w];
				ings[h * size + w] = null;
			}
	}
	
	private static boolean canMoveUp(ItemStack[] ings, int size) {
		for(int i = 0; i<size; i++)
			if(ings[i] != null) return false;
		return true;
	}
	
	private static boolean canMoveToLeft(ItemStack[] ings, int size) {
		for(int i = 0; i<size; i++)
			if(ings[i * size] != null) return false;
		return true;
	}
	
	public static boolean checkPerfectSquare(double x) { 
		double sq = Math.sqrt(x); 
		return ((sq - Math.floor(sq)) == 0); 
    } 
}