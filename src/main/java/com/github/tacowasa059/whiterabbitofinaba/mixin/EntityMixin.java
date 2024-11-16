package com.github.tacowasa059.whiterabbitofinaba.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.stream.Stream;

import static net.minecraft.entity.Entity.collideBoundingBoxHeuristically;
import static net.minecraft.entity.Entity.horizontalMag;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method="getAllowedMovement(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;"
            ,at=@At("HEAD"),cancellable = true)
    private void getAllowedMovement(Vector3d p_213306_1_, CallbackInfoReturnable<Vector3d> cir) {
        Entity entity = (Entity)(Object)this;
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        ISelectionContext iselectioncontext = ISelectionContext.forEntity(entity);
        VoxelShape voxelshape = entity.world.getWorldBorder().getShape();

        Stream<VoxelShape> stream = VoxelShapes.compare(voxelshape, VoxelShapes.create(axisalignedbb.shrink(1.0E-7)),
                IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        Stream<VoxelShape> stream1 = entity.world.func_230318_c_(entity, axisalignedbb.expand(p_213306_1_), (p_233561_0_) -> true);

        AxisAlignedBB axisAlignedBB = axisalignedbb.expand(p_213306_1_);

        Stream<VoxelShape> stream2 = entity.world.getEntitiesWithinAABB(LivingEntity.class, axisAlignedBB,
                        Objects::nonNull)
                .stream()
                .filter(entity2 -> entity2!=entity)
                .map(entity2 -> VoxelShapes.create(entity2.getBoundingBox()));

        ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(Stream.concat(stream1, stream),stream2));
        Vector3d vector3d = p_213306_1_.lengthSquared() == 0.0 ? p_213306_1_ :
                collideBoundingBoxHeuristically(entity, p_213306_1_, axisalignedbb, entity.world, iselectioncontext, reuseablestream);

        // 階段のステップなどの処理
        boolean flag = p_213306_1_.x != vector3d.x;
        boolean flag1 = p_213306_1_.y != vector3d.y;
        boolean flag2 = p_213306_1_.z != vector3d.z;
        boolean flag3 = entity.isOnGround() || flag1 && p_213306_1_.y < 0.0;
        if (entity.stepHeight > 0.0F && flag3 && (flag || flag2)) {
            Vector3d vector3d1 =
                    collideBoundingBoxHeuristically(entity, new Vector3d(p_213306_1_.x,
                            entity.stepHeight, p_213306_1_.z), axisalignedbb, entity.world, iselectioncontext, reuseablestream);
            Vector3d vector3d2 = collideBoundingBoxHeuristically(entity,
                    new Vector3d(0.0, entity.stepHeight, 0.0),
                    axisalignedbb.expand(p_213306_1_.x, 0.0, p_213306_1_.z), entity.world, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double)entity.stepHeight) {
                Vector3d vector3d3 = collideBoundingBoxHeuristically(entity,
                        new Vector3d(p_213306_1_.x, 0.0, p_213306_1_.z), axisalignedbb.offset(vector3d2),
                        entity.world, iselectioncontext, reuseablestream).add(vector3d2);
                if (horizontalMag(vector3d3) > horizontalMag(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if (horizontalMag(vector3d1) > horizontalMag(vector3d)) {
                cir.setReturnValue(vector3d1.add(collideBoundingBoxHeuristically(entity,
                        new Vector3d(0.0, -vector3d1.y + p_213306_1_.y, 0.0),
                        axisalignedbb.offset(vector3d1), entity.world, iselectioncontext, reuseablestream)));
            }
        }

        cir.setReturnValue(vector3d);
        cir.cancel();
    }
}
