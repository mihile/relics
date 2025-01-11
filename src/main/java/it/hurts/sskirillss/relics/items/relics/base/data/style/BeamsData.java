package it.hurts.sskirillss.relics.items.relics.base.data.style;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeamsData {
    @Builder.Default
    private int startColor = 0xFFFFFF00;
    @Builder.Default
    private int endColor = 0x00FF0000;
}