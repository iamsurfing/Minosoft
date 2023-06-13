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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.LineRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.MAX
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.offset
import de.bixilon.minosoft.util.KUtil.charCount

open class TextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    background: RGBColor? = RenderConstants.TEXT_BACKGROUND_COLOR,
    parent: Element? = null,
    properties: TextRenderProperties = TextRenderProperties.DEFAULT,
) : Element(guiRenderer, text.charCount * 6 * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX), Labeled {
    private var activeElement: TextComponent? = null
    lateinit var info: TextRenderInfo
        private set

    var background: RGBColor? = background
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var properties: TextRenderProperties = properties
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    override var size: Vec2i
        get() = super.size
        set(value) {}

    override var text: Any = text
        set(value) {
            chatComponent = ChatComponent.of(value, translator = Minosoft.LANGUAGE_MANAGER /*guiRenderer.connection.language*/) // Should the server be allowed to send minosoft namespaced translation keys?
            field = value
        }

    private var empty: Boolean = true

    var _chatComponent: ChatComponent = unsafeNull()
        set(value) {
            if (value == field) {
                return
            }
            field = value
            empty = value is EmptyComponent || value.message.isEmpty()
            updatePrefSize(value)
        }

    override var chatComponent: ChatComponent
        get() = _chatComponent
        protected set(value) {
            _chatComponent = value
            forceApply()
        }

    init {
        this._parent = parent
        this._chatComponent = ChatComponent.of(text)
        forceSilentApply()
    }

    private fun updatePrefSize(text: ChatComponent) {
        var prefSize = Vec2.EMPTY
        if (!empty) {
            val info = TextRenderInfo(Vec2.MAX)
            ChatComponentRenderer.render(TextOffset(), context.font, properties, info, null, null, text)
            prefSize = info.size
        }
        _prefSize = Vec2i(prefSize)
    }

    private fun updateText(text: ChatComponent) {
        val info = TextRenderInfo(Vec2(maxSize))
        if (!empty) {
            ChatComponentRenderer.render(TextOffset(), context.font, properties, info, null, null, text)
            info.rewind()
        }
        this.info = info
        _size = Vec2i(info.size)
    }

    override fun forceSilentApply() {
        updateText(this._chatComponent)
        this.cacheUpToDate = false
    }

    override fun onChildChange(child: Element) = Broken("A TextElement can not have a child!")

    private fun GUIVertexConsumer.renderBackground(color: RGBColor, properties: TextRenderProperties, info: TextRenderInfo, offset: Vec2, options: GUIVertexOptions?) {
        val start = Vec2()
        val end = Vec2()

        val lineHeight = properties.lineHeight

        for ((index, line) in info.lines.withIndex()) {
            start.x = offset.x + properties.alignment.getOffset(line.width, info.size.x)
            start.y = offset.y + (index * lineHeight) + (maxOf(index - 1, 0) * properties.lineSpacing)

            end.x = start.x + line.width
            end.y = start.y + lineHeight

            addQuad(start, end, context.textureManager.whiteTexture, color, options)
        }
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (empty) return
        val info = this.info
        val properties = this.properties
        val initialOffset = Vec2(offset + margin.offset)

        this.background?.let { consumer.renderBackground(it, properties, info, initialOffset, options) }

        var vertices = ChatComponentRenderer.calculatePrimitiveCount(chatComponent) * consumer.order.size * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX
        if (properties.shadow) {
            vertices *= 2
        }
        consumer.ensureSize(vertices)

        ChatComponentRenderer.render(TextOffset(Vec2(initialOffset)), context.font, properties, info, consumer, options, chatComponent)
        info.rewind()
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS || button != MouseButtons.LEFT) {
            return true
        }
        val pair = getTextComponentAt(position) ?: return false
        pair.first.clickEvent?.onClick(guiRenderer, pair.second, button, action)
        return true
    }

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getTextComponentAt(position) ?: return false
        activeElement = pair.first
        pair.first.hoverEvent?.onMouseEnter(guiRenderer, pair.second, absolute)
        if (pair.first.clickEvent != null) {
            context.window.cursorShape = CursorShapes.HAND
        }
        return true
    }

    override fun onMouseMove(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getTextComponentAt(Vec2(position))

        if (activeElement != pair?.first) {
            val activeElement = activeElement
            this.activeElement = pair?.first
            if (pair?.first?.clickEvent == null) {
                context.window.resetCursor()
            } else {
                context.window.cursorShape = CursorShapes.HAND
            }
            return (activeElement?.hoverEvent?.onMouseLeave(guiRenderer) ?: false) || (pair?.first?.hoverEvent?.onMouseEnter(guiRenderer, Vec2i(pair.second), absolute) ?: false)
        }
        return pair?.first?.hoverEvent?.onMouseMove(guiRenderer, Vec2i(pair.second), absolute) ?: false
    }

    override fun onMouseLeave(): Boolean {
        val activeElement = activeElement ?: return false
        this.activeElement = null
        if (activeElement.clickEvent != null) {
            context.window.resetCursor()
        }
        activeElement.hoverEvent?.onMouseLeave(guiRenderer) ?: return false
        return true
    }

    override fun onHide() {
        activeElement?.hoverEvent?.onMouseLeave(guiRenderer) // ToDo: This should not be here (if if should be anywhere)
        activeElement = null
    }

    private fun TextRenderInfo.getLineAt(lineHeight: Float, lineSpacing: Float, offset: Float): Pair<LineRenderInfo, Float>? {
        var offset = offset

        for ((index, line) in info.lines.withIndex()) {
            if (offset in 0.0f..lineHeight) {
                return Pair(line, offset)
            }
            offset -= lineHeight
            if (index > 0) {
                offset -= lineSpacing
            }
        }

        return null
    }

    @Deprecated("int")
    private fun getTextComponentAt(position: Vec2i): Pair<TextComponent, Vec2i>? = getTextComponentAt(Vec2(position))?.let { Pair(it.first, Vec2i(it.second)) }

    private fun getTextComponentAt(position: Vec2): Pair<TextComponent, Vec2>? {
        val offset = Vec2(position)
        val info = this.info
        val properties = this.properties
        val (line, yOffset) = info.getLineAt(properties.lineHeight, properties.lineSpacing, offset.y) ?: return null
        offset.y = yOffset


        val cutInfo = TextRenderInfo(Vec2(offset.x, properties.lineHeight))
        val cut = ChatComponentRenderer.render(TextOffset(), context.font, properties, cutInfo, null, null, line.text)

        val line0 = cutInfo.lines.getOrNull(0) ?: return null
        val message = line0.text.message
        var charToCheck = message.length
        if (cut) {
            // last char got cut off
            charToCheck++
        }
        val text = line.text.getTextAt(charToCheck)
        offset.x -= line0.width // TODO: the cut part of the last char is missing


        offset.x += properties.alignment.getOffset(info.size.x, line.width)
        return Pair(text, offset)
    }

    override fun toString(): String {
        return chatComponent.toString()
    }
}
