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
package de.bixilon.minosoft.data.entities.entities.vehicle

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

class CommandBlockMinecart(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractMinecart(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Command")
    val command: String?
        get() = entityMetaData.sets.getString(EntityMetaDataFields.MINECART_COMMAND_BLOCK_COMMAND)

    @get:EntityMetaDataFunction(name = "Last output")
    val lastOutput: ChatComponent?
        get() = entityMetaData.sets.getChatComponent(EntityMetaDataFields.MINECART_COMMAND_BLOCK_LAST_OUTPUT)


    companion object : EntityFactory<CommandBlockMinecart> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("command_block_minecart")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): CommandBlockMinecart {
            return CommandBlockMinecart(connection, entityType, position, rotation)
        }
    }
}
