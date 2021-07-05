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

package de.bixilon.minosoft.gui.rendering.hud.atlas

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

data class HUDAtlasElement(
    override val texture: Texture,
    val binding: Vec2Binding,
    val slots: Map<Int, Vec2Binding> = mapOf(),
) : TextureLike {
    override lateinit var uvStart: Vec2
        private set
    override lateinit var uvEnd: Vec2
        private set
    override val size: Vec2i
        get() = binding.size


    fun postInit() {
        uvStart = (Vec2(binding.start) + RenderConstants.PIXEL_UV_PIXEL_ADD) * texture.arraySinglePixelFactor
        uvEnd = Vec2(binding.end) * texture.arraySinglePixelFactor
    }

    companion object {
        fun deserialize(json: Map<ResourceLocation, Any>, textures: MutableMap<ResourceLocation, Texture>): Map<ResourceLocation, HUDAtlasElement> {
            val ret: MutableMap<ResourceLocation, HUDAtlasElement> = mutableMapOf()
            for ((resourceLocation, data) in json) {
                ret[resourceLocation] = deserialize(data.compoundCast()!!, textures)
            }
            return ret
        }

        fun deserialize(json: Map<String, Any>, textures: MutableMap<ResourceLocation, Texture>): HUDAtlasElement {
            val keys: MutableSet<Int> = mutableSetOf()
            var textureResourceLocation: ResourceLocation? = json["texture"]?.nullCast<String>()?.let { ResourceLocation(it) }
            for (key in json["versions"]!!.compoundCast()!!.keys) {
                keys.add(key.toInt())
            }
            // ToDo: Sort and get correct version
            val imageJson = json["versions"]!!.compoundCast()!![keys.iterator().next().toString()]!!.compoundCast()!!

            imageJson["texture"]?.nullCast<String>()?.let { textureResourceLocation = ResourceLocation(it) }


            val texture = textures.getOrPut(textureResourceLocation!!) { Texture(textureResourceLocation!!) }

            val slots: MutableMap<Int, Vec2Binding> = mutableMapOf()

            imageJson["slots"]?.compoundCast()?.let {
                for ((id, data) in it) {
                    slots[id.toInt()] = Vec2Binding.deserialize(data)
                }
            }


            return HUDAtlasElement(texture, Vec2Binding.deserialize(imageJson), slots)
        }
    }

}
