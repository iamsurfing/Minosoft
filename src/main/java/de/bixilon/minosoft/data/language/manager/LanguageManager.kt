/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.language.manager

import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent

class LanguageManager(
    private val languages: MutableList<Translator> = synchronizedListOf(),
) : Translator {

    override fun canTranslate(key: ResourceLocation?): Boolean {
        for (language in languages) {
            if (language.canTranslate(key)) {
                return true
            }
        }
        return false
    }

    override fun translate(key: ResourceLocation?, parent: TextComponent?, restrictedMode: Boolean, vararg data: Any?): ChatComponent {
        for (language in languages) {
            if (!language.canTranslate(key)) {
                continue
            }
            return language.translate(key, parent, restrictedMode, *data)
        }
        return LanguageUtil.getFallbackTranslation(key, parent, restrictedMode, data)
    }
}
