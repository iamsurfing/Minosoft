/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.registry

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.util.collections.Clearable

class FakeEnumRegistry<T : RegistryFakeEnumerable>(
    private var parentRegistry: FakeEnumRegistry<T>? = null,
) : Clearable {
    private var initialized = false
    private val idValueMap: MutableMap<Int, T> = mutableMapOf()
    private val valueIdMap: MutableMap<T, Int> = mutableMapOf()
    private val nameValueMap: MutableMap<String, T> = mutableMapOf()

    fun get(name: String): T? {
        return nameValueMap[name] ?: parentRegistry?.get(name)
    }

    fun get(id: Int): T? {
        return idValueMap[id] ?: parentRegistry?.get(id)
    }

    fun getId(value: T): Int {
        return valueIdMap[value] ?: parentRegistry?.getId(value)!!
    }

    fun setParent(registry: FakeEnumRegistry<T>?) {
        check(registry !== this) { "Can not set our self as parent!" }
        this.parentRegistry = registry
    }

    fun initialize(data: JsonObject?, mappings: VersionMapping, deserializer: IdDeserializer<T>): FakeEnumRegistry<T> {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return this
        }

        for ((id, value) in data.entrySet()) {
            check(value is JsonObject)
            var itemId = id.toInt()

            val item = deserializer.deserialize(mappings, value)
            value["id"]?.asInt?.let { providedItemId ->
                itemId = providedItemId
            }
            idValueMap[itemId] = item
            valueIdMap[item] = itemId
            nameValueMap[item.name] = item
        }
        initialized = true
        return this
    }

    override fun clear() {
        idValueMap.clear()
        valueIdMap.clear()
        nameValueMap.clear()
    }
}
