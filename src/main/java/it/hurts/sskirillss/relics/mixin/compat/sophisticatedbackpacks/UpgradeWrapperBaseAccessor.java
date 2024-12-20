package it.hurts.sskirillss.relics.mixin.compat.sophisticatedbackpacks;

import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpgradeWrapperBase.class)
public interface UpgradeWrapperBaseAccessor {
    @Accessor("storageWrapper")
    IStorageWrapper getStorageWrapper();
}