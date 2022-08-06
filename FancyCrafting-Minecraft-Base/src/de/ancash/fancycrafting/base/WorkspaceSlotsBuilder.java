package de.ancash.fancycrafting.base;

public class WorkspaceSlotsBuilder {

	private int resultSlot, closeSlot;
	private int[] craftingSlots, craftStateSlots, autoCraftingSlots;
	private boolean enableQuickCrafting;

	public WorkspaceSlots build() {
		return new WorkspaceSlots(resultSlot, closeSlot, craftingSlots, craftStateSlots, autoCraftingSlots, enableQuickCrafting);
	}

	public WorkspaceSlotsBuilder setEnableQuickCrafting(boolean b) {
		this.enableQuickCrafting = b;
		return this;
	}
	
	public WorkspaceSlotsBuilder setResultSlot(int resultSlot) {
		this.resultSlot = resultSlot;
		return this;
	}

	public WorkspaceSlotsBuilder setCloseSlot(int closeSlot) {
		this.closeSlot = closeSlot;
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