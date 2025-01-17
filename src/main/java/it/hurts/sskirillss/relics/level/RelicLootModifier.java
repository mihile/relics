package it.hurts.sskirillss.relics.level;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.init.LootCodecRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifier extends LootModifier {
    public static final Supplier<MapCodec<RelicLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, RelicLootModifier::new)));

    public static final Multimap<String, LootEntryCache> LOOT_ENTRIES = HashMultimap.create();

    public RelicLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static final float CHANCE = 0.35F;

    @Nonnull
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var id = context.getQueriedLootTableId().toString();
        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        var pos = context.getParamOrNull(LootContextParams.ORIGIN);

        var random = context.getRandom();

        if (!LOOT_ENTRIES.containsKey(id) || pos == null || entity == null || random.nextFloat() > CHANCE)
            return generatedLoot;

        var blockPos = new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z());
        var level = entity.level();

        List<Item> entries = new ArrayList<>();

        loop:

        for (var cache : LOOT_ENTRIES.get(id)) {
            for (var dimension : cache.getDimensions()) {
                if (!level.dimension().location().toString().equals(dimension))
                    continue;

                for (var biome : cache.getBiomes()) {
                    if (!level.getBiome(blockPos).is(ResourceLocation.parse(biome)))
                        continue;

                    entries.add(cache.getItem());

                    continue loop;
                }
            }
        }

        if (!entries.isEmpty())
            generatedLoot.add(entries.get(random.nextInt(entries.size())).getDefaultInstance());

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

        LOOT_ENTRIES.entries().removeIf(entry -> entry.getValue().getItem().equals(relic.getItem()));

        var registries = server.reloadableRegistries();

        var dimensionKeys = registries.getKeys(Registries.DIMENSION_TYPE);
        var biomeKeys = registries.getKeys(Registries.BIOME);
        var tableKeys = registries.getKeys(Registries.LOOT_TABLE);

        for (var entry : relic.getLootData().getEntries())
            for (var table : filterRegex(tableKeys, entry.getTables()))
                LOOT_ENTRIES.put(table, new LootEntryCache(filterRegex(dimensionKeys, entry.getDimensions()), filterRegex(biomeKeys, entry.getBiomes()), relic.getItem()));
    }

    private static List<String> filterRegex(Collection<ResourceLocation> keys, List<String> patterns) {
        List<String> entries = new ArrayList<>();

        for (var pattern : patterns) {
            for (var key : keys) {
                var id = key.toString();
                boolean isValid;

                try {
                    isValid = id.matches(pattern);
                } catch (PatternSyntaxException exception) {
                    isValid = id.equals(pattern);
                }

                if (isValid)
                    entries.add(id);
            }
        }

        return entries;
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
    @AllArgsConstructor
    public static class LootEntryCache {
        private List<String> dimensions;
        private List<String> biomes;

        private Item item;
    }
}