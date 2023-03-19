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

package de.bixilon.minosoft.terminal.commands

import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minosoft.account.AccountParser
import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.util.KUtil.table

object AccountManageCommand : Command {
    override var node = LiteralNode("account")
        .addChild(
            LiteralNode("list", onlyDirectExecution = false, executor = {
                val filtered = it.collect()
                if (filtered.isEmpty()) throw CommandException("No account matched your filter!")

                it.print.print(table(filtered, "Id", "Type", "Username", "UUID", "State") { a -> arrayOf(a.id, a.type, a.username, a.uuid, a.state) })
            })
                .addChild(ArgumentNode("filter", AccountParser, executable = true)),
            LiteralNode("remove").apply {
                addFilter { stack, connections ->
                    var count = 0
                    connections.forEach { AccountProfileManager.selected.entries.remove(it.id); count++ }
                    stack.print.print("Disconnected from $count connections.")
                }
            },
            LiteralNode("select").apply {
                addFilter(false) { stack, connections ->
                    val account = connections.first()
                    AccountProfileManager.selected.selected = account
                    stack.print.print("Selected $account")
                }
            },
        )


    private fun CommandNode.addFilter(multi: Boolean = true, executor: (stack: CommandStack, connections: Collection<Account>) -> Unit): CommandNode {
        val node = ArgumentNode("filter", AccountParser, executor = {
            val filtered = it.collect()
            if (filtered.isEmpty()) throw CommandException("No accounts matched your filter!")
            if (!multi && filtered.size > 1) throw CommandException("Can not select multiple accounts!")
            executor(it, filtered)
        })
        addChild(node)
        return node
    }


    private fun CommandStack.collect(): Collection<Account> {
        val accounts = AccountProfileManager.selected.entries.values
        if (accounts.isEmpty()) throw CommandException("Not accounts available!")
        return this.get<AbstractTarget<Account>?>("filter")?.filter(accounts) ?: accounts
    }
}
