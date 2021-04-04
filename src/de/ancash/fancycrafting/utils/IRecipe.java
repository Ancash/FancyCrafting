package de.ancash.fancycrafting.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.datastructures.tuples.Tuple;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;

public abstract class IRecipe {

	private final ItemStack result;
	private final Collection<ItemStack> ings;
	private final String name;
	
	public IRecipe(ItemStack result, Collection<ItemStack> ings, String name) {
		this.result = result;
		this.ings = ings;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack getResult() {
		return result.clone();
	}
	
	public ItemStack getResultWithId() {
		return result.clone();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IRecipe)) return false;
		return IRecipe.isSimilar(result, ((IRecipe) obj).getResult(), false) &&
				IRecipe.matchesShapeless(Arrays.asList(ings.toArray(new ItemStack[ings.size()])), 
						Arrays.asList(((IRecipe) obj).ings.toArray(new ItemStack[((IRecipe) obj).ings.size()])), false);
	}
	
	@Override
	public String toString() {
		return result.toString();
	}
	
	public abstract int getIngredientsSize();
	
	public static boolean matchesShaped(CompactMap<Integer, ItemStack> a, CompactMap<Integer, ItemStack> b, boolean ignoreData) {
		if(a.size() != b.size()) return false;
		int cnt = 0;
		for(int key : a.keySet()) {
			ItemStack aIs = a.get(key);
			ItemStack bIs = b.get(key);
			if(isSimilar(aIs, bIs, ignoreData)) cnt++;
		}
		if(cnt != a.size()) return false;
		return true;
	}
	
	public static boolean matchesShapeless(Collection<ItemStack> ingredients, Collection<ItemStack> b, boolean ignoreData) {
		if(ingredients.size() != b.size()) return false;
		ItemStack[] copyOfA = ingredients.toArray(new ItemStack[ingredients.size()]);
		List<ItemStack> copyOfB = Arrays.asList(b.toArray(new ItemStack[b.size()]));
		for(int i1 = 0; i1<copyOfB.size(); i1++) {
			ItemStack bIs = copyOfB.get(i1);
			boolean matches = false;
			for(int i2 = 0; i2<copyOfA.length ;i2++) {
				ItemStack aIs = copyOfA[i2];
				if(aIs == null) continue;
				if(isSimilar(bIs, aIs, ignoreData)) {
					matches = true;
					copyOfA[i2] = null;
					break;
				}
			}
			if(!matches) return false;
		}
		return true;
	}
	
	public static Duplet<Integer, Integer> optimize(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int key : ingredientsMap.keySet()) {
			if(ingredientsMap.get(key) == null) ingredientsMap.remove(key);
		}
		Duplet<Integer, Integer> moves = Tuple.of(0, 0);
		boolean canMoveToLeft = canMoveToLeft(ingredientsMap);
		while(canMoveToLeft) {
			moveToLeft(ingredientsMap);
			moves.setFirst(moves.getFirst() + 1);
			canMoveToLeft = canMoveToLeft(ingredientsMap);
		}
		boolean canMoveUp = canMoveUp(ingredientsMap);
		while(canMoveUp) {
			moveUp(ingredientsMap);
			moves.setSecond(moves.getSecond() + 1);
			canMoveUp = canMoveUp(ingredientsMap);
		}
		return moves;
	}
	
	private static void moveUp(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int i = 4; i<=9; i++) {
			if(!ingredientsMap.containsKey(i)) continue;
			set(i, i - 3, ingredientsMap);
		}
	}
	
	private static void moveToLeft(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int a = 0; a<3; a++) {
			for(int b = 1; b<=3; b++) {
				set(a * 3 + b, a * 3 + b - 1, ingredientsMap);
			}
		}
	}
	
	public static void set(int a, int b, CompactMap<Integer, ItemStack> ingredientsMap) {
		ItemStack temp = ingredientsMap.get(a);
		if(temp == null) return;
		ingredientsMap.put(b, temp);
		ingredientsMap.remove(a);
	}
	
	private static boolean canMoveUp(CompactMap<Integer, ItemStack> ingredientsMap) {
		if(ingredientsMap.size() == 0) return false;
		for(int i = 1; i<=3; i++) {
			if(ingredientsMap.get(i) != null) return false;
		}
		return true;
	}
	
	private static boolean canMoveToLeft(CompactMap<Integer, ItemStack> ingredientsMap) {
		if(ingredientsMap.size() == 0) return false;
		int moveableSlotsToLeft = 2;
		for(int a = 2; a>=0; a--) {
			for(int b = 2; b>0; b--) {
				if(ingredientsMap.get(a * 3 + b) != null) {
					if(moveableSlotsToLeft >= b) moveableSlotsToLeft = b - 1;
				}
			}
		}
		return moveableSlotsToLeft > 0;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isSimilar(ItemStack a, ItemStack b, boolean ignoreData) {
		boolean bNull = b == null;
		boolean aNull = a == null;
		if(a == null && b == null) {
			return true;
		}
		if(aNull != bNull) {
			return false;
		}
		if(!a.getType().equals(b.getType())) {
			return false;
		}
		if(!matchesMeta(a, b)) {
			return false;
		}
		if(!matchesNBT(a, b)) {
			return false;
		}
		
		boolean matches = false;
		if(!a.getType().equals(Material.SKULL_ITEM)) {
			String one = a.toString().split("\\{")[1].split(" x")[0];
			String two = b.toString().split("\\{")[1].split(" x")[0];
			if(!ignoreData && !one.equals(two)) {
				return false;
			}
			if(one.equals(two)) matches = true;
		} else {
			SkullMeta aM = (SkullMeta) a.getItemMeta();
			SkullMeta bM = (SkullMeta) b.getItemMeta();
			if(aM.getOwner() == null && bM.getOwner() != null && !bM.getOwner().equals("Ancash")) 
				return false;
			
			if(aM.getOwner() != null && bM.getOwner() == null && !aM.getOwner().equals("Ancash")) 
				return false;
			
			
			if(aM.getOwner() != null && !aM.getOwner().equals(bM.getOwner()))  
				return false;
			
			try {
				String aT = getTexure(a);
				String bT = getTexure(b);
				if(aT != null && !aT.equals(bT)) return false;
				if(bT != null && !aT.equals(aT)) return false;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			matches = true;
		}
		if(!ignoreData && a.getData().getData() != -1 
				&& b.getData().getData() != -1 
				&& a.getData().getData() != b.getData().getData()
				&& !matches) {
			return false;
		}
		return true;
	}
	
	public static boolean matchesNBT(ItemStack a, ItemStack b) {
		NBTItem aNbt = new NBTItem(a);
		NBTItem bNbt = new NBTItem(b);
		if(!aNbt.getKeys().equals(bNbt.getKeys())) {
			if(!(aNbt.getKeys().size() - 2 == bNbt.getKeys().size() ||
					aNbt.getKeys().size() - 1 == bNbt.getKeys().size() ||
					aNbt.getKeys().size() + 2 == bNbt.getKeys().size() ||
					aNbt.getKeys().size() + 1 == bNbt.getKeys().size())) {
				return false;
			}
		}
		for(String key : aNbt.getKeys()) {
			if(key.equals("meta-type") || key.equals("Damage")) continue;
			Object aOb = aNbt.getObject(key, Object.class);
			Object bOb = bNbt.getObject(key, Object.class);
			if(aOb == null && bOb == null) continue;
			if(aOb.equals(bOb)) return false;
		}
		return true;
	}
	
	public static boolean matchesMeta(ItemStack a, ItemStack b) {
		boolean aHas = a.getItemMeta() == null;
		boolean bHas = b.getItemMeta() == null;
		if(aHas != bHas) return false;
		if(a.getItemMeta() == null || b.getItemMeta() == null) return true;
		if(a.getAmount() < b.getAmount()) return false;
		ItemMeta aM = a.getItemMeta();
		ItemMeta bM = b.getItemMeta();
		if(aM.getLore() == null && bM.getLore() != null) return false;
		if(aM.getLore() != null && bM.getLore() == null) return false;
		if(aM.getLore() != null && !aM.getLore().equals(bM.getLore())) return false;
		if(!aM.getEnchants().equals(bM.getEnchants())) return false;
		if(aM.getDisplayName() != null && bM.getDisplayName() == null) return false;
		if(aM.getDisplayName() == null && bM.getDisplayName() != null) return false;
		if(aM.getDisplayName() != null && !aM.getDisplayName().equals(bM.getDisplayName())) return false;
		if(!aM.getItemFlags().equals(bM.getItemFlags())) return false;
		return true;
	}
	
	public static ItemStack setTexture(ItemStack pet, String texture) {
		SkullMeta hm = (SkullMeta) pet.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		try {
			Field field = hm.getClass().getDeclaredField("profile");
			field.setAccessible(true);
			field.set(hm, profile);
		} catch(IllegalArgumentException  | NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		pet.setItemMeta(hm);
		return pet;
	}
	
	public static String getTexure(ItemStack pet) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String texture = null;
		if(!pet.getType().name().toLowerCase().contains("skull_item")) return null;
		SkullMeta sm = (SkullMeta) pet.getItemMeta();
		Field profileField = sm.getClass().getDeclaredField("profile");
		profileField.setAccessible(true);
		GameProfile profile = (GameProfile) profileField.get(sm);
		Collection<Property> textures = profile.getProperties().get("textures");
		for(Property p : textures) {
			texture = p.getValue();
		}
		return texture;
	}
	
	public static CompactMap<Integer, ItemStack> toMap(Map<Character, ItemStack> ingredients, String[] shapes) {
		CompactMap<Integer, ItemStack> ings = new CompactMap<Integer, ItemStack>();
		int row = 0;
		for(String shape : shapes) {
			int charCnt = 1;
			for(char c : shape.toCharArray()) {
				ings.put(row * 3 + charCnt, ingredients.get(c));
				charCnt++;
			}
			row++;
		}
		return ings;
	}
	
	public static IRecipe toIRecipe(Recipe recipe) {
		if(recipe instanceof ShapedRecipe) return new IShapedRecipe(recipe.getResult(), ((ShapedRecipe)recipe).getIngredientMap(), ((ShapedRecipe) recipe).getShape());
		if(recipe instanceof ShapelessRecipe) return new IShapelessRecipe(recipe.getResult(), ((ShapelessRecipe) recipe).getIngredientList(), null);
		return null;
	}
}
