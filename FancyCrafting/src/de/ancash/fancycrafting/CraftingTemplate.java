package de.ancash.fancycrafting;

import java.util.HashMap;
import java.util.Map;

public class CraftingTemplate {

	private static final Map<Integer, Map<Integer, CraftingTemplate>> templates = new HashMap<>();

	public static void add(FancyCrafting pl, CraftingTemplate template, int width, int height) {
		templates.computeIfAbsent(width, k -> new HashMap<>());
		templates.get(width).put(height, template);
	}

	public static CraftingTemplate get(int width, int height) {
		if (!templates.containsKey(width))
			return null;
		if (!templates.get(width).containsKey(height))
			return null;
		return templates.get(width).get(height);
	}

	private final String title;
	private final int width;
	private final int height;
	private final int size;
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

	public CraftingTemplate(String title, int width, int height, int size, int resultSlot, int closeSlot, int backSlot,
			int prevSlot, int nextSlot, int editSlot, int saveSlot, int deleteSlot, int recipeTypeSlot,
			int[] craftingSlots, int[] craftStateSlots, int[] autoCraftingSlots) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.size = size;
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

	public int[] getCraftingSlots() {
		return craftingSlots;
	}

	public int[] getCraftStateSlots() {
		return craftStateSlots;
	}

	public String getTitle() {
		return title;
	}

	public int getSize() {
		return size;
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getAutoCraftingSlots() {
		return autoCraftingSlots;
	}
}