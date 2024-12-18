package me.sootysplash;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("OptimalAim");
	@Override
	public void onInitialize() {
		AutoConfig.register(Config.class, GsonConfigSerializer::new);
		LOGGER.info("OptimalAim | Sootysplash was here");
	}
}