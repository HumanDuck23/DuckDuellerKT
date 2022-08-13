package best.spaghetcodes.duckdueller.utils

import net.minecraft.util.Vec3

object Extensions {

    fun Vec3.scale (x: Double): Vec3 {
        return Vec3(this.xCoord * x, this.yCoord * x, this.zCoord * x)
    }

}