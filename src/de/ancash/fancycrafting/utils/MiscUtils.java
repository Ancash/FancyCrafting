package de.ancash.fancycrafting.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.ancash.ilibrary.yaml.configuration.file.YamlFile;

public class MiscUtils {

	public static ItemStack get(YamlFile configuration, String path) {
		ItemStack is = new ItemStack(Material.valueOf(configuration.getString(path + ".type")), 1, (short) configuration.getInt(path + ".meta.data"));
		ItemMeta im = is.getItemMeta();
		if(configuration.getString(path + ".meta.displayname") != null) im.setDisplayName(configuration.getString(path + ".meta.displayname").replace("&", "§"));
		List<String> lore = new ArrayList<String>();
		configuration.getStringList(path + ".meta.lore").forEach(str -> lore.add(str.replace("&", "§")));
		if(lore != null) im.setLore(lore);
		List<String> flags = configuration.getStringList(path + ".meta.flags");
		for(String flag : flags) {
			im.addItemFlags(ItemFlag.valueOf(flag));
		}
		is.setItemMeta(im);
		if(is.getType().equals(Material.valueOf("SKULL_ITEM")) && configuration.getString(path + ".meta.texture") != null) {
			is = setTexture(is, configuration.getString(path + ".meta.texture"));
		}
		List<String> enchs = configuration.getStringList(path + ".meta.enchantments");
		for(String ench : enchs) {
			is.addUnsafeEnchantment(Enchantment.getByName(ench.split(":")[0]), Integer.valueOf(ench.split(":")[1]));
		}
		return is;
	}
	
	public static ItemStack get(FileConfiguration configuration, String path) {
		ItemStack is = new ItemStack(Material.valueOf(configuration.getString(path + ".type")), 1, (short) configuration.getInt(path + ".meta.data"));
		ItemMeta im = is.getItemMeta();
		if(configuration.getString(path + ".meta.displayname") != null) im.setDisplayName(configuration.getString(path + ".meta.displayname").replace("&", "§"));
		List<String> lore = new ArrayList<String>();
		configuration.getStringList(path + ".meta.lore").forEach(str -> lore.add(str.replace("&", "§")));
		if(lore != null) im.setLore(lore);
		List<String> flags = configuration.getStringList(path + ".meta.flags");
		for(String flag : flags) {
			im.addItemFlags(ItemFlag.valueOf(flag));
		}
		is.setItemMeta(im);
		if(is.getType().equals(Material.valueOf("SKULL_ITEM")) && configuration.getString(path + ".meta.texture") != null) {
			is = setTexture(is, configuration.getString(path + ".meta.texture"));
		}
		List<String> enchs = configuration.getStringList(path + ".meta.enchantments");
		for(String ench : enchs) {
			is.addUnsafeEnchantment(Enchantment.getByName(ench.split(":")[0]), Integer.valueOf(ench.split(":")[1]));
		}
		return is;
	}
	
	public static ItemStack setTexture(ItemStack is, String texture) {
		SkullMeta hm = (SkullMeta) is.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		try {
			Field field = hm.getClass().getDeclaredField("profile");
			field.setAccessible(true);
			field.set(hm, profile);
		} catch(IllegalArgumentException  | NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		is.setItemMeta(hm);
		return is;
	}
	
}
