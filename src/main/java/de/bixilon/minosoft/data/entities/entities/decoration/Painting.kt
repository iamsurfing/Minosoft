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
package de.bixilon.minosoft.data.entities.entities.decoration

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.Motif
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.entityPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class Painting(
    session: PlaySession,
    entityType: EntityType,
    data: EntityData,
    position: BlockPosition,
    @get:SynchronizedEntityData val direction: Directions,
    val fixedMotif: Motif?,
) : Entity(session, entityType, data, position.entityPosition, EntityRotation(0.0f, 0.0f)) {

    @get:SynchronizedEntityData
    val motif: Motif?
        get() = fixedMotif ?: data.get<Motif?>(MOTIF_DATA, null)

    companion object : EntityFactory<Painting> {
        override val identifier: ResourceLocation = minecraft("painting")
        private val MOTIF_DATA = EntityDataField("MOTIF", "MOTIVE")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Painting {
            return Painting(session, entityType, data, position.blockPosition, Directions.NORTH, null)
        }
    }
}
