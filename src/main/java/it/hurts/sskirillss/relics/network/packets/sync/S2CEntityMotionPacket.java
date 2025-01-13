package it.hurts.sskirillss.relics.network.packets.sync;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.utils.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class S2CEntityMotionPacket implements CustomPacketPayload {
    private final int id;

    private final double x;
    private final double y;
    private final double z;

    public S2CEntityMotionPacket(int id, Vec3 motion) {
        this(id, motion.x(), motion.y(), motion.z());
    }

    public static final Type<S2CEntityMotionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "entity_motion"));

    public static final StreamCodec<ByteBuf, S2CEntityMotionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CEntityMotionPacket::getId,
            ByteBufCodecs.DOUBLE, S2CEntityMotionPacket::getX,
            ByteBufCodecs.DOUBLE, S2CEntityMotionPacket::getY,
            ByteBufCodecs.DOUBLE, S2CEntityMotionPacket::getZ,
            S2CEntityMotionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var level = ctx.player().getCommandSenderWorld();

            var entity = level.getEntity(id);

            if (entity == null)
                return;

            entity.setDeltaMovement(x, y, z);
        });
    }
}