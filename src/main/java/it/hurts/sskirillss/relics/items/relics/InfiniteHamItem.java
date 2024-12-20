package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class InfiniteHamItem extends RelicItem {
    public InfiniteHamItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("regeneration")
                                .requiredPoints(2)
                                .stat(StatData.builder("cooldown")
                                        .initialValue(30D, 15D)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("feed")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("marinade")
                                .requiredLevel(5)
                                .stat(StatData.builder("duration")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("meat_bat")
                                .requiredLevel(10)
                                .stat(StatData.builder("damage")
                                        .initialValue(0.5D, 2D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("stun")
                                        .initialValue(0.05D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(20)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("regeneration")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
                                        .build())
                                .source(LevelingSourceData.abilityBuilder("marinade")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
                                        .build())
                                .source(LevelingSourceData.abilityBuilder("meat_bat")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff644a41)
                                .borderBottom(0xff592410)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        ItemStack stack = this.getDefaultInstance();

        setPieces(stack, getMaxPieces());

        constructor.entry(CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if (level.isClientSide() || !(entityIn instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration")
                || entityIn.tickCount % (int) (getStatValue(stack, "regeneration", "cooldown") * 20) != 0)
            return;

        int charge = stack.getOrDefault(CHARGE, 0);

        if (charge >= getMaxPieces())
            return;

        stack.set(CHARGE, ++charge);

        super.inventoryTick(stack, level, entityIn, itemSlot, isSelected);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (canPlayerUseAbility(player, stack, "regeneration") && stack.getOrDefault(CHARGE, 0) > 0 && player.getFoodData().needsFood()) {
            player.startUsingItem(hand);

            return InteractionResultHolder.pass(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration"))
            return stack;

        var properties = getFoodProperties(stack, entity);

        if (properties == null)
            return stack;

        player.eat(level, stack.copy());

        var eaten = (int) Math.ceil(properties.nutrition() / getStatValue(stack, "regeneration", "feed"));

        if (eaten > 0) {
            addPieces(stack, -eaten);

            if (isLevelingSourceUnlocked(stack, "regeneration"))
                spreadRelicExperience(player, stack, eaten);
        }

        return stack;
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        if (!(entity instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration"))
            return super.getFoodProperties(stack, entity);

        var charge = getPieces(stack);

        if (charge == 0)
            return null;

        var nutrition = Math.min((int) Math.ceil(charge * getStatValue(stack, "regeneration", "feed")), 20 - player.getFoodData().getFoodLevel());

        var builder = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(nutrition / 3F);

        if (canPlayerUseAbility(player, stack, "marinade")) {
            var contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents != null)
                contents.forEachEffect(effect -> builder.effect(() -> new MobEffectInstance(effect.getEffect(), (int) (nutrition * getStatValue(stack, "marinade", "duration") * 20), effect.getAmplifier()), 1F));
        }

        return builder.build();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(DataComponents.POTION_CONTENTS) != null;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    public int getMaxPieces() {
        return 6;
    }

    public int getPieces(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(CHARGE, 0), 0, getMaxPieces());
    }

    public void setPieces(ItemStack stack, int amount) {
        stack.set(CHARGE, Math.clamp(amount, 0, getMaxPieces()));
    }

    public void addPieces(ItemStack stack, int amount) {
        setPieces(stack, getPieces(stack) + amount);
    }

    @EventBusSubscriber
    public static class InfinityHamEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getDirectEntity() instanceof Player player))
                return;

            var stack = player.getMainHandItem();

            if (!(stack.getItem() instanceof InfiniteHamItem relic) || !relic.canPlayerUseAbility(player, stack, "meat_bat"))
                return;

            var charge = relic.getPieces(stack);

            if (charge <= 0)
                return;

            if (relic.isLevelingSourceUnlocked(stack, "meat_bat"))
                relic.spreadRelicExperience(player, stack, charge);

            event.setAmount((float) (event.getAmount() + (relic.getStatValue(stack, "meat_bat", "damage") * charge)));
            event.getEntity().addEffect(new MobEffectInstance(EffectRegistry.STUN, (int) Math.round(relic.getStatValue(stack, "meat_bat", "stun") * charge * 20), 0));

            stack.set(CHARGE, 0);
        }

        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {
            if (event.getAction() != ClickAction.PRIMARY)
                return;

            var player = event.getEntity();

            var heldStack = event.getHeldStack();
            var slotStack = event.getSlotStack();

            if (!(heldStack.getItem() instanceof PotionItem) || !(slotStack.getItem() instanceof InfiniteHamItem relic)
                    || !relic.canPlayerUseAbility(player, slotStack, "marinade"))
                return;

            var contents = heldStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            var effects = StreamSupport.stream(contents.getAllEffects().spliterator(), false).toList();

            if (effects.isEmpty())
                slotStack.set(DataComponents.POTION_CONTENTS, null);
            else {
                slotStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects));

                if (relic.isLevelingSourceUnlocked(slotStack, "marinade"))
                    relic.spreadRelicExperience(player, slotStack, effects.size());
            }

            var bottle = new ItemStack(Items.GLASS_BOTTLE);

            if (player.containerMenu.getCarried().getCount() <= 1)
                player.containerMenu.setCarried(bottle);
            else {
                player.containerMenu.getCarried().shrink(1);

                EntityUtils.addItem(player, bottle);
            }

            player.playSound(SoundEvents.BOTTLE_FILL, 1F, 1F);

            event.setCanceled(true);
        }
    }
}