package kitkat.vignetteplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import kitkat.vignetteplus.Vignetteplus;
import kitkat.vignetteplus.VignetteplusConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

// keybind script

public final class VignetteplusKeybinds {

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Vignetteplus.id("vignetteplus"));

	private static KeyMapping toggleMod;
	private static KeyMapping openConfig;

	private VignetteplusKeybinds() {
	}

	public static void register() {
		toggleMod = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.vignetteplus.toggle_mod",
				InputConstants.KEY_F12,
				CATEGORY
		));

		openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.vignetteplus.open_config",
				InputConstants.KEY_V,
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleMod.consumeClick()) {
				VignetteplusConfig cfg = VignetteplusConfig.get();
				cfg.modEnabled = !cfg.modEnabled;
				VignetteplusConfig.save();
			}

			while (openConfig.consumeClick()) {
				client.gui.setScreen(VignetteplusConfigScreen.create(client.gui.screen()));
			}
		});
	}
}