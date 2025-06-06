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
package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.state.error.StatelessBlockError
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.container.block.SectionOcclusion.Companion._isFullyOpaque
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender

open class BlockState(
    @JvmField val block: Block,
    val luminance: Int,
) {
    @JvmField var model: BlockRender? = null
    val flags = BlockStateFlags.set()

    init {
        if (_isFullyOpaque()) {
            flags += BlockStateFlags.FULLY_OPAQUE
        }
    }

    constructor(block: Block, settings: BlockStateSettings) : this(block, settings.luminance)


    override fun hashCode(): Int {
        return block.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is ResourceLocation) return other == block.identifier
        if (other is BlockState) return other.block == block && other.luminance == luminance
        return false
    }

    override fun toString(): String {
        return block.toString()
    }

    open fun withProperties(vararg properties: Pair<BlockProperty<*>, Any>): BlockState {
        if (properties.isEmpty()) return this
        throw StatelessBlockError(this)
    }

    open fun withProperties(properties: Map<BlockProperty<*>, Any>): BlockState {
        if (properties.isEmpty()) return this
        throw StatelessBlockError(this)
    }

    open fun cycle(property: BlockProperty<*>): BlockState = throw StatelessBlockError(this)

    open operator fun <T> get(property: BlockProperty<T>): T = throw StatelessBlockError(this)
    open fun <T> getOrNull(property: BlockProperty<T>): T? = throw StatelessBlockError(this)
}
