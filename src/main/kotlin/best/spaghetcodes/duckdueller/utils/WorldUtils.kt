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
        return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(90f), 30, 60)
    }

    fun airOnRight(player: EntityPlayer, distance: Float): Boolean {
        return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(-90f), 30, 60)
    }

    // Circular air check - rotates lookVec a little to find air that's not right next to the player
    // rotates in steps of 5
    private fun circleAirCheck(pos: BlockPos, distance: Float, lookVec: Vec3, rF: Int, rB: Int): Boolean {
        for (i in 0..rF step 5) {
            val nLookVec = lookVec.rotateYaw(i.toFloat())
            if (airCheck(pos, distance, nLookVec)) {
                return true
            }
        }
        for (i in 0 downTo -rB step 5) {
            val nLookVec = lookVec.rotateYaw(i.toFloat())
            if (airCheck(pos, distance, nLookVec)) {
                return true
            }
        }
        return false
    }

    private fun airCheck(pos: BlockPos, distance: Float, lookVec: Vec3): Boolean {
        return DuckDueller.mc.theWorld.getBlockState(BlockPos(pos.x + lookVec.xCoord * 2, pos.y - 0.2, pos.z + lookVec.zCoord * 2)).block == Blocks.air
    }
    
}