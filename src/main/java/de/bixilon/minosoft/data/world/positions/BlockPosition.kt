/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.assertPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.generatePositionHash
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class BlockPosition(
    inline val index: Long,
) : TextFormattable {

    init {
        TODO()
    }

    constructor() : this(0, 0, 0)
    constructor(x: Int, y: Int, z: Int) : this((y and 0xFFF shl SHIFT_Y) or (z shl SHIFT_Z) or (x shl SHIFT_X)) {
        assertPosition(x, 0, ProtocolDefinition.SECTION_MAX_X)
        assertPosition(y, ProtocolDefinition.CHUNK_MIN_Y, ProtocolDefinition.CHUNK_MAX_Y)
        assertPosition(z, 0, ProtocolDefinition.SECTION_MAX_Z)
    }
    constructor(position: InChunkPosition) : this(position.x, position.y, position.z)
    constructor(position: InSectionPosition) : this(position.x, position.y, position.z)

    inline val x: Int get() = (index and MASK_X) shr SHIFT_X
    inline val y: Int get() = (index and MASK_Y) shr SHIFT_Y
    inline val z: Int get() = (index and MASK_Z) shr SHIFT_Z
    inline val xz: Int get() = (index and MASK_Z or MASK_X)


    inline fun plusX(): BlockPosition {
        assertPosition(this.x < ProtocolDefinition.SECTION_MAX_X)
        return BlockPosition(index + X * 1)
    }

    inline fun plusX(x: Int): BlockPosition {
        assertPosition(this.x + x, 0, ProtocolDefinition.SECTION_MAX_X)
        return BlockPosition(index + X * x)
    }

    inline fun minusX(): BlockPosition {
        assertPosition(this.x > 0)
        return BlockPosition(index - X * 1)
    }

    inline fun plusY(): BlockPosition {
        assertPosition(this.y < ProtocolDefinition.CHUNK_MAX_Y)
        return BlockPosition(index + Y * 1)
    }

    inline fun plusY(y: Int): BlockPosition {
        assertPosition(this.y + y, ProtocolDefinition.CHUNK_MIN_Y, ProtocolDefinition.CHUNK_MAX_Y)
        return BlockPosition(index + Y * y)
    }

    inline fun minusY(): BlockPosition {
        assertPosition(this.y > ProtocolDefinition.CHUNK_MIN_Y)
        return BlockPosition(index - Y * 1)
    }

    inline fun plusZ(): BlockPosition {
        assertPosition(this.z < ProtocolDefinition.SECTION_MAX_Z)
        return BlockPosition(index + Z * 1)
    }

    inline fun plusZ(z: Int): BlockPosition {
        assertPosition(this.z + z, 0, ProtocolDefinition.SECTION_MAX_Z)
        return BlockPosition(index + Z * z)
    }

    inline fun minusZ(): BlockPosition {
        assertPosition(this.z > 0)
        return BlockPosition(index - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPosition(x, y, z)

    inline operator fun plus(value: Int) = BlockPosition(this.x + value, this.y + value, this.z + value)
    inline operator fun minus(value: Int) = BlockPosition(this.x - value, this.y - value, this.z - value)
    inline operator fun times(value: Int) = BlockPosition(this.x * value, this.y * value, this.z * value)
    inline operator fun div(value: Int) = BlockPosition(this.x / value, this.y / value, this.z / value)

    inline operator fun plus(position: BlockPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun plus(position: InChunkPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun plus(position: InSectionPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun minus(position: BlockPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)
    inline operator fun minus(position: InChunkPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)
    inline operator fun minus(position: InSectionPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)

    inline operator fun unaryMinus() = BlockPosition(-this.x, -this.y, -this.z)
    inline operator fun unaryPlus() = this

    inline operator fun plus(direction: Directions) = BlockPosition(this.x + direction.vector.x, this.y + direction.vector.y, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = BlockPosition(this.x - direction.vector.x, this.y - direction.vector.y, this.z - direction.vector.z)

    inline infix fun and(mask: Int) = BlockPosition(x and mask, y and mask, z and mask)

    inline val hash get() = generatePositionHash(x, y, z)
    inline val sectionHeight get() = y.sectionHeight
    inline val chunkPosition get() = ChunkPosition(x shr 4, z shr 4)
    inline val inChunkPosition get() = InChunkPosition(x and 0x0F, y, this.z and 0x0F)
    inline val inSectionPosition get() = InSectionPosition(x and 0x0F, y.inSectionHeight, z and 0x0F)

    override fun toText() = "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
    override fun toString() = "b($x $y $z)"

    companion object {
        const val MASK_X = 0x00F
        const val SHIFT_X = 0

        const val MASK_Z = 0x0F0
        const val SHIFT_Z = 4

        const val MASK_Y = 0xFFF00
        const val SHIFT_Y = 8

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z
        const val Y = 1 shl SHIFT_Y


        val EMPTY = BlockPosition(0, 0, 0)


        fun of(chunk: ChunkPosition, sectionHeight: Int): BlockPosition {
            return BlockPosition(
                chunk.x * ProtocolDefinition.SECTION_WIDTH_X,
                sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y,
                chunk.z * ProtocolDefinition.SECTION_WIDTH_Z
            ) // ToDo: Confirm
        }

        fun of(chunk: ChunkPosition, sectionHeight: Int, inSection: InSectionPosition): BlockPosition {
            return BlockPosition(
                chunk.x * ProtocolDefinition.SECTION_WIDTH_X + inSection.x,
                sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inSection.y,
                chunk.z * ProtocolDefinition.SECTION_WIDTH_Z + inSection.z
            ) // ToDo: Confirm
        }
    }
}
