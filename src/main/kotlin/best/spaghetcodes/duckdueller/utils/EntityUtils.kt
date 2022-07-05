package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper

object EntityUtils {

    fun getOpponentEntity(): EntityPlayer? {
        if (DuckDueller.mc.theWorld != null) {
            for (entity in DuckDueller.mc.theWorld.playerEntities) {
                if (entity.displayName != DuckDueller.mc.thePlayer.displayName && shouldTarget(entity)) {
                    return entity
                }
            }
        }
        return null
    }

    fun shouldTarget(entity: EntityPlayer?): Boolean {
        return if (entity == null) {
            false
        } else if (DuckDueller.mc.thePlayer.isEntityAlive && entity.isEntityAlive) {
            if (!entity.isInvisible /*&& !entity.isInvisibleToPlayer(mc.thePlayer)*/) {
                if (DuckDueller.mc.thePlayer.getDistanceToEntity(entity) > 64.0f) {
                    false
                } else {
                    DuckDueller.mc.thePlayer.canEntityBeSeen(entity)
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * Get the rotations needed to look at an entity
     *
     * *Not originally my code, but I forgot where I found it.*
     *
     * @param target target entity
     * @param raw If true, only returns difference in yaw and pitch instead of values needed
     * @return float[] - {yaw, pitch}
     */
    fun getRotations(player: EntityPlayer?, target: Entity?, raw: Boolean): FloatArray? {
        return if (target == null || player == null) {
            null
        } else {
            val diffX = target.posX - player.posX
            val diffY: Double = if (target is EntityLivingBase) {
                target.posY + target.eyeHeight.toDouble() - (player.posY + player.getEyeHeight().toDouble())
            } else {
                (target.entityBoundingBox.minY + target.entityBoundingBox.maxY) / 2.0 - (player.posY + player.getEyeHeight().toDouble())
            }
            val diffZ = target.posZ - player.posZ
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            val yaw = (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793)).toFloat()
            if (raw) {
                floatArrayOf(
                    MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                    MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
                )
            } else floatArrayOf(
                player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
            )
        }
    }

}