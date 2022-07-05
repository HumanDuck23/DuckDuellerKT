package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object WorldUtils {

    /**
     * Check if the block in front of the player is air
     * @param player
     * @return boolean
     */
    fun isAirInFront(player: EntityPlayer, blocks: Float): Boolean {
        // get the block in front of the player
        val pos = BlockPos(player.posX, player.posY - 0.5, player.posZ)
        val lookVecScaled = Vec3(player.lookVec.xCoord * (blocks + 1), 0.0, player.lookVec.zCoord * (blocks + 1))
        return checkAir(lookVecScaled, pos)
    }

    /**
     * Check if the block in front of the player is air
     * @param player
     * @return boolean
     */
    fun isAirOnLeft(player: EntityPlayer, blocks: Float): Boolean {
        // get the block in front of the player
        val pos = BlockPos(player.posX, player.posY - 0.5, player.posZ)
        var lookVecScaled = Vec3(player.lookVec.xCoord * (blocks + 1), 0.0, player.lookVec.zCoord * (blocks + 1))
        lookVecScaled = lookVecScaled.rotateYaw(90f)
        return checkAir(lookVecScaled, pos)
    }

    /**
     * Check if the block in front of the player is air
     * @param player
     * @return boolean
     */
    fun isAirOnRight(player: EntityPlayer, blocks: Float): Boolean {
        // get the block in front of the player
        val pos = BlockPos(player.posX, player.posY - 0.5, player.posZ)
        var lookVecScaled = Vec3(player.lookVec.xCoord * (blocks + 1), 0.0, player.lookVec.zCoord * (blocks + 1))
        lookVecScaled = lookVecScaled.rotateYaw(-90f)
        return checkAir(lookVecScaled, pos)
    }

    private fun checkAir(lookVec: Vec3, pos: BlockPos): Boolean {
        val checkRadius = 2
        for (i in 0 until checkRadius) {
            if (subCheckAir(lookVec, pos, i)) {
                return true
            }
        }
        for (i in 0 downTo -checkRadius + 1) {
            if (subCheckAir(lookVec, pos, i)) {
                return true
            }
        }
        return false
    }

    private fun subCheckAir(lookVec: Vec3, pos: BlockPos, i: Int): Boolean {
        val newVec = lookVec.rotateYaw(i.toFloat())
        val pos2 = pos.add(newVec.xCoord, newVec.yCoord, newVec.zCoord)
        val block: Block = DuckDueller.mc.theWorld.getBlockState(pos2).getBlock()
        return block == Blocks.air
    }
    
}