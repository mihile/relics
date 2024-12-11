package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class LevelingSourcesData {
    @Builder.Default
    private Map<String, LevelingSourceData> sources;

    public static class LevelingSourcesDataBuilder {
        private Map<String, LevelingSourceData> sources = new LinkedHashMap<>();

        // TODO: Replace static init with registry entry
        {
            var entry = LevelingSourceData.genericBuilder("spreading")
                    .genericIcon("spreading")
                    .initialValue(25)
                    .gem(GemShape.OVAL, GemColor.YELLOW)
                    .build();

            sources.put(entry.getId(), entry);
        }

        public LevelingSourcesDataBuilder source(LevelingSourceData source) {
            sources.put(source.getId(), source);

            return this;
        }
    }
}