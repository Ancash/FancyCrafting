package de.ancash.fancycrafting.gui;

public class WorkspaceSlots {

	private final int resultSlot;
	private final int closeSlot;
	private final int backSlot;
	private final int prevSlot;
	private final int nextSlot;
	private final int editSlot;
	private final int saveSlot;
	private final int deleteSlot;
	private final int recipeTypeSlot;
	private final int[] craftingSlots;
	private final int[] craftStateSlots;
	private final int[] autoCraftingSlots;

	WorkspaceSlots(int resultSlot, int closeSlot, int backSlot, int prevSlot, int nextSlot, int editSlot, int saveSlot,
			int deleteSlot, int recipeTypeSlot, int[] craftingSlots, int[] craftStateSlots, int[] autoCraftingSlots) {
		this.resultSlot = resultSlot;
		this.closeSlot = closeSlot;
		this.backSlot = backSlot;
		this.prevSlot = prevSlot;
		this.nextSlot = nextSlot;
		this.editSlot = editSlot;
		this.saveSlot = saveSlot;
		this.deleteSlot = deleteSlot;
		this.recipeTypeSlot = recipeTypeSlot;
		this.craftingSlots = craftingSlots;
		this.craftStateSlots = craftStateSlots;
		this.autoCraftingSlots = autoCraftingSlots;
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

	public int getSaveSlot() {
		return saveSlot;
	}

	public int getRecipeTypeSlot() {
		return recipeTypeSlot;
	}

	public int getDeleteSlot() {
		return deleteSlot;
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
}