/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.LevelTypes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import glm_.vec3.Vec3

class RespawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    lateinit var dimension: Dimension
        private set
    var difficulty: Difficulties = Difficulties.NORMAL
        private set
    val gamemode: Gamemodes
    var levelType: LevelTypes = LevelTypes.UNKNOWN
        private set
    var hashedSeed = 0L
        private set
    var isDebug = false
        private set
    var isFlat = false
        private set
    var copyMetaData = false
        private set

    init {
        when {
            buffer.versionId < ProtocolVersions.V_20W21A -> {
                dimension = buffer.connection.mapping.dimensionRegistry[if (buffer.versionId < ProtocolVersions.V_1_8_9) { // ToDo: this should be 108 but wiki.vg is wrong. In 1.8 it is an int.
                    buffer.readByte().toInt()
                } else {
                    buffer.readInt()
                }]
            }
            buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 -> {
                dimension = buffer.connection.mapping.dimensionRegistry[buffer.readResourceLocation()]!!
            }
            else -> {
                buffer.readNBT()?.compoundCast() // current dimension data
            }
        }
        if (buffer.versionId < ProtocolVersions.V_19W11A) {
            difficulty = Difficulties.byId(buffer.readUnsignedByte())
        }
        if (buffer.versionId >= ProtocolVersions.V_20W22A) {
            dimension = buffer.connection.mapping.dimensionRegistry[buffer.readResourceLocation()]!!
        }
        if (buffer.versionId >= ProtocolVersions.V_19W36A) {
            hashedSeed = buffer.readLong()
        }
        gamemode = Gamemodes[buffer.readUnsignedByte()]
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
            buffer.readByte() // previous game mode
        }
        if (buffer.versionId >= ProtocolVersions.V_13W42B && buffer.versionId < ProtocolVersions.V_20W20A) {
            levelType = LevelTypes.byType(buffer.readString())
        }
        if (buffer.versionId >= ProtocolVersions.V_20W20A) {
            isDebug = buffer.readBoolean()
            isFlat = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_20W18A) {
            copyMetaData = buffer.readBoolean()
        }
    }

    override fun handle(connection: PlayConnection) {
        // clear all chunks
        connection.world.chunks.clear()
        connection.world.dimension = dimension
        connection.player.isSpawnConfirmed = false
        connection.player.entity.tabListItem.gamemode = gamemode
        connection.player.entity.velocity = Vec3()

        connection.fireEvent(RespawnEvent(connection, this))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Respawn (dimension=$dimension, difficulty=$difficulty, gamemode=$gamemode, levelType=$levelType)" }
    }
}
