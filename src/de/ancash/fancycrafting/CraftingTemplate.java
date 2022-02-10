package de.ancash.fancycrafting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CraftingTemplate {
	
	private static final Map<Integer, CraftingTemplate> templates = new HashMap<>();
	
	public static void add(FancyCrafting pl, CraftingTemplate template, int m) {
		if(template.craftingSlots.length != m * m)  {
			pl.warn("Invalid crafting slots for " + m + "x" + m + " crafting! Need " + (m * m) + " slots, got " + template.craftingSlots.length);
			return;
		}
		templates.put(m, template);
		pl.info("Properties for " + m + "x" + m + " crafting:");
		pl.info("Title: " + template.title);
		pl.info("Size: " + template.size);
		pl.info("Result slot: " + template.resultSlot);
		pl.info("Close slot: " + template.closeSlot);
		pl.info("Back slot: " + template.backSlot);
		pl.info("Prev slot: " + template.prevSlot);
		pl.info("Next slot: " + template.nextSlot);
		pl.info("Edit slot: " + template.editSlot);
		if(m == 6) {
			pl.info("Save slot: " + template.saveSlot);
			pl.info("Delete slot: " + template.deleteSlot);
			pl.info("Recipe type slot: " + template.recipeTypeSlot);
		}
		pl.info("Crafting slots: " + Arrays.stream(template.craftingSlots).boxed().collect(Collectors.toList()));
		pl.info("Craft state slots: " + Arrays.stream(template.craftStateSlots).boxed().collect(Collectors.toList()));
	}
	
	public static CraftingTemplate get(int m) {
		return templates.get(m);
	}
	
	private final String title;
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
	
	public CraftingTemplate(String title, int size, int resultSlot, int closeSlot, 
			int backSlot, int prevSlot, int nextSlot, int editSlot, 
			int saveSlot, int deleteSlot, int recipeTypeSlot, int[] craftingSlots, int[] craftStateSlots) {
		this.title = title;
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
}