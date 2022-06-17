package de.ancash.fancycrafting.gui;

import java.util.HashMap;
import java.util.Map;

import de.ancash.fancycrafting.FancyCrafting;

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

	public static void add(FancyCrafting pl, WorkspaceTemplate template) {
		templates.computeIfAbsent(template.getDimension().getWidth(), k -> new HashMap<>());
		templates.get(template.getDimension().getWidth()).put(template.getDimension().getHeight(), template);
	}

	public static WorkspaceTemplate get(int width, int height) {
		if (!templates.containsKey(width))
			return null;
		if (!templates.get(width).containsKey(height))
			return null;
		return templates.get(width).get(height);
	}
}