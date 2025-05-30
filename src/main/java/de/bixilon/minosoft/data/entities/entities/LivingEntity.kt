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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.equipment.EntityEquipment
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.properties.StatusEffectProperty
import de.bixilon.minosoft.data.entities.event.events.damage.DamageEvent
import de.bixilon.minosoft.data.entities.event.events.damage.DamageListener
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class LivingEntity(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(session, entityType, data, position, rotation), DamageListener {
    private val entityEffectParticle = session.registries.particleType[EntityEffectParticle]
    private val ambientEntityEffectParticle = session.registries.particleType[AmbientEntityEffectParticle]

    open val equipment = EntityEquipment(this)
    val effects = StatusEffectProperty()
    val attributes = EntityAttributes(entityType.attributes)


    override val canRaycast: Boolean get() = super.canRaycast && health > 0.0
    override val name: ChatComponent? get() = super.name

    private var flags by data(FLAGS_DATA, 0x00) { it.toInt() }
    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return flags.isBitMask(bitMask)
    }

    @get:SynchronizedEntityData
    open val pose: Poses? by data(POSE_DATA, Poses.STANDING)

    @get:SynchronizedEntityData
    open val usingHand: Hands?
        get() {
            if (!getLivingEntityFlag(0x01)) return null // not using item

            return if (getLivingEntityFlag(0x02)) Hands.OFF else Hands.MAIN
        }

    @get:SynchronizedEntityData // aka using riptide
    val isRiptideAttacking: Boolean
        get() = getLivingEntityFlag(0x04)

    private val _health: Float? by data(HEALTH_DATA, null)

    @get:SynchronizedEntityData
    open val health: Double
        get() = _health?.toDouble() ?: attributes[MinecraftAttributes.MAX_HEALTH]

    @get:SynchronizedEntityData
    val effectColor: RGBColor?
        get() = data.get<Int?>(EFFECT_COLOR_DATA, null)?.asRGBColor()

    @get:SynchronizedEntityData
    val effectAmbient: Boolean
        get() = data.getBoolean(EFFECT_AMBIENT_DATA, false)

    @get:SynchronizedEntityData
    val arrowCount: Int by data(ARROW_COUNT_DATA, 0)

    @get:SynchronizedEntityData
    val absorptionHearts: Int by data(ABSORPTION_HEARTS_DATA, 0)

    @get:SynchronizedEntityData
    val bedPosition: BlockPosition? by data(BED_POSITION_DATA, null)

    open val isSleeping: Boolean
        get() = bedPosition != null


    override val hitboxColor: RGBColor?
        get() = when {
            isInvisible -> ChatColors.GREEN
            else -> super.hitboxColor
        }

    override fun createPhysics(): LivingEntityPhysics<*> = LivingEntityPhysics(this)

    override fun tick() {
        super.tick()
        effects.tick()
    }

    val activelyRiding: Boolean get() = false

    override fun physics(): LivingEntityPhysics<*> = super.physics().unsafeCast()

    override fun onDamage(type: DamageEvent) {
        this.renderer?.nullCast<DamageListener>()?.onDamage(type)
    }

    companion object {
        private val FLAGS_DATA = EntityDataField("LIVING_ENTITY_FLAGS")
        private val HEALTH_DATA = EntityDataField("LIVING_ENTITY_HEALTH")
        private val EFFECT_COLOR_DATA = EntityDataField("LIVING_ENTITY_EFFECT_COLOR")
        private val EFFECT_AMBIENT_DATA = EntityDataField("LIVING_ENTITY_EFFECT_AMBIENCE")
        private val ARROW_COUNT_DATA = EntityDataField("LIVING_ENTITY_ARROW_COUNT")
        private val ABSORPTION_HEARTS_DATA = EntityDataField("LIVING_ENTITY_ABSORPTION_HEARTS")
        private val BED_POSITION_DATA = EntityDataField("LIVING_ENTITY_BED_POSITION")
    }
}
