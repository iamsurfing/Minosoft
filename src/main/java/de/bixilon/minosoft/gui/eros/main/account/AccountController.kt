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

package de.bixilon.minosoft.gui.eros.main.account

import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane

class AccountController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var accountTypeListViewFX: ListView<ResourceLocation>

    @FXML private lateinit var accountListViewFX: ListView<Account>
    @FXML private lateinit var accountInfoFX: AnchorPane


    override fun init() {

    }

    companion object {
        val LAYOUT = "minosoft:eros/main/account/account.fxml".asResourceLocation()
    }
}
