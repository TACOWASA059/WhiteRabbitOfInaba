package com.github.tacowasa059.whiterabbitofinaba.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow
    abstract boolean func_242375_q();
    @Shadow
    abstract boolean isStayingOnGroundSurface();
    @Inject(method="maybeBackOffFromEdge",at=@At("HEAD"),cancellable = true)
    protected void maybeBackOffFromEdge(Vector3d p_225514_1_, MoverType p_225514_2_, CallbackInfoReturnable<Vector3d> cir) {
        PlayerEntity player = (PlayerEntity) (Object)this;

        if (!player.abilities.isFlying && (p_225514_2_ == MoverType.SELF || p_225514_2_ == MoverType.PLAYER) &&
                this.isStayingOnGroundSurface() && this.func_242375_q()) {
            double d0 = p_225514_1_.x;
            double d1 = p_225514_1_.z;

            boolean done = false;

            while(!done) {
                AxisAlignedBB axisAlignedBB = player.getBoundingBox().offset(d0, - player.stepHeight, 0.0);
                boolean intersect = isIntersect(axisAlignedBB, getLivingEntitiesWithinAABB(player,axisAlignedBB));
                while(d0 != 0.0 && player.world.hasNoCollisions(player, axisAlignedBB) && !intersect) {
                    if (d0 < 0.05 && d0 >= -0.05) {
                        d0 = 0.0;
                    } else if (d0 > 0.0) {
                        d0 -= 0.05;
                    } else {
                        d0 += 0.05;
                    }
                }

                while(!done) {
                    axisAlignedBB = player.getBoundingBox().offset(0.0, -player.stepHeight, d1);
                    intersect = isIntersect(axisAlignedBB, getLivingEntitiesWithinAABB(player,axisAlignedBB));
                    while(d1 != 0.0 && player.world.hasNoCollisions(player, axisAlignedBB) && !intersect) {
                        if (d1 < 0.05 && d1 >= -0.05) {
                            d1 = 0.0;
                        } else if (d1 > 0.0) {
                            d1 -= 0.05;
                        } else {
                            d1 += 0.05;
                        }
                    }

                    while(!done) {
                        axisAlignedBB = player.getBoundingBox().offset(d0, -player.stepHeight, d1);
                        intersect = isIntersect(axisAlignedBB, getLivingEntitiesWithinAABB(player,axisAlignedBB));
                        while(d0 != 0.0 && d1 != 0.0 && player.world.hasNoCollisions(player, axisAlignedBB) && !intersect) {
                            if (d0 < 0.05 && d0 >= -0.05) {
                                d0 = 0.0;
                            } else if (d0 > 0.0) {
                                d0 -= 0.05;
                            } else {
                                d0 += 0.05;
                            }

                            if (d1 < 0.05 && d1 >= -0.05) {
                                d1 = 0.0;
                            } else if (d1 > 0.0) {
                                d1 -= 0.05;
                            } else {
                                d1 += 0.05;
                            }
                        }

                        p_225514_1_ = new Vector3d(d0, p_225514_1_.y, d1);
                        done = true;
                    }
                }
            }
        }
        cir.setReturnValue(p_225514_1_);
        cir.cancel();
    }


    // onGround:trueの処理
    @Inject(method="func_242375_q",at=@At("HEAD"),cancellable = true)
    private void func_242375_q(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object)this;
        AxisAlignedBB axisAlignedBB = player.getBoundingBox().offset(0.0, player.fallDistance - player.stepHeight, 0.0);
        boolean isIntersect = isIntersect(axisAlignedBB, getLivingEntitiesWithinAABB(player,axisAlignedBB));
        cir.setReturnValue(player.isOnGround() || player.fallDistance < player.stepHeight && (!player.world.hasNoCollisions(player, axisAlignedBB)||isIntersect));
        cir.cancel();
    }
    private boolean isIntersect(AxisAlignedBB axisAlignedBB, List<?extends Entity> entities){
        for(Entity entity : entities){
            if(axisAlignedBB.intersects(entity.getBoundingBox())){
                return true;
            }
        }
        return false;
    }

    private List<LivingEntity> getLivingEntitiesWithinAABB(Entity  entity, AxisAlignedBB axisAlignedBB){
        return entity.world.getEntitiesWithinAABB(LivingEntity.class, axisAlignedBB)
                .stream().filter(entity2 -> entity2!=entity).collect(Collectors.toList());
    }
}
