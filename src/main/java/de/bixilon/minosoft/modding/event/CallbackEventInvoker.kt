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
package de.bixilon.minosoft.modding.event

import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.loading.Priorities

class CallbackEventInvoker<E : Event?> private constructor(
    ignoreCancelled: Boolean,
    private val callback: (event: E) -> Unit,
    private val eventType: Class<out Event>,
) : EventInvoker(ignoreCancelled, Priorities.NORMAL, null) {

    override fun invoke(event: Event) {
        callback.invoke(event as E)
    }

    override fun getEventType(): Class<out Event> {
        return eventType
    }

    companion object {
        @JvmOverloads
        @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
        inline fun <reified E : Event> of(ignoreCancelled: Boolean = false, noinline callback: (event: E) -> Unit): CallbackEventInvoker<E> {
            return CallbackEventInvoker(
                ignoreCancelled = ignoreCancelled,
                callback = callback,
                eventType = E::class.java
            )
        }
    }
}
