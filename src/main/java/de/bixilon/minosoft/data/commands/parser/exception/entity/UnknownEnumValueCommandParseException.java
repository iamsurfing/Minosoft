/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.commands.parser.exception.entity;

import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class UnknownEnumValueCommandParseException extends CommandParseException {

    private static final String ERROR_MESSAGE = "Unknown enum value!";

    public UnknownEnumValueCommandParseException(ImprovedStringReader command, String currentArgument) {
        super(ERROR_MESSAGE, command, currentArgument);
    }

    public UnknownEnumValueCommandParseException(ImprovedStringReader command, String currentArgument, Throwable cause) {
        super(ERROR_MESSAGE, command, currentArgument, cause);
    }
}
