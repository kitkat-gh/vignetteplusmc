package kitkat.vignetteplus.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kitkat.vignetteplus.Vignetteplus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class VignetteplusClient implements ClientModInitializer {
	public static final String MOD_ID = "vignetteplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				Vignetteplus.id("vignettes"),
				VignetteRenderer::extract
		);

		VignetteplusKeybinds.register();
		LOGGER.info("[ 🟢| VignetteplusClient.java ] Init success on client");
	}
}