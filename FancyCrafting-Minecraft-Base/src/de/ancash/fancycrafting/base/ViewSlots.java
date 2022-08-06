package de.ancash.fancycrafting.base;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ViewSlots {

	private final int size;
	private final int resultSlot;
	private final int probabilitySlot;
	private final int ingredientsSlot;
	private final int closeSlot;
	private final int backSlot;
	private final int prevSlot;
	private final int nextSlot;
	private final int editSlot;

	public ViewSlots(int size, int resultSlot, int ingredientsSlot, int probabilitySlot, int closeSlot, int backSlot, int prevSlot, int nextSlot, int editSlot) {
		this.size = size;
		this.resultSlot = resultSlot;
		this.ingredientsSlot = ingredientsSlot;
		this.probabilitySlot = probabilitySlot;
		this.closeSlot = closeSlot;
		this.backSlot = backSlot;
		this.prevSlot = prevSlot;
		this.nextSlot = nextSlot;
		this.editSlot = editSlot;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Field f : getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				Object o = f.get(this);
				if (o != null && o.getClass().isArray())
					builder.append(f.getName() + ": " + IntStream.of((int[]) o).boxed().collect(Collectors.toList())) //$NON-NLS-1$
							.append("\n"); //$NON-NLS-1$
				else
					builder.append(f.getName() + ": " + (o == null ? "null" : o)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return builder.toString().substring(0, builder.toString().length() - 1);
	}

	public int getResultSlot() {
		return resultSlot;
	}

	public int getCloseSlot() {
		return closeSlot;
	}

	public int getBackSlot() {
		return backSlot;
	}

	public int getPrevSlot() {
		return prevSlot;
	}

	public int getNextSlot() {
		return nextSlot;
	}

	public int getEditSlot() {
		return editSlot;
	}

	public int getProbabilitySlot() {
		return probabilitySlot;
	}

	public int getSize() {
		return size;
	}

	public int getIngredientsSlot() {
		return ingredientsSlot;
	}

}
