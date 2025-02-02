package org.valkyrienskies.mod.mixin.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.game.ships.ShipObjectClient;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixin.client.render.MixinLevelRenderer;

@Mixin(Particle.class)
public abstract class MixinParticle {

    @Shadow
    public abstract void setPos(double x, double y, double z);

    @Shadow
    protected double xd;

    @Shadow
    protected double yd;

    @Shadow
    protected double zd;

    /**
     * See also {@link MixinLevelRenderer}
     */
    @Inject(
        method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V",
        at = @At("TAIL")
    )
    public void checkShipCoords(final ClientLevel world, final double x, final double y, final double z,
        final CallbackInfo ci) {
        final ShipObjectClient ship = VSGameUtilsKt.getShipObjectManagingPos(world, (int) x >> 4, (int) z >> 4);
        if (ship == null) {
            return;
        }

        // in-world position
        final Vector3d p = ship.getRenderTransform().getShipToWorldMatrix().transformPosition(new Vector3d(x, y, z));
        this.setPos(p.x, p.y, p.z);
    }

    /**
     * See also {@link MixinLevelRenderer}
     */
    @Inject(
        method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDD)V",
        at = @At("TAIL")
    )
    public void checkShipPosAndVelocity(final ClientLevel world, final double x, final double y, final double z,
        final double velocityX,
        final double velocityY, final double velocityZ, final CallbackInfo ci) {

        final ShipObjectClient ship = VSGameUtilsKt.getShipObjectManagingPos(world, (int) x >> 4, (int) z >> 4);
        if (ship == null) {
            return;
        }

        final Matrix4dc transform = ship.getRenderTransform().getShipToWorldMatrix();
        // in-world position
        final Vector3d p = transform.transformPosition(new Vector3d(x, y, z));
        // in-world velocity
        final Vector3d v = transform
            // Rotate velocity wrt ship transform
            .transformDirection(new Vector3d(this.xd, this.yd, this.zd))
            // Tack on the ships linear velocity (no angular velocity param unfortunately)
            .add(ship.getShipData().getPhysicsData().getLinearVelocity());

        this.setPos(p.x, p.y, p.z);
        this.xd = v.x;
        this.yd = v.y;
        this.zd = v.z;
    }

}
