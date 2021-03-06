package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
     * @param center If true, returns values to look at the player's face, if false, returns the values to look at the closest point in the hitbox
     * @return float[] - {yaw, pitch}
     */
    fun getRotations(player: EntityPlayer?, target: Entity?, raw: Boolean, center: Boolean = false): FloatArray? {
        return if (target == null || player == null) {
            null
        } else {
            var pos: Vec3? = null
            if (center) {
                pos = Vec3(target.posX, target.posY + target.eyeHeight, target.posZ)
            } else {
                val box = target.entityBoundingBox

                // get the four corners of the hitbox
                var yPos = player.posY + player.eyeHeight

                if (!player.onGround) {
                    yPos = target.posY + target.eyeHeight
                } else if (abs(target.posY - player.posY) > player.eyeHeight) {
                    yPos = target.posY + target.eyeHeight / 2f
                }

                val corner1 = Vec3(box.minX, yPos, box.minZ)
                val corner2 = Vec3(box.maxX, yPos, box.minZ)
                val corner3 = Vec3(box.minX, yPos, box.maxZ)
                val corner4 = Vec3(box.maxX, yPos, box.maxZ)

                // get the closest 2 corners
                val closest = getClosestCorner(corner1, corner2, corner3, corner4)
                var a = closest[0]
                var b = closest[1]

                val p = Vec3(player.posX, player.posY + player.eyeHeight, player.posZ)

                // since the two corners are either always on the same X or same Z position, we don't need complicated math
                if (a.zCoord == b.zCoord) {
                    if (a.xCoord > b.xCoord) {
                        val temp = a
                        a = b
                        b = temp
                    }
                    if (p.xCoord < a.xCoord) {
                        pos = a
                    } else if (p.xCoord > b.xCoord) {
                        pos = b
                    } else {
                        pos = Vec3(p.xCoord, a.yCoord, a.zCoord)
                    }
                } else {
                    if (a.zCoord > b.zCoord) {
                        val temp = a
                        a = b
                        b = temp
                    }
                    if (p.zCoord < a.zCoord) {
                        pos = a
                    } else if (p.zCoord > b.zCoord) {
                        pos = b
                    } else {
                        pos = Vec3(a.xCoord, a.yCoord, p.zCoord)
                    }
                }
            }

            val diffX = pos.xCoord - player.posX
            val diffY: Double = pos.yCoord - (player.posY + player.getEyeHeight().toDouble())
            val diffZ = pos.zCoord - player.posZ
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            val yaw = (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793)).toFloat()

            if (crossHairDistance(yaw, pitch, player) > 2) {
                if (raw) {
                    floatArrayOf(
                        MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                        MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
                    )
                } else floatArrayOf(
                    player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                    player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
                )
            } else {
                if (raw) {
                    floatArrayOf(
                        0F, 0F
                    )
                } else {
                    floatArrayOf(
                        player.rotationYaw,
                        player.rotationPitch
                    )
                }
            }
        }
    }

    fun crossHairDistance(yaw: Float, pitch: Float, player: EntityPlayer): Float {
        val nYaw = yaw - player.rotationYaw - yaw
        val nPitch = pitch - player.rotationPitch - pitch
        return MathHelper.sqrt_float(nYaw * nYaw + nPitch * nPitch)
    }

    fun getDistanceNoY(player: EntityPlayer?, target: Entity?): Float {
        return if (target == null || player == null) {
            0f
        } else {
            val diffX = player.posX - target.posX
            val diffZ = player.posZ - target.posZ
            MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toFloat()
        }
    }

    fun get2dLookVec(entity: Entity): Vec3 {
        val yaw = ((entity.rotationYaw + 90)  * Math.PI) / 180
        return Vec3(cos(yaw), 0.0, sin(yaw))
    }

    private fun getClosestCorner(corner1: Vec3, corner2: Vec3, corner3: Vec3, corner4: Vec3): ArrayList<Vec3> {
        val pos = Vec3(DuckDueller.mc.thePlayer.posX, DuckDueller.mc.thePlayer.posY + DuckDueller.mc.thePlayer.eyeHeight, DuckDueller.mc.thePlayer.posZ)

        val smallest = arrayListOf(corner1, corner2, corner3, corner4)
        smallest.sortBy { abs(pos.distanceTo(it)) }

        return arrayListOf(smallest[0], smallest[1])
    }

}