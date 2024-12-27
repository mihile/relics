package it.hurts.sskirillss.relics.mixin.compat.sophisticatedbackpacks;

import it.hurts.sskirillss.relics.items.relics.InfiniteHamItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeedingUpgradeWrapper.class)
public class FeedingUpgradeWrapperMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(Entity entity, Level level, BlockPos pos, CallbackInfo ci) {
        if (!(entity instanceof Player))
            return;

        InventoryHelper.iterate(((UpgradeWrapperBaseAccessor) this).getStorageWrapper().getInventoryForUpgradeProcessing(), (slot, stack) -> {
            if (stack.getItem() instanceof InfiniteHamItem relic)
                relic.inventoryTick(stack, level, entity, slot, false);

            return true;
        }, () -> false, (result) -> false);
    }
}