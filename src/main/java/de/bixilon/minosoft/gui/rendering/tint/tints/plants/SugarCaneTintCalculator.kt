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

package de.bixilon.minosoft.gui.rendering.tint.tints.plants

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.tints.grass.GrassTintCalculator

class SugarCaneTintCalculator(val grassTintCalculator: GrassTintCalculator) : TintProvider {

    override fun getBlockColor(state: BlockState, biome: Biome?, position: BlockPosition, tintIndex: Int): Int {
        return grassTintCalculator.getBlockColor(state, biome, position, tintIndex)
    }

    override fun getItemColor(stack: ItemStack, tintIndex: Int): Int {
        return Colors.WHITE_RGB
    }
}
