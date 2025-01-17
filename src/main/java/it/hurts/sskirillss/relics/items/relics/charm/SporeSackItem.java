package it.hurts.sskirillss.relics.items.relics.charm;

import it.hurts.sskirillss.relics.entities.SporeEntity;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SporeSackItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("spore_mist")
                                .stat(StatData.builder("amount")
                                        .initialValue(3, 8)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("spore_mist")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff582f27)
                                .borderBottom(0xff503f3a)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFF00FF00)
                                .endColor(0x00FFFF00)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.TROPIC)
                        .build())
                .build();
    }

    public boolean isToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, false);
    }

    public void setToggled(ItemStack stack, boolean isToggled) {
        stack.set(DataComponentRegistry.TOGGLED, isToggled);
    }

    public int getCharges(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharges(ItemStack stack, int charges) {
        stack.set(DataComponentRegistry.CHARGE, charges);
    }

    public void addCharges(ItemStack stack, int charges) {
        setCharges(stack, Math.clamp(getCharges(stack) + charges, 0, (int) Math.round(getStatValue(stack, "spore_mist", "amount"))));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        var charges = getCharges(stack);
        var isToggled = isToggled(stack);

        var time = player.tickCount + (slotContext.index() * 10);

        var percentageMedian = 0.5D;
        var percentage = player.getHealth() / player.getMaxHealth();

        if (isToggled) {
            if (charges > 0) {
                var speed = 3D;

                if (time % speed != 0)
                    return;

                var level = player.getCommandSenderWorld();

                var cycle = charges * 1D;
                var angle = (Math.floor(time / speed) % cycle) / cycle * 2D * Math.PI;

                var entity = new SporeEntity(EntityRegistry.SPORE.get(), level);

                entity.setOwner(player);
                entity.setRelicStack(stack);
                entity.setPos(player.position().add(0F, player.getBbHeight() / 2F, 0F));
                entity.setDeltaMovement(Math.cos(angle) * 0.5F, 0.35F, Math.sin(angle) * 0.5F);
                entity.setDamage((float) ((player.getMaxHealth() - player.getHealth()) * getStatValue(stack, "spore_mist", "damage")));

                level.addFreshEntity(entity);

                level.playSound(null, player.blockPosition(), SoundEvents.PUFFER_FISH_FLOP, SoundSource.MASTER, 1F, 1.5F);

                addCharges(stack, -1);
            } else if (percentage > percentageMedian)
                setToggled(stack, false);
        } else if (percentage < percentageMedian) {
            setToggled(stack, true);
            setCharges(stack, (int) Math.round(getStatValue(stack, "spore_mist", "amount")));
        }
    }
}