package de.ancash.fancycrafting.gui;

public class WorkspaceSlotsBuilder {

	private int resultSlot, closeSlot, backSlot, prevSlot, nextSlot, editSlot, saveSlot, deleteSlot, recipeTypeSlot;
	private int[] craftingSlots, craftStateSlots, autoCraftingSlots;

	public WorkspaceSlots build() {
		return new WorkspaceSlots(resultSlot, closeSlot, backSlot, prevSlot, nextSlot, editSlot, saveSlot, deleteSlot,
				recipeTypeSlot, craftingSlots, craftStateSlots, autoCraftingSlots);
	}

	public WorkspaceSlotsBuilder setResultSlot(int resultSlot) {
		this.resultSlot = resultSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setCloseSlot(int closeSlot) {
		this.closeSlot = closeSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setBackSlot(int backSlot) {
		this.backSlot = backSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setPrevSlot(int prevSlot) {
		this.prevSlot = prevSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setNextSlot(int nextSlot) {
		this.nextSlot = nextSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setEditSlot(int editSlot) {
		this.editSlot = editSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setSaveSlot(int saveSlot) {
		this.saveSlot = saveSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setDeleteSlot(int deleteSlot) {
		this.deleteSlot = deleteSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setRecipeTypeSlot(int recipeTypeSlot) {
		this.recipeTypeSlot = recipeTypeSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setCraftingSlots(int... craftingSlots) {
		this.craftingSlots = craftingSlots;
		return this;
	}

	public WorkspaceSlotsBuilder setCraftStateSlots(int... craftStateSlots) {
		this.craftStateSlots = craftStateSlots;
		return this;
	}

	public WorkspaceSlotsBuilder setAutoCraftingSlots(int... autoCraftingSlots) {
		this.autoCraftingSlots = autoCraftingSlots;
		return this;
	}
}