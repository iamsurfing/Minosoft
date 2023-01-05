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

package de.bixilon.minosoft.recipes.special.banner

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.recipes.RecipeCategories
import de.bixilon.minosoft.recipes.special.SpecialRecipe
import de.bixilon.minosoft.recipes.special.SpecialRecipeFactory

class ShieldDecorationRecipe(
    override val category: RecipeCategories?,
) : SpecialRecipe {

    companion object : SpecialRecipeFactory<ShieldDecorationRecipe> {
        override val identifier = minecraft("crafting_special_shielddecoration")

        override fun build(category: RecipeCategories?): ShieldDecorationRecipe {
            return ShieldDecorationRecipe(category)
        }
    }
}
