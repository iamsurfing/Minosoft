/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.decoration

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Location.Companion.fromPosition
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.Motive
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.protocol.network.Connection
import java.util.*

class Painting(
    connection: Connection,
    entityId: Int,
    uuid: UUID,
    position: BlockPosition,
    @get:EntityMetaDataFunction(identifier = "Direction") val direction: Directions,
    @get:EntityMetaDataFunction(identifier = "Motive") val motive: Motive,
) : Entity(connection, entityId, uuid, fromPosition(position), EntityRotation(0f, 0f, 0f))
