package kitkat.vignetteplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

// file writing, reading, config, etc. script

public class VignetteplusConfig {

	public interface TranslatableOption {
		String translationKey();
	}

	public enum HungerDisplayType implements TranslatableOption {
		LOW_SATURATION("low_saturation"),
		LOW_HUNGER("low_hunger");

		private final String id;

		HungerDisplayType(String id) {
			this.id = id;
		}

		@Override
		public String translationKey() {
			return "config.vignetteplus.enum.hunger_display_type." + id;
		}
	}

	public enum HungerAppearType implements TranslatableOption {
		FADE_IN("fade_in"),
		APPEAR("appear"),
		MATCH_SATURATION_OR_HUNGER("match");

		private final String id;

		HungerAppearType(String id) {
			this.id = id;
		}

		@Override
		public String translationKey() {
			return "config.vignetteplus.enum.hunger_appear_type." + id;
		}
	}

	public enum DrowningAppearType implements TranslatableOption {
		FADE_IN("fade_in"),
		APPEAR("appear"),
		ADJUST_TO_HEALTH_LEFT("adjust");

		private final String id;

		DrowningAppearType(String id) {
			this.id = id;
		}

		@Override
		public String translationKey() {
			return "config.vignetteplus.enum.drowning_appear_type." + id;
		}
	}

	// === general ===
	public boolean modEnabled = true;
	public int allVignettesTransparency = 0;
	public boolean healthVignetteEnabled = true;
	public boolean drowningVignetteEnabled = true;
	public boolean hungerVignetteEnabled = true;

	// === low health vignette ===
	public int minHealthThreshold = 6;
	public int maxHealthThreshold = 2;

	// === hunger vignette ===
	public HungerDisplayType hungerDisplayType = HungerDisplayType.LOW_SATURATION;
	public HungerAppearType hungerAppearType = HungerAppearType.FADE_IN;
	public int hungerMinTransparency = 100;
	public int hungerMaxTransparency = 0;
	public int saturationThreshold = 4;
	public int hungerThreshold = 16;

	// === drowning vignette ===
	public boolean drowningOnlyBelowHealth = false;
	public int drowningHealthThreshold = 10;
	public DrowningAppearType drowningAppearType = DrowningAppearType.FADE_IN;
	public int drowningMinTransparency = 0;
	public int drowningMaxTransparency = 100;

	// === customization ===
	public int healthColor = 0xFC0303;
	public int hungerColor = 0xFC7B03;
	public int drowningColor = 0x80D0FF;

	// ------------------------------------------------------------------
	// persistence
	// ------------------------------------------------------------------

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("vignetteplus.json");

	private static VignetteplusConfig instance = new VignetteplusConfig();

	public static VignetteplusConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				VignetteplusConfig loaded = GSON.fromJson(reader, VignetteplusConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | RuntimeException e) {
				Vignetteplus.LOGGER.error("Could not read vignetteplus.json, using defaults", e);
			}
		}
		save();
	}

	public static void save() {
		instance.validate();
		try (Writer writer = Files.newBufferedWriter(PATH)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			Vignetteplus.LOGGER.error("Could not write vignetteplus.json", e);
		}
	}

	/** hand edit mistake repair */
	private void validate() {
		allVignettesTransparency = clamp(allVignettesTransparency, 0, 100);

		minHealthThreshold = clamp(minHealthThreshold, 1, 20);
		maxHealthThreshold = clamp(maxHealthThreshold, 1, 20);

		if (hungerDisplayType == null) hungerDisplayType = HungerDisplayType.LOW_SATURATION;
		if (hungerAppearType == null) hungerAppearType = HungerAppearType.FADE_IN;
		hungerMinTransparency = clamp(hungerMinTransparency, 0, 100);
		hungerMaxTransparency = clamp(hungerMaxTransparency, 0, 100);
		saturationThreshold = clamp(saturationThreshold, 1, 20);
		hungerThreshold = clamp(hungerThreshold, 1, 20);

		drowningHealthThreshold = clamp(drowningHealthThreshold, 1, 20);
		if (drowningAppearType == null) drowningAppearType = DrowningAppearType.FADE_IN;
		drowningMinTransparency = clamp(drowningMinTransparency, 0, 100);
		drowningMaxTransparency = clamp(drowningMaxTransparency, 0, 100);

		healthColor &= 0xFFFFFF;
		hungerColor &= 0xFFFFFF;
		drowningColor &= 0xFFFFFF;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}