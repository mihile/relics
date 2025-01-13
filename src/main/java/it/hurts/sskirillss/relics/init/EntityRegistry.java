package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.entities.*;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reference.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShadowGlaiveEntity>> SHADOW_GLAIVE = ENTITIES.register("shadow_glaive", () ->
            EntityType.Builder.<ShadowGlaiveEntity>of(ShadowGlaiveEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.1F)
                    .build("shadow_glaive")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BlockSimulationEntity>> BLOCK_SIMULATION = ENTITIES.register("block_simulation", () ->
            EntityType.Builder.<BlockSimulationEntity>of(BlockSimulationEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .build("block_simulation")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ShockwaveEntity>> SHOCKWAVE = ENTITIES.register("shockwave", () ->
            EntityType.Builder.<ShockwaveEntity>of(ShockwaveEntity::new, MobCategory.MISC)
                    .sized(1F, 0.1F)
                    .build("shockwave")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<LifeEssenceEntity>> LIFE_ESSENCE = ENTITIES.register("life_essence", () ->
            EntityType.Builder.<LifeEssenceEntity>of(LifeEssenceEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("life_essence")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<StalactiteEntity>> STALACTITE = ENTITIES.register("stalactite", () ->
            EntityType.Builder.<StalactiteEntity>of(StalactiteEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .build("stalactite")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DissectionEntity>> DISSECTION = ENTITIES.register("dissection", () ->
            EntityType.Builder.<DissectionEntity>of(DissectionEntity::new, MobCategory.MISC)
                    .sized(3F, 3F)
                    .build("dissection")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SporeEntity>> SPORE = ENTITIES.register("spore", () ->
            EntityType.Builder.<SporeEntity>of(SporeEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .build("spore")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SolidSnowballEntity>> SOLID_SNOWBALL = ENTITIES.register("solid_snowball", () ->
            EntityType.Builder.<SolidSnowballEntity>of(SolidSnowballEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("solid_snowball")
    );

//    public static final DeferredHolder<EntityType<?>, EntityType<ArrowRainEntity>> ARROW_RAIN = ENTITIES.register("arrow_rain", () ->
//            EntityType.Builder.<ArrowRainEntity>of(ArrowRainEntity::new, MobCategory.MISC)
//                    .sized(1F, 1F)
//                    .build("arrow_rain")
//    );

    public static final DeferredHolder<EntityType<?>, EntityType<RelicExperienceOrbEntity>> RELIC_EXPERIENCE_ORB = ENTITIES.register("relic_experience_orb", () ->
            EntityType.Builder.of(RelicExperienceOrbEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .build("relic_experience_orb")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownRelicExperienceBottle>> THROWN_RELIC_EXPERIENCE_BOTTLE = ENTITIES.register("thrown_relic_experience_bottle", () ->
            EntityType.Builder.of(ThrownRelicExperienceBottle::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("thrown_relic_experience_bottle")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DeathEssenceEntity>> DEATH_ESSENCE = ENTITIES.register("death_essence", () ->
            EntityType.Builder.<DeathEssenceEntity>of(DeathEssenceEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("death_essence")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ChairEntity>> CHAIR = ENTITIES.register("chair", () ->
            EntityType.Builder.<ChairEntity>of(ChairEntity::new, MobCategory.MISC)
                    .sized(0F, 0F)
                    .build("chair")
    );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}