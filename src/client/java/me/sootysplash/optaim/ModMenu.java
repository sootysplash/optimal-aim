package me.sootysplash.optaim;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.*;
import net.minecraft.network.chat.Component;


public class ModMenu implements ModMenuApi {


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.nullToEmpty("Config"))
                    .setSavingRunnable(config::save);

            ConfigCategory general = builder.getOrCreateCategory(Component.nullToEmpty("General"));
            ConfigEntryBuilder cfgent =  builder.entryBuilder();



            general.addEntry(cfgent.startBooleanToggle(Component.nullToEmpty("Enabled"), config.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Component.nullToEmpty("Render the cube?"))
                    .setSaveConsumer(newValue -> config.enabled = newValue)
                    .build());

            general.addEntry(cfgent.startDoubleField(Component.nullToEmpty("Size"), config.size)
                    .setMin(0.2)
                    .setMax(1.0)
                    .setDefaultValue(0.6)
                    .setTooltip(Component.nullToEmpty("The size of the cube"))
                    .setSaveConsumer(newValue -> config.size = newValue)
                    .build());

            general.addEntry(cfgent.startColorField(Component.nullToEmpty("Color"), config.color)
                    .setDefaultValue(11477776)
                    .setTooltip(Component.nullToEmpty("The RGB value"))
                    .setSaveConsumer(newValue -> config.color = newValue)
                    .build());

            general.addEntry(cfgent.startIntField(Component.nullToEmpty("Transparency"), config.transparency)
                    .setMin(0)
                    .setMax(100)
                    .setDefaultValue(42)
                    .setSaveConsumer(newValue -> config.transparency = newValue)
                    .build());

            general.addEntry(cfgent.startBooleanToggle(Component.nullToEmpty("Conform to hitbox"), config.hitbox)
                    .setDefaultValue(true)
                    .setTooltip(Component.nullToEmpty("Contains the cube to the entity hitbox"))
                    .setSaveConsumer(newValue -> config.hitbox = newValue)
                    .build());

            general.addEntry(cfgent.startDoubleField(Component.nullToEmpty("Max distance to entity"), config.dist)
                    .setMin(1.0)
                    .setMax(12.0)
                    .setDefaultValue(6.0)
                    .setSaveConsumer(newValue -> config.dist = newValue)
                    .build());



            return builder.build();
        };
    }

}
