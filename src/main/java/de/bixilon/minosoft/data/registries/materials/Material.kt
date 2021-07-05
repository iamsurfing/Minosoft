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
package de.bixilon.minosoft.data.registries.materials

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.booleanCast
import java.util.*

data class Material(
    override val resourceLocation: ResourceLocation,
    val color: RGBColor?,
    val pushReaction: PushReactions,
    val blockMotion: Boolean,
    val flammable: Boolean,
    val liquid: Boolean,
    val soft: Boolean,
    val solidBlocking: Boolean,
    val replaceable: Boolean,
    val solid: Boolean,
) : RegistryItem() {

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Material> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Material {
            return Material(
                resourceLocation = resourceLocation,
                color = TintColorCalculator.getJsonColor(data["color"]?.nullCast<Int>() ?: 0),
                pushReaction = data["push_reaction"]?.nullCast<String>()?.let { PushReactions.valueOf(it.uppercase(Locale.getDefault())) } ?: PushReactions.NORMAL,
                blockMotion = data["blocks_motion"]?.booleanCast() ?: false,
                flammable = data["flammable"]?.booleanCast() ?: false,
                liquid = data["liquid"]?.booleanCast() ?: false,
                soft = data["is_soft"]?.booleanCast() ?: false,
                solidBlocking = data["solid_blocking"]?.booleanCast() ?: false,
                replaceable = data["replaceable"]?.booleanCast() ?: false,
                solid = data["solid"]?.booleanCast() ?: false,
            )
        }
    }
}
