package kitkat.vignetteplus;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vignetteplus implements ModInitializer {
	public static final String MOD_ID = "vignetteplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		VignetteplusConfig.load();
		LOGGER.info("[ 🟢| Vignetteplus.java ] Init success on server");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}