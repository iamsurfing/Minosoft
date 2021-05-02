/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.nodes

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.layout.AbsoluteLayout

abstract class HUDElement(protected val hudRenderer: HUDRenderer) {
    val layout = AbsoluteLayout(hudRenderer.renderWindow)

    lateinit var properties: HUDElementProperties

    val scale: Float
        get() = properties.scale * Minosoft.getConfig().config.game.hud.scale

    open fun init() {}
    open fun postInit() {}
    open fun draw() {}
}
