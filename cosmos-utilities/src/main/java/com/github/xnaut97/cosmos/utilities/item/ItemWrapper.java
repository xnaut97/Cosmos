package com.github.xnaut97.cosmos.utilities.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemWrapper {
	/**
	 * Convert {@code item} into {@code String} with encoder as <b>Base64</b>
	 */
	public static String toBase64(ItemStack item) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			dataOutput.writeObject(item);

			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception ignored) {
		}
		return null;
	}

	/**
	 * Reverse {@code data} ({@code String} form) of an item back to an
	 * {@link ItemStack} by using <b>Base64</b>
	 */
	public static ItemStack fromBase64(String data) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack i = (ItemStack) dataInput.readObject();

			dataInput.close();
			return i;
		} catch (Exception ignored) {
		}
		return null;
	}
}
