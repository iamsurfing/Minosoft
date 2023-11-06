/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer

class EntityScoreboardFeature(renderer: PlayerRenderer<*>) : BillboardTextFeature(renderer, null) {

    override fun update(millis: Long, delta: Float) {
        updateScore()
        renderer.name.offset = if (this.text != null) NAME_OFFSET else DEFAULT_OFFSET
        super.update(millis, delta)
    }

    private fun updateScore() {
        if (!renderScore()) {
            this.text = null
            return
        }
        // TODO: cache score (just update every x time, listen for events, ...)
        this.text = getScore()
    }

    private fun renderScore(): Boolean {
        if (renderer.distance > RENDER_DISTANCE * RENDER_DISTANCE) return false
        val renderer = renderer.renderer
        val profile = renderer.profile.features.score
        if (!profile.enabled) return false
        if (this.renderer.entity === renderer.connection.camera.entity && (!renderer.context.camera.view.view.renderSelf || !profile.local)) return false

        return true
    }

    private fun getScore(): ChatComponent? {
        val objective = renderer.renderer.connection.scoreboard.positions[ScoreboardPositions.BELOW_NAME] ?: return null
        val score = objective.scores[renderer.entity.unsafeCast<PlayerEntity>().additional.name] ?: return null
        val text = BaseComponent()
        text += TextComponent(score.value)
        text += " "
        text += objective.displayName

        return text
    }

    companion object {
        const val RENDER_DISTANCE = 10
        val NAME_OFFSET = DEFAULT_OFFSET + PROPERTIES.lineHeight * BillboardTextMesh.SCALE
    }
}
