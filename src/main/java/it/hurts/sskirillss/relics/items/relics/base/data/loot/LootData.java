package it.hurts.sskirillss.relics.items.relics.base.data.loot;

import it.hurts.sskirillss.relics.config.data.LootConfigData;
import it.hurts.sskirillss.relics.config.data.LootEntryConfigData;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class LootData {
    @Builder.Default
    private List<LootEntry> entries;

    public LootConfigData toConfigData() {
        return new LootConfigData(entries.stream().map(entry -> new LootEntryConfigData(entry.getDimensions(), entry.getBiomes(), entry.getTables(), entry.getChance())).toList());
    }

    public static class LootDataBuilder {
        private List<LootEntry> entries = new ArrayList<>();

        public LootDataBuilder entry(LootEntry... entries) {
            this.entries.addAll(Arrays.asList(entries));

            return this;
        }
    }
}