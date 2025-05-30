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

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BlockSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<BlockState?>(lock, true) {
    val occlusion = SectionOcclusion(this)
    var hasFluid = false
        private set

    init {
        recalculate(false)
    }

    override fun recalculate() {
        recalculate(true)
    }

    private fun recalculateFluid() {
        val data: Array<Any?> = data ?: return
        if (isEmpty) {
            this.hasFluid = false
            return
        }

        var hasFluid = false
        for (state in data) {
            if (state !is BlockState?) continue
            if (state.isFluid()) {
                hasFluid = true
                break
            }
        }
        this.hasFluid = hasFluid
    }

    fun recalculate(notify: Boolean) {
        super.recalculate()
        if (isEmpty) {
            hasFluid = false
            occlusion.clear(notify)
            return
        }
        recalculateFluid()

        occlusion.recalculate(notify)
    }

    fun noOcclusionSet(position: InSectionPosition, value: BlockState?): BlockState? {
        val previous = super.unsafeSet(position, value)
        val previousFluid = previous.isFluid()
        val valueFluid = value.isFluid()

        if (!previousFluid && valueFluid) {
            hasFluid = true
        } else if (previousFluid && !valueFluid) {
            recalculateFluid()
        }

        return previous
    }

    override fun unsafeSet(position: InSectionPosition, value: BlockState?): BlockState? {
        val previous = noOcclusionSet(position, value)

        occlusion.onSet(previous, value)

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        if (this == null) return false
        if (this.block is FluidHolder) {
            return true
        }
        return this.isWaterlogged()
    }
}
