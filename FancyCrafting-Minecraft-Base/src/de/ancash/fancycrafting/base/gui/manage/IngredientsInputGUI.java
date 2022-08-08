package de.ancash.fancycrafting.base.gui.manage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.minecraft.inventory.input.ItemInputIGUI;
import de.ancash.minecraft.inventory.input.ItemInputSlots;

public class IngredientsInputGUI extends ItemInputIGUI {

	private static final ItemInputSlots inputSlots = new ItemInputSlots(
			IntStream.range(0, 54).filter(i -> (i + 1) % 9 != 0).boxed().collect(Collectors.toSet()));

	private final AbstractFancyCrafting pl;
	private boolean shaped;
	private final ItemStack[] ingredients;
	private BiConsumer<ItemStack[], Boolean> onInput;

	public IngredientsInputGUI(AbstractFancyCrafting pl, UUID id, ItemStack[] ingredients, boolean shaped) {
		super(inputSlots, id, 54, pl.getWorkspaceObjects().getIngredientsInputTitle());
		this.pl = pl;
		this.ingredients = ingredients;
		this.shaped = shaped;
		super.setOnInput(i -> Optional.ofNullable(this.onInput).ifPresent(con -> con.accept(i, this.shaped)));
	}

	@Override
	public void open() {
		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getCloseItem().getOriginal(), 53,
				(d, e, f, top2) -> Optional.ofNullable(top2 ? this : null).ifPresent(IngredientsInputGUI::closeAll)));
		for (int col = 0; col < 6; col++)
			for (int row = 0; row < 8; row++)
				setItem(ingredients[col * 8 + row], col * 9 + row);
		addInventoryItem(new InventoryItem(this, shaped ? pl.getWorkspaceObjects().getShapedItem().getOriginal()
				: pl.getWorkspaceObjects().getShapelessItem().getOriginal(), 44, (a, b, c, top) -> {
					if (top) {
						shaped = !shaped;
						setItem(shaped ? pl.getWorkspaceObjects().getShapedItem().getOriginal()
								: pl.getWorkspaceObjects().getShapelessItem().getOriginal(), 44);
					}
				}));
		for (int i = 0; i < 4; i++)
			setItem(pl.getWorkspaceObjects().getBackgroundItem().getOriginal(), i * 9 + 8);
		IGUIManager.register(this, getId());
		super.open();
	}

	@Override
	public void setOnInput(Consumer<ItemStack[]> onInput) {
		throw new UnsupportedOperationException();
	}

	public void onInput(BiConsumer<ItemStack[], Boolean> onInput) {
		this.onInput = onInput;
	}

	public static ItemStack getManageIngredientsItem(AbstractFancyCrafting pl, List<ItemStack> ings, int width, int heigth, boolean shaped,
			boolean random) {
		ItemStack item = pl.getWorkspaceObjects().getManageIngredientsItem().getOriginal();
		Map<String, String> placeholder = new HashMap<>();
		ItemStack[] ingsa = new ItemStack[width * heigth];
		for(int i = 0; i<ings.size(); i++)
			ingsa[i] = ings.get(i);
		placeholder.put("%ingredients%", String.join("\n", IRecipe.ingredientsToList(pl, ingsa, width, heigth, pl.getWorkspaceObjects().getManageIngredientsIdFormat()))); //$NON-NLS-1$ //$NON-NLS-2$
		placeholder.put("%rtype%", (shaped ? "Shaped" : "Shapeless") + (random ? " Random Recipe" : " Recipe")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		return ItemStackUtils.setLore(ItemStackUtils.replacePlaceholder(item.getItemMeta().getLore(), placeholder),
				item);
	}

}