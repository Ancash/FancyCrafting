package de.ancash.fancycrafting.base;

import java.util.HashMap;
import java.util.Map;

public class WorkspaceTemplate {

	private static final Map<Integer, Map<Integer, WorkspaceTemplate>> templates = new HashMap<>();

	private final String title;
	private final WorkspaceDimension dim;
	private final WorkspaceSlots slots;

	public WorkspaceTemplate(String title, WorkspaceDimension dim, WorkspaceSlots slots) {
		this.title = title;
		this.dim = dim;
		this.slots = slots;
	}

	public WorkspaceDimension getDimension() {
		return dim;
	}

	public WorkspaceSlots getSlots() {
		return slots;
	}

	public String getTitle() {
		return title;
	}

	@SuppressWarnings("nls")
	public static void add(AbstractFancyCrafting pl, WorkspaceTemplate template) {
		templates.computeIfAbsent(template.getDimension().getWidth(), k -> new HashMap<>());
		templates.get(template.getDimension().getWidth()).put(template.getDimension().getHeight(), template);
		pl.getLogger().fine("----------------------------------------------");
		pl.getLogger().fine("WorkspaceTemplate: ");
		pl.getLogger().fine("Title: " + template.getTitle());
		pl.getLogger().fine("Width: " + template.getDimension().getWidth());
		pl.getLogger().fine("Height: " + template.getDimension().getHeight());
		pl.getLogger().fine("Size: " + template.getDimension().getSize());
		pl.getLogger().fine("Slots: \n" + template.getSlots().toString());
		pl.getLogger().fine("----------------------------------------------");
	}

	public static WorkspaceTemplate get(int width, int height) {
		if (!templates.containsKey(width))
			return null;
		if (!templates.get(width).containsKey(height))
			return null;
		return templates.get(width).get(height);
	}
}