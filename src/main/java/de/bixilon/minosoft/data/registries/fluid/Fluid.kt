/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.fluid

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidFilled
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*
import kotlin.math.abs

abstract class Fluid(override val identifier: ResourceLocation) : RegistryItem() {
    open var model: FluidModel? = null
    open val priority: Int get() = Int.MAX_VALUE

    abstract fun getVelocityMultiplier(session: PlaySession): Double

    fun getVelocity(session: PlaySession, blockState: BlockState, blockPosition: BlockPosition): Vec3d {
        val chunk = session.world.chunks[blockPosition.chunkPosition] ?: return Vec3d.EMPTY
        return getVelocity(blockState, blockPosition, chunk)
    }

    open fun getVelocity(blockState: BlockState, blockPosition: BlockPosition, chunk: Chunk): Vec3d {
        if (!this.matches(blockState)) {
            return Vec3d.EMPTY
        }
        val fluidHeight = getHeight(blockState)

        val velocity = Vec3d.EMPTY

        val offset = blockPosition.inChunkPosition
        for (direction in Directions.SIDES) {
            val neighbour = chunk.neighbours.traceBlock(offset, direction) ?: continue
            if (!this.matches(neighbour)) {
                continue
            }
            val height = getHeight(neighbour)

            var heightDifference = 0.0f

            if (height == 0.0f) {
                // ToDo
            } else {
                heightDifference = fluidHeight - height
            }

            if (heightDifference != 0.0f) {
                velocity += (direction.vectord * heightDifference)
            }
        }

        // ToDo: Falling fluid

        if (velocity == Vec3d.EMPTY) {
            return velocity
        }

        return velocity.normalize()
    }


    open fun travel(physics: LivingEntityPhysics<*>, input: MovementInput, gravity: Double, falling: Boolean) = Unit

    fun LivingEntityPhysics<*>.applyFluidMovingSpeed(gravity: Double, falling: Boolean, motion: Vec3d) {
        if (!entity.hasGravity || entity.isSprinting) {
            return
        }

        val up = if (falling && abs(motion.y - 0.005) >= 0.003 && abs(motion.y - gravity / 16.0) < 0.003) {
            -0.003
        } else {
            motion.y - gravity / 16.0
        }

        this.velocity = Vec3d(motion.x, up, motion.z)
    }


    protected fun LivingEntityPhysics<*>.applyBouncing(y: Double) {
        val velocity = this.velocity
        if (!horizontalCollision || !doesNotCollide(Vec3d(velocity.x, velocity.y + 0.6f - position.y + y, velocity.z))) return
        this.velocity = Vec3d(velocity.x, 0.3, velocity.z)
    }

    protected fun LivingEntityPhysics<*>.applyFriction(horizontal: Double, vertical: Double = 0.8f.toDouble()) {
        this.velocity = velocity * Vec3d(horizontal, vertical, horizontal)
    }

    override fun toString(): String {
        return identifier.toString()
    }

    open fun matches(other: Fluid): Boolean {
        return other == this
    }

    open fun matches(other: BlockState?): Boolean {
        if (other == null || other.block !is FluidHolder) return false
        return matches(other.block.fluid)
    }

    open fun getHeight(state: BlockState): Float {
        if (state.block !is FluidHolder || state.block.fluid != this) {
            return 0.0f
        }
        if (state.block is FluidFilled && state.block.fluid == this) {
            return MAX_LEVEL
        }
        val level = state[FluidBlock.LEVEL]
        if (level <= 0 || level >= 8) {
            return MAX_LEVEL
        }
        return (LEVELS - level) / DIVIDER
    }

    open fun randomTick(session: PlaySession, blockState: BlockState, blockPosition: BlockPosition, random: Random) = Unit

    open fun createModel(): FluidModel? {
        return null
    }

    companion object {
        const val LEVELS = 8
        const val DIVIDER = 9.0f
        const val MAX_LEVEL = LEVELS / DIVIDER
        const val MIN_LEVEL = 1 / DIVIDER
    }
}
