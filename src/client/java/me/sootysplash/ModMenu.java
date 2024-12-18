package me.sootysplash;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.*;
import net.minecraft.text.Text;


public class ModMenu implements ModMenuApi {


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("Config"))
                    .setSavingRunnable(config::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));
            ConfigEntryBuilder cfgent =  builder.entryBuilder();



            general.addEntry(cfgent.startBooleanToggle(Text.of("Enabled"), config.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.of("Render the cube?"))
                    .setSaveConsumer(newValue -> config.enabled = newValue)
                    .build());

            general.addEntry(cfgent.startDoubleField(Text.of("Size"), config.size)
                    .setMin(0.2)
                    .setMax(1.0)
                    .setDefaultValue(0.6)
                    .setTooltip(Text.of("The size of the cube"))
                    .setSaveConsumer(newValue -> config.size = newValue)
                    .build());

            general.addEntry(cfgent.startColorField(Text.of("Color"), config.color)
                    .setDefaultValue(11477776)
                    .setTooltip(Text.of("The RGB value"))
                    .setSaveConsumer(newValue -> config.color = newValue)
                    .build());

            general.addEntry(cfgent.startIntSlider(Text.of("Transparency"), config.transparency, 0, 100)
                    .setDefaultValue(42)
                    .setSaveConsumer(newValue -> config.transparency = newValue)
                    .build());

            general.addEntry(cfgent.startBooleanToggle(Text.of("Conform to hitbox"), config.hitbox)
                    .setDefaultValue(true)
                    .setTooltip(Text.of("Contains the cube to the entity hitbox"))
                    .setSaveConsumer(newValue -> config.hitbox = newValue)
                    .build());

            general.addEntry(cfgent.startDoubleField(Text.of("Max distance to entity"), config.dist)
                    .setMin(1.0)
                    .setMax(12.0)
                    .setDefaultValue(6.0)
                    .setSaveConsumer(newValue -> config.dist = newValue)
                    .build());



            return builder.build();
        };
    }

}
