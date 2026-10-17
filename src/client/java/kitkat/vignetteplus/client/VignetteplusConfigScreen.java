package kitkat.vignetteplus.client;

import kitkat.vignetteplus.VignetteplusConfig;
import kitkat.vignetteplus.VignetteplusConfig.DrowningAppearType;
import kitkat.vignetteplus.VignetteplusConfig.HungerAppearType;
import kitkat.vignetteplus.VignetteplusConfig.HungerDisplayType;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.ColorFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// cloth config implementation script

public final class VignetteplusConfigScreen {
	private VignetteplusConfigScreen() {
	}

	public static Screen create(Screen parent) {
		VignetteplusConfig cfg = VignetteplusConfig.get();
		VignetteplusConfig defaults = new VignetteplusConfig();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(text("title"))
				.setSavingRunnable(VignetteplusConfig::save);

		ConfigEntryBuilder eb = builder.entryBuilder();

		// ==============================================================
		// general
		// ==============================================================
		ConfigCategory general = builder.getOrCreateCategory(text("category.general"));

		general.addEntry(eb.startBooleanToggle(text("option.mod_enabled"), cfg.modEnabled)
				.setDefaultValue(defaults.modEnabled)
				.setTooltip(text("option.mod_enabled.tooltip"))
				.setSaveConsumer(v -> cfg.modEnabled = v)
				.build());

		slider(general, eb, "all_transparency", cfg.allVignettesTransparency, defaults.allVignettesTransparency,
				0, 100, "%", v -> cfg.allVignettesTransparency = v, null);

		BooleanListEntry healthEnabled = eb.startBooleanToggle(text("option.health_enabled"), cfg.healthVignetteEnabled)
				.setDefaultValue(defaults.healthVignetteEnabled)
				.setTooltip(text("option.health_enabled.tooltip"))
				.setSaveConsumer(v -> cfg.healthVignetteEnabled = v)
				.build();
		BooleanListEntry drowningEnabled = eb.startBooleanToggle(text("option.drowning_enabled"), cfg.drowningVignetteEnabled)
				.setDefaultValue(defaults.drowningVignetteEnabled)
				.setTooltip(text("option.drowning_enabled.tooltip"))
				.setSaveConsumer(v -> cfg.drowningVignetteEnabled = v)
				.build();
		BooleanListEntry hungerEnabled = eb.startBooleanToggle(text("option.hunger_enabled"), cfg.hungerVignetteEnabled)
				.setDefaultValue(defaults.hungerVignetteEnabled)
				.setTooltip(text("option.hunger_enabled.tooltip"))
				.setSaveConsumer(v -> cfg.hungerVignetteEnabled = v)
				.build();
		general.addEntry(healthEnabled);
		general.addEntry(drowningEnabled);
		general.addEntry(hungerEnabled);

		Requirement healthOn = healthEnabled::getValue;
		Requirement drowningOn = drowningEnabled::getValue;
		Requirement hungerOn = hungerEnabled::getValue;

		// ==============================================================
		// low health vignette
		// ==============================================================
		ConfigCategory health = builder.getOrCreateCategory(text("category.health"));

		slider(health, eb, "min_health_threshold", cfg.minHealthThreshold, defaults.minHealthThreshold,
				1, 20, "", v -> cfg.minHealthThreshold = v, healthOn);
		slider(health, eb, "max_health_threshold", cfg.maxHealthThreshold, defaults.maxHealthThreshold,
				1, 20, "", v -> cfg.maxHealthThreshold = v, healthOn);

		// ==============================================================
		// hunger vignette
		// ==============================================================
		ConfigCategory hunger = builder.getOrCreateCategory(text("category.hunger"));

		EnumListEntry<HungerDisplayType> hungerType = enumEntry(hunger, eb, "hunger_display_type",
				HungerDisplayType.class, cfg.hungerDisplayType, defaults.hungerDisplayType,
				v -> cfg.hungerDisplayType = v, hungerOn);

		EnumListEntry<HungerAppearType> hungerAppear = enumEntry(hunger, eb, "hunger_appear_type",
				HungerAppearType.class, cfg.hungerAppearType, defaults.hungerAppearType,
				v -> cfg.hungerAppearType = v, hungerOn);

		Requirement hungerMatch = () -> hungerEnabled.getValue()
				&& hungerAppear.getValue() == HungerAppearType.MATCH_SATURATION_OR_HUNGER;
		Requirement saturationOn = () -> hungerEnabled.getValue()
				&& hungerType.getValue() == HungerDisplayType.LOW_SATURATION;
		Requirement hungerLevelOn = () -> hungerEnabled.getValue()
				&& hungerType.getValue() == HungerDisplayType.LOW_HUNGER;

		slider(hunger, eb, "hunger_min_transparency", cfg.hungerMinTransparency, defaults.hungerMinTransparency,
				0, 100, "%", v -> cfg.hungerMinTransparency = v, hungerMatch);
		slider(hunger, eb, "hunger_max_transparency", cfg.hungerMaxTransparency, defaults.hungerMaxTransparency,
				0, 100, "%", v -> cfg.hungerMaxTransparency = v, hungerMatch);
		slider(hunger, eb, "saturation_threshold", cfg.saturationThreshold, defaults.saturationThreshold,
				1, 20, "", v -> cfg.saturationThreshold = v, saturationOn);
		slider(hunger, eb, "hunger_threshold", cfg.hungerThreshold, defaults.hungerThreshold,
				1, 20, "", v -> cfg.hungerThreshold = v, hungerLevelOn);

		// ==============================================================
		// drowning vignette
		// ==============================================================
		ConfigCategory drowning = builder.getOrCreateCategory(text("category.drowning"));

		BooleanToggleBuilder onlyBelowBuilder = eb.startBooleanToggle(text("option.drowning_only_below_health"), cfg.drowningOnlyBelowHealth)
				.setDefaultValue(defaults.drowningOnlyBelowHealth)
				.setTooltip(text("option.drowning_only_below_health.tooltip"))
				.setSaveConsumer(v -> cfg.drowningOnlyBelowHealth = v);
		onlyBelowBuilder.setRequirement(drowningOn);
		BooleanListEntry onlyBelowHealth = onlyBelowBuilder.build();
		drowning.addEntry(onlyBelowHealth);

		Requirement drowningThresholdOn = () -> drowningEnabled.getValue() && onlyBelowHealth.getValue();
		slider(drowning, eb, "drowning_health_threshold", cfg.drowningHealthThreshold, defaults.drowningHealthThreshold,
				1, 20, "", v -> cfg.drowningHealthThreshold = v, drowningThresholdOn);

		EnumListEntry<DrowningAppearType> drowningAppear = enumEntry(drowning, eb, "drowning_appear_type",
				DrowningAppearType.class, cfg.drowningAppearType, defaults.drowningAppearType,
				v -> cfg.drowningAppearType = v, drowningOn);

		Requirement drowningAdjust = () -> drowningEnabled.getValue()
				&& drowningAppear.getValue() == DrowningAppearType.ADJUST_TO_HEALTH_LEFT;

		slider(drowning, eb, "drowning_min_transparency", cfg.drowningMinTransparency, defaults.drowningMinTransparency,
				0, 100, "%", v -> cfg.drowningMinTransparency = v, drowningAdjust);
		slider(drowning, eb, "drowning_max_transparency", cfg.drowningMaxTransparency, defaults.drowningMaxTransparency,
				0, 100, "%", v -> cfg.drowningMaxTransparency = v, drowningAdjust);

		// ==============================================================
		// customization
		// ==============================================================
		ConfigCategory customization = builder.getOrCreateCategory(text("category.customization"));

		color(customization, eb, "health_color", cfg.healthColor, defaults.healthColor,
				v -> cfg.healthColor = v, healthOn);
		color(customization, eb, "hunger_color", cfg.hungerColor, defaults.hungerColor,
				v -> cfg.hungerColor = v, hungerOn);
		color(customization, eb, "drowning_color", cfg.drowningColor, defaults.drowningColor,
				v -> cfg.drowningColor = v, drowningOn);

		return builder.build();
	}

	// ------------------------------------------------------------------
	// lang & misc.
	// ------------------------------------------------------------------

	private static Component text(String key) {
		return Component.translatable("config.vignetteplus." + key);
	}

	private static void slider(ConfigCategory category, ConfigEntryBuilder eb, String name,
							   int value, int defaultValue, int min, int max, String suffix,
							   Consumer<Integer> save, Requirement requirement) {
		IntSliderBuilder b = eb.startIntSlider(text("option." + name), value, min, max)
				.setDefaultValue(defaultValue)
				.setTooltip(text("option." + name + ".tooltip"))
				.setTextGetter(v -> Component.literal(v + suffix))
				.setSaveConsumer(save);
		if (requirement != null) {
			b.setRequirement(requirement);
		}
		category.addEntry(b.build());
	}

	private static <E extends Enum<E> & VignetteplusConfig.TranslatableOption> EnumListEntry<E> enumEntry(
			ConfigCategory category, ConfigEntryBuilder eb, String name, Class<E> type,
			E value, E defaultValue, Consumer<E> save, Requirement requirement) {
		EnumSelectorBuilder<E> b = eb.startEnumSelector(text("option." + name), type, value)
				.setDefaultValue(defaultValue)
				.setTooltip(text("option." + name + ".tooltip"))
				.setEnumNameProvider(e -> Component.translatable(((VignetteplusConfig.TranslatableOption) e).translationKey()))
				.setSaveConsumer(save);
		if (requirement != null) {
			b.setRequirement(requirement);
		}
		EnumListEntry<E> entry = b.build();
		category.addEntry(entry);
		return entry;
	}

	private static void color(ConfigCategory category, ConfigEntryBuilder eb, String name,
							  int value, int defaultValue, Consumer<Integer> save, Requirement requirement) {
		ColorFieldBuilder b = eb.startColorField(text("option." + name), value)
				.setAlphaMode(false) // opacity is controlled by the transparency options, not the color
				.setDefaultValue(defaultValue)
				.setTooltip(text("option." + name + ".tooltip"))
				.setSaveConsumer(save);
		if (requirement != null) {
			b.setRequirement(requirement);
		}
		category.addEntry(b.build());
	}
}