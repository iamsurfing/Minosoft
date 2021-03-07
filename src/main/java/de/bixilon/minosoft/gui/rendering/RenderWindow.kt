/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.WorldRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.EventInvokerCallback
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketPlayerPositionAndRotation
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import glm_.vec2.Vec2
import org.lwjgl.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.ConcurrentLinkedQueue

class RenderWindow(
    val connection: Connection,
    val rendering: Rendering,
) {
    private val keyBindingCallbacks: MutableMap<ResourceLocation, Pair<KeyBinding, MutableSet<((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)>>> = mutableMapOf()
    private val keysDown: MutableSet<KeyCodes> = mutableSetOf()
    private val keyBindingDown: MutableSet<KeyBinding> = mutableSetOf()
    val renderStats = RenderStats()
    var screenWidth = 900
        private set
    var screenHeight = 500
        private set
    var screenDimensions = Vec2(screenWidth, screenHeight)
        private set

    private var windowId = 0L
    private var deltaFrameTime = 0.0 // time between current frame and last frame

    private var lastFrame = 0.0
    val camera: Camera = Camera(connection, Minosoft.getConfig().config.game.camera.fov, this)
    private val latch = CountUpAndDownLatch(1)

    private var renderingStatus = RenderingStates.RUNNING

    private var polygonEnabled = false
    private var mouseCatch = !StaticConfiguration.DEBUG_MODE

    private val screenshotTaker = ScreenshotTaker(this)
    val tintColorCalculator = TintColorCalculator()
    val font = Font()
    val textures = TextureArray(mutableListOf())

    // all renderers
    val worldRenderer: WorldRenderer = WorldRenderer(connection, connection.player.world, this)
    val hudRenderer: HUDRenderer = HUDRenderer(connection, this)

    val renderQueue = ConcurrentLinkedQueue<Runnable>()

    init {
        connection.registerEvent(EventInvokerCallback<ConnectionStateChangeEvent> {
            if (it.connection.isDisconnected) {
                renderQueue.add {
                    glfwSetWindowShouldClose(windowId, true)
                }
            }
        })
        connection.registerEvent(EventInvokerCallback<PacketReceiveEvent> {
            val packet = it.packet
            if (packet !is PacketPlayerPositionAndRotation) {
                return@EventInvokerCallback
            }
            if (latch.count > 0) {
                latch.countDown()
            }
            renderQueue.add {
                camera.setPosition(packet.position)
                camera.setRotation(packet.rotation.yaw.toDouble(), packet.rotation.pitch.toDouble())
            }
        })
    }


    fun init(latch: CountUpAndDownLatch) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize  Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        windowId = glfwCreateWindow(screenWidth, screenHeight, "Minosoft", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowId == MemoryUtil.NULL) {
            glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
        camera.init(this)

        tintColorCalculator.init(connection.version.assetsManager)


        glfwSetKeyCallback(this.windowId) { _: Long, key: Int, _: Int, action: Int, _: Int ->
            val keyCode = KeyCodes.KEY_CODE_GLFW_ID_MAP[key] ?: KeyCodes.KEY_UNKNOWN
            val keyAction = when (action) {
                GLFW_PRESS -> KeyAction.PRESS
                GLFW_RELEASE -> KeyAction.RELEASE
                // ToDo: Double, Hold
                else -> return@glfwSetKeyCallback
            }
            if (keyAction == KeyAction.PRESS) {
                keysDown.add(keyCode)
            } else if (keyAction == KeyAction.RELEASE) {
                keysDown.remove(keyCode)
            }

            for ((_, keyCallbackPair) in keyBindingCallbacks) {
                run {
                    val keyBinding = keyCallbackPair.first
                    val keyCallbacks = keyCallbackPair.second

                    var anyCheckRun = false
                    keyBinding.action[KeyAction.MODIFIER]?.let {
                        val previousKeysDown = if (keyAction == KeyAction.RELEASE) {
                            val previousKeysDown = keysDown.toMutableList()
                            previousKeysDown.add(keyCode)
                            previousKeysDown
                        } else {
                            keysDown
                        }
                        if (!previousKeysDown.containsAll(it)) {
                            return@run
                        }
                        anyCheckRun = true
                    }
                    keyBinding.action[KeyAction.CHANGE]?.let {
                        if (!it.contains(keyCode)) {
                            return@run
                        }
                        anyCheckRun = true
                    }

                    // release or press
                    if (keyBinding.action[KeyAction.CHANGE] == null) {
                        keyBinding.action[keyAction].let {
                            if (it == null) {
                                return@run
                            }
                            if (!it.contains(keyCode)) {
                                return@run
                            }
                            anyCheckRun = true
                        }
                    }

                    if (!anyCheckRun) {
                        return@run
                    }

                    if (keyAction == KeyAction.PRESS) {
                        keyBindingDown.add(keyBinding)
                    } else if (keyAction == KeyAction.RELEASE) {
                        keyBindingDown.remove(keyBinding)
                    }
                    for (keyCallback in keyCallbacks) {
                        keyCallback.invoke(keyCode, keyAction)
                    }
                }
            }
        }


        if (mouseCatch) {
            glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetCursorPosCallback(windowId) { _: Long, xPos: Double, yPos: Double -> camera.mouseCallback(xPos, yPos) }
        MemoryStack.stackPush().let { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(windowId, (videoMode.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId)
        // Enable v-sync
        glfwSwapInterval(0)


        // Make the window visible
        GL.createCapabilities()

        setSkyColor(RGBColor("#fffe7a"))

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


        textures.textures.add(Texture(TextureArray.DEBUG_TEXTURE))

        font.load(connection.version.assetsManager)

        font.preLoadAtlas(textures)

        worldRenderer.init()
        hudRenderer.init()


        textures.preLoad(connection.version.assetsManager)

        font.loadAtlas(textures)
        textures.load()



        worldRenderer.postInit()
        hudRenderer.postInit()


        glfwSetWindowSizeCallback(windowId, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                screenWidth = width
                screenHeight = height
                screenDimensions = Vec2(width, height)
                camera.screenChangeResizeCallback(screenWidth, screenHeight)
                hudRenderer.screenChangeResizeCallback(width, height)
            }
        })

        glfwSetWindowFocusCallback(windowId, object : GLFWWindowFocusCallback() {
            override fun invoke(window: Long, focused: Boolean) {
                setRenderStatus(if (focused) {
                    RenderingStates.RUNNING
                } else {
                    RenderingStates.SLOW
                })
            }
        })

        glfwSetWindowIconifyCallback(windowId, object : GLFWWindowIconifyCallback() {
            override fun invoke(window: Long, iconified: Boolean) {
                setRenderStatus(if (iconified) {
                    RenderingStates.PAUSED
                } else {
                    RenderingStates.RUNNING
                })
            }
        })


        registerGlobalKeyCombinations()

        hudRenderer.screenChangeResizeCallback(screenWidth, screenHeight)

        camera.addShaders(worldRenderer.chunkShader)

        camera.screenChangeResizeCallback(screenWidth, screenHeight)

        glEnable(GL_DEPTH_TEST)

        Log.debug("Rendering is prepared and ready to go!")
        latch.countDown()
        latch.waitUntilZero()
        this.latch.waitUntilZero()
        glfwShowWindow(windowId)
    }

    private fun registerGlobalKeyCombinations() {
        registerKeyCallback(KeyBindingsNames.DEBUG_POLYGEN) { _: KeyCodes, _: KeyAction ->
            polygonEnabled = !polygonEnabled
            glPolygonMode(GL_FRONT_AND_BACK, if (polygonEnabled) {
                GL_LINE
            } else {
                GL_FILL
            })
            sendDebugMessage("Toggled polygen mode!")
        }
        registerKeyCallback(KeyBindingsNames.DEBUG_MOUSE_CATCH) { _: KeyCodes, _: KeyAction ->
            mouseCatch = !mouseCatch
            if (mouseCatch) {
                glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            } else {
                glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
            }
            sendDebugMessage("Toggled mouse catch!")
        }
        registerKeyCallback(KeyBindingsNames.QUIT_RENDERING) { _: KeyCodes, _: KeyAction ->
            glfwSetWindowShouldClose(windowId, true)
        }
        registerKeyCallback(KeyBindingsNames.TAKE_SCREENSHOT) { _: KeyCodes, _: KeyAction ->
            screenshotTaker.takeScreenshot()
        }
    }

    fun startRenderLoop() {
        while (!glfwWindowShouldClose(windowId)) {
            if (renderingStatus == RenderingStates.PAUSED) {
                Thread.sleep(100L)
                glfwPollEvents()
                continue
            }
            renderStats.startFrame()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            val currentFrame = glfwGetTime()
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame



            worldRenderer.draw()
            hudRenderer.draw()

            renderStats.endDraw()


            glfwSwapBuffers(windowId)
            glfwPollEvents()
            camera.draw()
            camera.handleInput(deltaFrameTime)


            // handle opengl context tasks, maximum 10 per frame
            var actionsDone = 0
            for (renderQueueElement in renderQueue) {
                if (actionsDone == 10) {
                    break
                }
                renderQueueElement.run()
                renderQueue.remove(renderQueueElement)
                actionsDone++
            }

            when (renderingStatus) {
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.STOPPED -> glfwSetWindowShouldClose(windowId, true)
            }
            renderStats.endFrame()
        }
    }

    fun exit() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowId)
        glfwDestroyWindow(windowId)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()

        // disconnect
        connection.disconnect()
    }

    private fun setRenderStatus(renderingStatus: RenderingStates) {
        if (renderingStatus == this.renderingStatus) {
            return
        }
        if (this.renderingStatus == RenderingStates.PAUSED) {
            renderQueue.clear()
            worldRenderer.refreshChunkCache()
        }
        this.renderingStatus = renderingStatus
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, callback: ((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)) {
        var resourceLocationCallbacks = keyBindingCallbacks[resourceLocation]?.second
        if (resourceLocationCallbacks == null) {
            resourceLocationCallbacks = mutableSetOf()
            val keyBinding = Minosoft.getConfig().config.game.controls.keyBindings.entries[resourceLocation] ?: return
            keyBindingCallbacks[resourceLocation] = Pair(keyBinding, resourceLocationCallbacks)
        }
        resourceLocationCallbacks.add(callback)
    }

    fun setSkyColor(color: RGBColor) {
        glClearColor(color.floatRed, color.floatGreen, color.floatBlue, 1.0f)
    }

    fun sendDebugMessage(message: String) {
        connection.sender.sendFakeChatMessage(RenderConstants.DEBUG_MESSAGES_PREFIX + message)
    }
}
