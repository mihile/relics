package it.hurts.sskirillss.relics.config;

import it.hurts.octostudios.octolib.modules.config.impl.OctoConfig;
import lombok.Data;

@Data
public class LootConfigData implements OctoConfig {
    private double relicGenChance = 0.33D;
}