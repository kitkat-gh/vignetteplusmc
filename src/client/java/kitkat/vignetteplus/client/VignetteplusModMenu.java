package kitkat.vignetteplus.client;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// mod menu implementation script

public class VignetteplusModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return VignetteplusConfigScreen::create;
	}
}