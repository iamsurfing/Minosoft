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

package de.bixilon.minosoft.data.registries.blocks.types.properties.offset

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE

interface RandomOffsetBlock : OffsetBlock {
    val randomOffset: RandomOffsetTypes? // TODO: make non nullable


    override fun offsetShape(position: BlockPosition): Vec3 {
        val offset = this.randomOffset ?: return Vec3.EMPTY_INSTANCE
        return position.getWorldOffset(offset)
    }

    override fun offsetModel(position: BlockPosition): Vec3 = offsetShape(position)
}
