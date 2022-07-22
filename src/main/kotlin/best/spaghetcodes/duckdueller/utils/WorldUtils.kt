package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object WorldUtils {

    fun airInFront(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player.position, distance, EntityUtils.get2dLookVec(player))
    }

    fun airInBack(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(180f))
    }

    fun airOnLeft(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(90f))
        //return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(90f), 2, 2)
    }

    fun airOnRight(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(-90f))
        //return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(-90f), 2, 2)
    }

    fun airCheckAngle(player: EntityPlayer, distance: Float, angle: Float): Boolean {
        return airCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(angle))
    }

    private fun airCheck(pos: BlockPos, distance: Float, lookVec: Vec3): Boolean {
        for (i in 1..distance.toInt()) {
            if (DuckDueller.mc.theWorld.getBlockState(BlockPos(pos.x + lookVec.xCoord * i, pos.y - 0.2, pos.z + lookVec.zCoord * i)).block == Blocks.air) {
                return true
            }
        }
        return false
    }
    
}