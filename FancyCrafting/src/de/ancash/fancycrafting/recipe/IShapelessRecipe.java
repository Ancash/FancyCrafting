package de.ancash.fancycrafting.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.IItemStack;

public class IShapelessRecipe extends IRecipe {

	private final List<ItemStack> ings;
	private final List<IItemStack> iings;
	private final int width;
	private final int height;

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null).collect(Collectors.toList()));
		this.iings = Collections.unmodifiableList(this.ings.stream().map(IItemStack::new).collect(Collectors.toList()));
		int w = 1;
		int h = 1;
		while (w * h < ings.size()) {
			w++;
			h++;
			if (h == 6)
				h = 6;
			if (w == 8)
				break;
		}
		width = w;
		height = h;
	}

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla) {
		super(result, name, vanilla);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null).collect(Collectors.toList()));
		this.iings = Collections.unmodifiableList(this.ings.stream().map(IItemStack::new).collect(Collectors.toList()));
		int w = 1;
		int h = 1;
		while (w * h < ings.size()) {
			w++;
			h++;
			if (h == 6)
				h = 6;
			if (w == 8)
				break;
		}
		width = w;
		height = h;
	}

	public List<ItemStack> getIngredients() {
		return ings;
	}
	
	public List<IItemStack> getIIngredients() {
		return iings;
	}
	
	@Override
	public int getIngredientsSize() {
		return ings.size();
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}
}