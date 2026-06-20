package com.example.game

import androidx.compose.ui.geometry.Rect

const val GRAVITY = 2000f
const val JUMP_VELOCITY = -1150f
const val MOVE_SPEED = 400f
const val MAX_FALL_SPEED = 1200f
const val BLOCK_SIZE = 80f

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 60f,
    var height: Float = 60f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isJumping: Boolean = false,
    var isCrouching: Boolean = false,
    var facingRight: Boolean = true,
    var state: State = State.IDLE,
    var stateTime: Float = 0f
) {
    enum class State { IDLE, WALKING, JUMPING, CROUCHING }

    val bounds: Rect get() {
        val h = if (isCrouching) height / 2f else height
        val currentY = if (isCrouching) y + height / 2f else y
        return Rect(x, currentY, x + width, currentY + h)
    }

    fun updateState(dt: Float) {
        stateTime += dt
        val oldState = state
        state = when {
            isJumping -> State.JUMPING
            isCrouching -> State.CROUCHING
            vx != 0f -> State.WALKING
            else -> State.IDLE
        }
        if (oldState != state) {
            stateTime = 0f
        }
    }
}

enum class BlockType { GROUND, DIRT, BRICK, QUESTION, WATER, BUSH, LOG }

enum class ItemType { COIN, FRUIT }

data class Item(
    val x: Float,
    val y: Float,
    val type: ItemType,
    var collected: Boolean = false,
    val size: Float = 40f
) {
    val bounds: Rect get() = Rect(x, y, x + size, y + size)
}

data class Block(
    val x: Float,
    val y: Float,
    val width: Float = BLOCK_SIZE,
    val height: Float = BLOCK_SIZE,
    val type: BlockType
) {
    val bounds: Rect get() = Rect(x, y, x + width, y + height)
}
