package com.example.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

class GameEngine {
    var player = Player(x = 100f, y = 300f)
    var blocks = mutableListOf<Block>()
    var decor = mutableListOf<Block>() // Non-collidable
    var items = mutableListOf<Item>()
    var cameraX by mutableStateOf(0f)
    var score by mutableStateOf(0)
    
    var moveCrouch = false

    var frameCount by mutableStateOf(0)

    private val levelLength = 1000 // endless

    init {
        generateLevel()
    }

    private fun generateLevel() {
        val groundY = 800f
        blocks.clear()
        decor.clear()
        items.clear()
        
        var i = 0
        while (i < levelLength) {
            // Pit chance
            if (i > 10 && Random.nextFloat() < 0.15f) {
                val pitSize = Random.nextInt(2, 5)
                for (p in 0 until pitSize) {
                    decor.add(Block((i + p) * BLOCK_SIZE, groundY + BLOCK_SIZE, type = BlockType.WATER))
                }
                
                // Maybe a floating coin over the pit
                if (Random.nextBoolean()) {
                    items.add(Item((i + pitSize/2f) * BLOCK_SIZE, groundY - 150f, ItemType.COIN))
                }
                
                i += pitSize
                continue
            }

            // Ground
            blocks.add(Block(i * BLOCK_SIZE, groundY, type = BlockType.GROUND))
            blocks.add(Block(i * BLOCK_SIZE, groundY + BLOCK_SIZE, type = BlockType.DIRT))
            blocks.add(Block(i * BLOCK_SIZE, groundY + 2 * BLOCK_SIZE, type = BlockType.DIRT))

            // Obstacles / Platforms
            if (i > 5) {
                val rand = Random.nextFloat()
                if (rand < 0.2f) {
                    // Small jump obstacle on ground
                    blocks.add(Block(i * BLOCK_SIZE, groundY - BLOCK_SIZE, type = BlockType.BRICK))
                    // Item over obstacle
                    items.add(Item(i * BLOCK_SIZE + 20f, groundY - BLOCK_SIZE - 80f, if (Random.nextFloat() < 0.2f) ItemType.FRUIT else ItemType.COIN))
                } else if (rand < 0.35f) {
                    // Crouch obstacle (floating block with small gap underneath)
                    // Gap is 50f, player height is 60f (needs ducking to 30f)
                    val numBlocks = Random.nextInt(2, 5)
                    for (b in 0 until numBlocks) {
                        blocks.add(Block((i + b) * BLOCK_SIZE, groundY - 130f, type = BlockType.BRICK))
                        if (Random.nextBoolean()) {
                            items.add(Item((i + b) * BLOCK_SIZE + 20f, groundY - 40f, ItemType.COIN)) // item under obstacle
                        }
                    }
                    i += numBlocks - 1
                } else if (rand < 0.5f) {
                    // Normal floating platform
                    val numBlocks = Random.nextInt(1, 4)
                    for (b in 0 until numBlocks) {
                         val type = if (Random.nextFloat() < 0.3f) BlockType.QUESTION else BlockType.BRICK
                         blocks.add(Block((i + b) * BLOCK_SIZE, groundY - 4 * BLOCK_SIZE, type = type))
                         if (type == BlockType.QUESTION) {
                             items.add(Item((i + b) * BLOCK_SIZE + 20f, groundY - 4 * BLOCK_SIZE - 60f, ItemType.COIN))
                         }
                    }
                    i += numBlocks - 1
                } else if (rand < 0.6f) {
                    val decorType = if (Random.nextBoolean()) BlockType.BUSH else BlockType.LOG
                    decor.add(Block(i * BLOCK_SIZE, groundY - BLOCK_SIZE, type = decorType))
                } else {
                    // Free floating items occasionally
                    if (Random.nextFloat() < 0.1f) {
                         items.add(Item(i * BLOCK_SIZE + 20f, groundY - BLOCK_SIZE - 20f, ItemType.COIN))
                    }
                }
            }
            i++
        }
    }

    fun jump() {
        if (!player.isJumping) {
            player.vy = JUMP_VELOCITY
            player.isJumping = true
        }
    }

    fun update(dt: Float) {
        player.isCrouching = moveCrouch
        
        // Endless runner, auto-move right
        player.vx = MOVE_SPEED
        player.facingRight = true

        player.vy += GRAVITY * dt
        if (player.vy > MAX_FALL_SPEED) player.vy = MAX_FALL_SPEED

        player.x += player.vx * dt
        checkCollisionsX()

        player.y += player.vy * dt
        checkCollisionsY()
        
        checkItemCollisions()
        
        player.updateState(dt)

        // Death pit
        if (player.y > 1500f) {
            resetPlayer()
        }

        // Camera follow (strict follow for endless runner)
        cameraX = player.x - 200f
        if (cameraX < 0f) cameraX = 0f
        
        // score = score + kotlin.math.max(0, (player.x / 1000f).toInt()) // Only score coins/fruits

        frameCount++ // Trigger Compose recomposition
    }

    private fun checkItemCollisions() {
        val pBounds = player.bounds
        for (item in items) {
            if (!item.collected && pBounds.overlaps(item.bounds)) {
                item.collected = true
                score += if (item.type == ItemType.COIN) 10 else 50
            }
        }
    }

    private fun checkCollisionsX() {
        val pBounds = player.bounds
        for (b in blocks) {
            if (pBounds.overlaps(b.bounds)) {
                if (player.vx > 0) {
                    player.x = b.bounds.left - player.width
                } else if (player.vx < 0) {
                    player.x = b.bounds.right
                }
                player.vx = 0f
            }
        }
    }

    private fun checkCollisionsY() {
        val pBounds = player.bounds
        var onGround = false
        for (b in blocks) {
            if (pBounds.overlaps(b.bounds)) {
                if (player.vy > 0) {
                    player.y = b.bounds.top - player.height
                    player.isJumping = false
                    onGround = true
                } else if (player.vy < 0) {
                    player.y = b.bounds.bottom - if (player.isCrouching) player.height / 2f else 0f
                    
                    // Hit question block logic could go here
                    if (b.type == BlockType.QUESTION) {
                        score += 10
                        // Could turn into BRICK here
                    }
                }
                player.vy = 0f
            }
        }
        if (!onGround && player.vy > 0) {
            player.isJumping = true
        }
    }

    private fun resetPlayer() {
        player.x = 100f
        player.y = 100f
        player.vx = 0f
        player.vy = 0f
        player.isJumping = true
        cameraX = 0f
        score = 0
        generateLevel()
    }
}
