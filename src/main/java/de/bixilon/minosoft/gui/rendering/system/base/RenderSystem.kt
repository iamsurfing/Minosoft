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

package de.bixilon.minosoft.gui.rendering.system.base

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.Colors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2i
import java.nio.ByteBuffer
import java.nio.FloatBuffer

interface RenderSystem {
    val shaders: MutableSet<Shader>
    val vendor: GPUVendor
    var shader: Shader?
    var framebuffer: Framebuffer?

    fun init()

    fun reset(
        depthTest: Boolean = true,
        blending: Boolean = false,
        faceCulling: Boolean = true,
        depthMask: Boolean = true,
        sourceAlpha: BlendingFunctions = BlendingFunctions.SOURCE_ALPHA,
        destinationAlpha: BlendingFunctions = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
        depth: DepthFunctions = DepthFunctions.LESS,
        clearColor: RGBColor = Colors.TRANSPARENT,
    ) {
        setBlendFunction(sourceAlpha, destinationAlpha, BlendingFunctions.ONE, BlendingFunctions.ZERO)
        this[RenderingCapabilities.DEPTH_TEST] = depthTest
        this[RenderingCapabilities.BLENDING] = blending
        this[RenderingCapabilities.FACE_CULLING] = faceCulling
        this.depth = depth
        this.depthMask = depthMask
        this.clearColor = clearColor
        shader = null
    }

    fun enable(capability: RenderingCapabilities)
    fun disable(capability: RenderingCapabilities)
    operator fun set(capability: RenderingCapabilities, status: Boolean)
    operator fun get(capability: RenderingCapabilities): Boolean

    operator fun set(source: BlendingFunctions, destination: BlendingFunctions)

    fun setBlendFunction(sourceRGB: BlendingFunctions = BlendingFunctions.SOURCE_ALPHA, destinationRGB: BlendingFunctions = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, sourceAlpha: BlendingFunctions = BlendingFunctions.ONE, destinationAlpha: BlendingFunctions = BlendingFunctions.ZERO)

    var depth: DepthFunctions
    var depthMask: Boolean

    var polygonMode: PolygonModes


    val usedVRAM: Long
    val availableVRAM: Long
    val maximumVRAM: Long

    val vendorString: String
    val version: String
    val gpuType: String

    var clearColor: RGBColor

    var preferredPrimitiveType: PrimitiveTypes
    var primitiveMeshOrder: Array<Pair<Int, Int>>

    fun readPixels(start: Vec2i, end: Vec2i, type: PixelTypes): ByteBuffer


    fun createShader(resourceLocation: ResourceLocation): Shader

    fun createVertexBuffer(structure: MeshStruct, data: FloatBuffer, primitiveType: PrimitiveTypes = preferredPrimitiveType): FloatVertexBuffer
    fun createIntUniformBuffer(bindingIndex: Int = 0, data: IntArray = IntArray(0)): IntUniformBuffer
    fun createFloatUniformBuffer(bindingIndex: Int = 0, data: FloatBuffer): FloatUniformBuffer
    fun createFramebuffer(): Framebuffer

    fun createTextureManager(): TextureManager

    fun clear(vararg buffers: IntegratedBufferTypes)
}
