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

package de.bixilon.minosoft.updater

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.commands.stack.print.PrintTarget

class UpdateProgress(
    val progress: AbstractLatch = SimpleLatch(0),
    var log: PrintTarget? = null,
) {
    var stage by observed(UpdateStage.WAITING)
    var error: Throwable? = null

    enum class UpdateStage {
        WAITING,
        DOWNLOADING,
        VERIFYING,
        STORING,

        DONE,
        FAILED,
    }
}
