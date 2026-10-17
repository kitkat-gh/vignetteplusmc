package kitkat.vignetteplus.client;

import kitkat.vignetteplus.Vignetteplus;
import kitkat.vignetteplus.VignetteplusConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;

// vignette rendering script

public final class VignetteRenderer {
	private static final Identifier TEXTURE = Vignetteplus.id("textures/gui/vignette.png");

	// fade time in ticks
	private static final float FADE_TICKS = 20.0F;

	private static float hungerFade = 0.0F;
	private static float drowningFade = 0.0F;

	private VignetteRenderer() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		VignetteplusConfig cfg = VignetteplusConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (!cfg.modEnabled || player == null || player.isCreative() || player.isSpectator()) {
			hungerFade = 0.0F;
			drowningFade = 0.0F;
			return;
		}

		float fadeStep = deltaTracker.getRealtimeDeltaTicks() / FADE_TICKS;
		float globalTransparency = cfg.allVignettesTransparency / 100.0F;
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();

		if (cfg.hungerVignetteEnabled) {
			float opacity = hungerOpacity(player, cfg, fadeStep);
			draw(graphics, width, height, cfg.hungerColor, opacity, globalTransparency);
		} else {
			hungerFade = 0.0F;
		}

		if (cfg.drowningVignetteEnabled) {
			float opacity = drowningOpacity(player, cfg, fadeStep);
			draw(graphics, width, height, cfg.drowningColor, opacity, globalTransparency);
		} else {
			drowningFade = 0.0F;
		}

		if (cfg.healthVignetteEnabled) {
			float opacity = healthOpacity(player, cfg);
			draw(graphics, width, height, cfg.healthColor, opacity, globalTransparency);
		}
	}

	// ------------------------------------------------------------------
	// low health vignette
	// ------------------------------------------------------------------

	private static float healthOpacity(LocalPlayer player, VignetteplusConfig cfg) {
		float health = player.getHealth();
		float start = cfg.minHealthThreshold; // vignette starts here, fully transparent
		float end = cfg.maxHealthThreshold;   // vignette is fully opaque here

		if (start <= end) {
			return health <= end ? 1.0F : 0.0F;
		}
		if (health >= start) {
			return 0.0F;
		}
		if (health <= end) {
			return 1.0F;
		}
		return (start - health) / (start - end);
	}

	// ------------------------------------------------------------------
	// hunger vignette
	// ------------------------------------------------------------------

	private static float hungerOpacity(LocalPlayer player, VignetteplusConfig cfg, float fadeStep) {
		FoodData food = player.getFoodData();
		boolean useSaturation = cfg.hungerDisplayType == VignetteplusConfig.HungerDisplayType.LOW_SATURATION;

		float value = useSaturation ? food.getSaturationLevel() : food.getFoodLevel();
		float threshold = useSaturation ? cfg.saturationThreshold : cfg.hungerThreshold;
		boolean active = value <= threshold;

		switch (cfg.hungerAppearType) {
			case APPEAR -> {
				hungerFade = active ? 1.0F : 0.0F;
				return hungerFade;
			}
			case FADE_IN -> {
				hungerFade = Mth.clamp(hungerFade + (active ? fadeStep : -fadeStep), 0.0F, 1.0F);
				return hungerFade;
			}
			case MATCH_SATURATION_OR_HUNGER -> {
				hungerFade = 0.0F;
				if (!active) {
					return 0.0F;
				}
				// 0 at the threshold, 1 when saturation/hunger is completely empty.
				float depletion = Mth.clamp(1.0F - value / threshold, 0.0F, 1.0F);
				// "min" transparency applies at the threshold, "max" when empty (defaults: 100% -> 0%).
				float transparency = Mth.lerp(depletion, cfg.hungerMinTransparency, cfg.hungerMaxTransparency) / 100.0F;
				return 1.0F - transparency;
			}
		}
		return 0.0F;
	}

	// ------------------------------------------------------------------
	// drowning vignette
	// ------------------------------------------------------------------

	private static float drowningOpacity(LocalPlayer player, VignetteplusConfig cfg, float fadeStep) {
		float health = player.getHealth();

		// 0 air check
		boolean active = player.getAirSupply() <= 0;
		if (active && cfg.drowningOnlyBelowHealth && health > cfg.drowningHealthThreshold) {
			active = false;
		}

		switch (cfg.drowningAppearType) {
			case APPEAR -> {
				drowningFade = active ? 1.0F : 0.0F;
				return drowningFade;
			}
			case FADE_IN -> {
				drowningFade = Mth.clamp(drowningFade + (active ? fadeStep : -fadeStep), 0.0F, 1.0F);
				return drowningFade;
			}
			case ADJUST_TO_HEALTH_LEFT -> {
				drowningFade = 0.0F;
				if (!active) {
					return 0.0F;
				}

				float top = cfg.drowningOnlyBelowHealth ? cfg.drowningHealthThreshold : player.getMaxHealth();
				float lost = Mth.clamp(1.0F - health / top, 0.0F, 1.0F);
				float transparency = Mth.lerp(lost, cfg.drowningMaxTransparency, cfg.drowningMinTransparency) / 100.0F;
				return 1.0F - transparency;
			}
		}
		return 0.0F;
	}

	// ------------------------------------------------------------------
	// core drawing
	// ------------------------------------------------------------------

	private static void draw(GuiGraphicsExtractor graphics, int width, int height, int rgb,
							 float opacity, float globalTransparency) {
		// vignette transparency setting scaling
		float finalOpacity = Mth.clamp(opacity, 0.0F, 1.0F) * (1.0F - globalTransparency);
		int alpha = Math.round(finalOpacity * 255.0F);
		if (alpha <= 0) {
			return;
		}

		int argb = (alpha << 24) | (rgb & 0xFFFFFF);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0.0F, 0.0F, width, height, width, height, argb);
	}
}