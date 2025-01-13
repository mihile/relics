package it.hurts.sskirillss.relics.level;

import com.google.common.base.Suppliers;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.init.LootCodecRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifier extends LootModifier {
    public static final Supplier<MapCodec<RelicLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, RelicLootModifier::new)));

    public static final Multimap<IRelicItem, LootEntryCache> LOOT_ENTRIES = LinkedHashMultimap.create();

    public RelicLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        var pos = context.getParamOrNull(LootContextParams.ORIGIN);

        if (pos == null || entity == null)
            return generatedLoot;

        var blockPos = new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z());
        var level = entity.level();

        loop:

        for (var entry : LOOT_ENTRIES.entries()) {
            var item = entry.getKey().getItem();

            if (item == null)
                continue;

            var cache = entry.getValue();

            if (context.getRandom().nextFloat() > cache.getChance())
                continue;

            for (var dimension : cache.getDimensions()) {
                if (!level.dimension().location().toString().equals(dimension))
                    continue;

                for (var biome : cache.getBiomes()) {
                    if (!level.getBiome(blockPos).is(ResourceLocation.parse(biome)))
                        continue;

                    for (var table : cache.getTables()) {
                        if (!table.equals(context.getQueriedLootTableId().toString()))
                            continue;

                        generatedLoot.add(item.getDefaultInstance());

                        continue loop;
                    }
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return LootCodecRegistry.RELIC_LOOT.get();
    }

    public static void processRelicCache(IRelicItem relic) {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null)
            return;

        LOOT_ENTRIES.removeAll(relic);

        for (var entry : relic.getLootData().getEntries()) {
            List<String> dimensions = new ArrayList<>();

            for (var dimension : entry.getDimensions()) {
                for (var candidate : server.reloadableRegistries().getKeys(Registries.DIMENSION_TYPE)) {
                    var id = candidate.toString();

                    boolean isValid;

                    try {
                        isValid = id.matches(dimension);
                    } catch (PatternSyntaxException exception) {
                        isValid = id.equals(dimension);
                    }

                    if (isValid)
                        dimensions.add(id);
                }
            }

            List<String> biomes = new ArrayList<>();

            for (var biome : entry.getBiomes()) {
                for (var candidate : server.reloadableRegistries().getKeys(Registries.BIOME)) {
                    var id = candidate.toString();

                    boolean isValid;

                    try {
                        isValid = id.matches(biome);
                    } catch (PatternSyntaxException exception) {
                        isValid = id.equals(biome);
                    }

                    if (isValid)
                        biomes.add(id);
                }
            }

            List<String> tables = new ArrayList<>();

            for (var table : entry.getTables()) {
                for (var candidate : server.reloadableRegistries().getKeys(Registries.LOOT_TABLE)) {
                    var id = candidate.toString();

                    boolean isValid;

                    try {
                        isValid = id.matches(table);
                    } catch (PatternSyntaxException exception) {
                        isValid = id.equals(table);
                    }

                    if (isValid)
                        tables.add(id);
                }
            }

            LOOT_ENTRIES.put(relic, new LootEntryCache(dimensions, biomes, tables, entry.getChance()));
        }
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            if (!LOOT_ENTRIES.isEmpty())
                return;

            for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                if (!(entry.getValue() instanceof IRelicItem relic))
                    continue;

                processRelicCache(relic);
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LootEntryCache {
        private List<String> dimensions = new ArrayList<>();
        private List<String> biomes = new ArrayList<>();
        private List<String> tables = new ArrayList<>();

        private float chance = 1F;
    }
}