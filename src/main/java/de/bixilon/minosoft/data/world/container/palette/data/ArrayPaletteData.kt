/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.container.palette.data

import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16

class ArrayPaletteData(
    val versionId: Int,
    val elementBits: Int,
    override val size: Int,
) : PaletteData {
    private lateinit var data: LongArray

    init {
        check(elementBits in 0..32)
    }

    override fun read(buffer: PlayInByteBuffer) {
        buffer.readVarInt() // minecraft ignores the length prefix
        val longs: Int = if (versionId < V_1_16) { // ToDo: When did this changed? is just a guess
            val bits = size * elementBits

            (bits + (Long.SIZE_BITS - 1)) / Long.SIZE_BITS // divide up
        } else {
            val elementsPerLong = Long.SIZE_BITS / elementBits
            (size + elementsPerLong - 1) / elementsPerLong
        }
        data = buffer.readLongArray(longs)
    }

    override operator fun get(index: Int): Int {
        val individualValueMask = (1 shl elementBits) - 1

        var blockId: Long = if (versionId < V_1_16) { // ToDo: When did this changed? is just a guess
            val startLong = index * elementBits / Long.SIZE_BITS
            val startOffset = index * elementBits % Long.SIZE_BITS
            val endLong = ((index + 1) * elementBits - 1) / Long.SIZE_BITS

            if (startLong == endLong) {
                data[startLong] ushr startOffset
            } else {
                val endOffset = Long.SIZE_BITS - startOffset
                data[startLong] ushr startOffset or (data[endLong] shl endOffset)
            }
        } else {
            val startLong = index / (Long.SIZE_BITS / elementBits)
            val startOffset = index % (Long.SIZE_BITS / elementBits) * elementBits
            data[startLong] ushr startOffset
        }

        blockId = blockId and individualValueMask.toLong()

        return blockId.toInt()
    }
}
