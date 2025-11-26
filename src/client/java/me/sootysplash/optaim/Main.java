package me.sootysplash.optaim;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("OptimalAim");
	public static void onInitialize() {
		AutoConfig.register(Config.class, GsonConfigSerializer::new);
		LOGGER.info("OptimalAim | Sootysplash was here");
	}
}