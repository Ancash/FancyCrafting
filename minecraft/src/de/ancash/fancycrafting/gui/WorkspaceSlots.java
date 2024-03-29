package de.ancash.fancycrafting.gui;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WorkspaceSlots {

	private final int resultSlot;
	private final int closeSlot;
	private final int[] craftingSlots;
	private final int[] craftStateSlots;
	private final int[] autoCraftingSlots;
	private final boolean enableQuickCrafting;

	public WorkspaceSlots(int resultSlot, int closeSlot, int[] craftingSlots, int[] craftStateSlots,
			int[] autoCraftingSlots, boolean enableQuickCrafting) {
		this.resultSlot = resultSlot;
		this.closeSlot = closeSlot;
		this.craftingSlots = craftingSlots;
		this.craftStateSlots = craftStateSlots;
		this.autoCraftingSlots = autoCraftingSlots;
		this.enableQuickCrafting = enableQuickCrafting;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Field f : getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				Object o = f.get(this);
				if (o != null && o.getClass().isArray())
					builder.append(f.getName()).append(": ") //$NON-NLS-1$
							.append(IntStream.of((int[]) o).boxed().collect(Collectors.toList())).append('\n');
				else
					builder.append(f.getName()).append(": ").append(o == null ? "null" : o).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
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

	public int[] getAutoCraftingSlots() {
		return autoCraftingSlots;
	}

	public int[] getCraftingSlots() {
		return craftingSlots;
	}

	public int[] getCraftStateSlots() {
		return craftStateSlots;
	}

	public boolean enableQuickCrafting() {
		return enableQuickCrafting;
	}
}