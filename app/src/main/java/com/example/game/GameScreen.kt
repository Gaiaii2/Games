package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreen() {
    val engine = remember { GameEngine() }
    
    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val time = withFrameNanos { it }
            val dt = (time - lastTime) / 1_000_000_000f
            lastTime = time
            engine.update(minOf(dt, 0.05f))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF5C94FC))) { // Theme Sky blue
        GameCanvas(engine)
        
        // UI Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            // Upper Left: Combined stats box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                // Main stats pill
                Row(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(32.dp))
                        .border(3.dp, Color(0xFF5D4037), RoundedCornerShape(32.dp))
                        .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Capybara Head Mock
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF87CEEB), CircleShape)
                            .border(2.dp, Color(0xFF5D4037), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(32.dp).background(Color(0xFFD4A373), CircleShape))
                        // Simple face
                        Box(modifier = Modifier.offset(x = 8.dp, y = (-2).dp).size(4.dp).background(Color.Black, CircleShape))
                        Box(modifier = Modifier.offset(x = 12.dp, y = 6.dp).size(8.dp).background(Color(0xFF8B5A2B), RoundedCornerShape(2.dp)))
                    }
                    
                    // Two Hearts
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) {
                            // Mocking a heart visual with a red circle for now
                            Text("❤️", fontSize = 28.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Backpack Mock
                    Text("🎒", fontSize = 32.sp)
                }

                // Score section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Paw coin
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFBBF24), CircleShape)
                            .border(3.dp, Color(0xFFD97706), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐾", fontSize = 20.sp)
                    }
                    
                    Text(
                        text = engine.score.toString(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
            
            // Upper Middle: LEVEL 1: CAPYBARA RUN!
            Text(
                "LEVEL 1: CAPYBARA RUN!",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 80.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
        
        ControlsOverlay(engine)
    }
}

@Composable
fun GameCanvas(engine: GameEngine) {
    val frameCount = engine.frameCount
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Sky Gradient is drawn by Box background, or we can draw it here. 
        // We'll let Box handle the solid sky color, and draw mountains here.
        val mountainColor = Color(0xFF90A4AE) // Mountain grayish blue
        val hillColor1 = Color(0xFFC5E1A5) // Distant hill light green
        val hillColor2 = Color(0xFFAED581) // Closer hill darker green
        
        val horizonY = size.height * 0.7f

        // Distant Mountains (parallax slowness 0.1)
        val mntXOffset = -(engine.cameraX * 0.1f) % size.width
        val pathMnt = Path().apply {
            moveTo(mntXOffset, horizonY)
            lineTo(mntXOffset + size.width * 0.4f, horizonY - size.height * 0.5f)
            lineTo(mntXOffset + size.width * 0.8f, horizonY)
            close()
            // Wrap around
            val offsetMntX = mntXOffset + size.width
            moveTo(offsetMntX, horizonY)
            lineTo(offsetMntX + size.width * 0.4f, horizonY - size.height * 0.5f)
            lineTo(offsetMntX + size.width * 0.8f, horizonY)
            close()
            // Snow cap
        }
        drawPath(pathMnt, mountainColor)

        // Snow Peak overlay (simple)
        val pathSnow = Path().apply {
            moveTo(mntXOffset + size.width * 0.4f, horizonY - size.height * 0.5f)
            lineTo(mntXOffset + size.width * 0.4f - 50f, horizonY - size.height * 0.5f + 80f)
            lineTo(mntXOffset + size.width * 0.4f + 50f, horizonY - size.height * 0.5f + 80f)
            close()

            val offsetMntX = mntXOffset + size.width
            moveTo(offsetMntX + size.width * 0.4f, horizonY - size.height * 0.5f)
            lineTo(offsetMntX + size.width * 0.4f - 50f, horizonY - size.height * 0.5f + 80f)
            lineTo(offsetMntX + size.width * 0.4f + 50f, horizonY - size.height * 0.5f + 80f)
            close()
        }
        drawPath(pathSnow, Color.White)


        // Rolling Hills Distant (parallax slowness 0.2)
        val hill1XOffset = -(engine.cameraX * 0.2f) % (size.width * 1.5f)
        drawCircle(
            color = hillColor1,
            radius = size.width * 0.6f,
            center = Offset(hill1XOffset + size.width * 0.5f, horizonY + size.width * 0.2f)
        )
        drawCircle(
            color = hillColor1,
            radius = size.width * 0.6f,
            center = Offset(hill1XOffset + size.width * 2f, horizonY + size.width * 0.2f)
        )

        // Rolling Hills Closer (parallax slowness 0.3)
        val hill2XOffset = -(engine.cameraX * 0.3f) % (size.width * 1.2f)
        drawCircle(
            color = hillColor2,
            radius = size.width * 0.8f,
            center = Offset(hill2XOffset, horizonY + size.width * 0.5f)
        )
        drawCircle(
            color = hillColor2,
            radius = size.width * 0.8f,
            center = Offset(hill2XOffset + size.width * 1.2f, horizonY + size.width * 0.5f)
        )
        
        // Palm Trees
        val treeSpacing = 600f
        val treeShift = -(engine.cameraX * 0.4f) % treeSpacing
        for (i in 0..5) {
            val tx = treeShift + i * treeSpacing - 100f
            // Trunk
            drawRoundRect(color = Color(0xFFC78440), topLeft = Offset(tx - 10f, horizonY - 150f), size = Size(20f, 150f), cornerRadius = CornerRadius(5f))
            // Leaves
            drawCircle(color = Color(0xFF539420), radius = 50f, center = Offset(tx, horizonY - 150f))
            drawCircle(color = Color(0xFF67B228), radius = 30f, center = Offset(tx + 20f, horizonY - 160f))
        }
        
        translate(left = -engine.cameraX) {
            for (b in engine.decor) {
                drawDecor(b)
            }
            for (b in engine.blocks) {
                drawBlock(b)
            }
            for (item in engine.items) {
                if (!item.collected) {
                    drawItem(item)
                }
            }
            drawPlayer(engine.player)
        }
    }
}

fun DrawScope.drawItem(item: Item) {
    if (item.type == ItemType.COIN) {
        val cx = item.x + item.size / 2f
        val cy = item.y + item.size / 2f
        drawCircle(color = Color(0xFFFBBF24), radius = item.size / 2f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFFD97706), radius = item.size / 2f, center = Offset(cx, cy), style = Stroke(width = 3f))
        // tiny inner dot replacing complex paw for scale
        drawCircle(color = Color(0xFFD97706), radius = item.size / 5f, center = Offset(cx, cy))
    } else {
        // Red fruit (strawberry-like)
        val cx = item.x + item.size / 2f
        val cy = item.y + item.size / 2f
        drawCircle(color = Color(0xFFEF4444), radius = item.size / 2f, center = Offset(cx, cy + 5f))
        // Fruit stem
        val path = Path().apply {
            moveTo(cx, cy - item.size / 2f + 5f)
            lineTo(cx - 8f, cy - item.size / 2f - 5f)
            lineTo(cx + 8f, cy - item.size / 2f - 5f)
            close()
        }
        drawPath(path, color = Color(0xFF22C55E))
    }
}

fun DrawScope.drawDecor(b: Block) {
    when (b.type) {
        BlockType.WATER -> {
            drawRect(color = Color(0xFF63B4D1), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height))
            drawRect(color = Color(0x3CF1F8FF), topLeft = Offset(b.x, b.y), size = Size(b.width, 10f))
        }
        BlockType.BUSH -> {
            drawCircle(color = Color(0xFF6DB139), radius = b.width/2, center = Offset(b.x + b.width/2, b.y + b.height/2))
            drawCircle(color = Color(0xFF6DB139), radius = b.width/2.5f, center = Offset(b.x + b.width/4, b.y + b.height/2 + 10f))
            drawCircle(color = Color(0xFF6DB139), radius = b.width/2.5f, center = Offset(b.x + b.width*0.75f, b.y + b.height/2 + 10f))
        }
        BlockType.LOG -> {
            // Draw a cylindrical log lying horizontally on the ground
            drawRoundRect(color = Color(0xFF8B5A2B), topLeft = Offset(b.x, b.y + b.height - 30f), size = Size(b.width * 1.5f, 30f), cornerRadius = CornerRadius(10f))
            drawCircle(color = Color(0xFFDEB887), radius = 12f, center = Offset(b.x + 10f, b.y + b.height - 15f))
            drawCircle(color = Color(0xFF6B4226), radius = 6f, center = Offset(b.x + 10f, b.y + b.height - 15f))
        }
        else -> {}
    }
}

fun DrawScope.drawBlock(b: Block) {
    when (b.type) {
        BlockType.GROUND -> {
            drawRect(color = Color(0xFF7A4B3A), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height))
            drawRect(color = Color(0xFF7CBA3D), topLeft = Offset(b.x, b.y), size = Size(b.width, 20f))
        }
        BlockType.DIRT -> {
            drawRect(color = Color(0xFF7A4B3A), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height))
            drawRect(color = Color(0xFF693E30), topLeft = Offset(b.x + 10f, b.y + 10f), size = Size(10f, 10f))
            drawRect(color = Color(0xFF693E30), topLeft = Offset(b.x + 40f, b.y + 30f), size = Size(15f, 10f))
        }
        BlockType.BRICK -> {
            drawRect(color = Color(0xFFC75B39), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height))
            drawRect(color = Color(0xFFA04225), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height), style = Stroke(width = 4f))
            drawLine(color = Color(0xFFA04225), start = Offset(b.x, b.y + b.height/2), end = Offset(b.x + b.width, b.y + b.height/2), strokeWidth = 4f)
            drawLine(color = Color(0xFFA04225), start = Offset(b.x + b.width/2, b.y), end = Offset(b.x + b.width/2, b.y + b.height/2), strokeWidth = 4f)
        }
        BlockType.QUESTION -> {
            drawRoundRect(color = Color(0xFFF1C40F), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height), cornerRadius = CornerRadius(8f))
            drawRoundRect(color = Color(0xFFD4AC0D), topLeft = Offset(b.x, b.y), size = Size(b.width, b.height), cornerRadius = CornerRadius(8f), style = Stroke(width = 6f))
            drawCircle(color = Color(0xFFD4AC0D), radius = 6f, center = Offset(b.x + b.width/2, b.y + b.height - 20f))
            val path = Path().apply {
                moveTo(b.x + b.width/2 - 10f, b.y + b.height/2 - 5f)
                cubicTo(b.x + b.width/2 - 10f, b.y + 10f, b.x + b.width/2 + 20f, b.y + 10f, b.x + b.width/2 + 10f, b.y + b.height/2 + 10f)
                lineTo(b.x + b.width/2, b.y + b.height/2 + 15f)
            }
            drawPath(path, color = Color(0xFFD4AC0D), style = Stroke(width = 8f))
        }
        else -> {}
    }
}

fun DrawScope.drawPlayer(p: Player) {
    val bBody = Color(0xFF8B5A2B)
    val bHead = Color(0xFF8B5A2B)
    val bSnout = Color(0xFF6B4226)
    val bPack = Color(0xFF6B8E23)

    val drawY = if (p.isCrouching) p.y + p.height / 2f else p.y
    val drawHeight = if (p.isCrouching) p.height / 2f else p.height

    // Body
    drawRoundRect(
        color = bBody,
        topLeft = Offset(p.x, drawY + 15f),
        size = Size(p.width, drawHeight - 15f),
        cornerRadius = CornerRadius(20f)
    )
    
    // Head
    val headX = if (p.facingRight) p.x + p.width - 35f else p.x - 5f
    val headYObjOffset = if (p.isCrouching) drawY else drawY + 5f // adjust head position when crouched
    drawRoundRect(
        color = bHead,
        topLeft = Offset(headX, headYObjOffset),
        size = Size(40f, if (p.isCrouching) 30f else 35f),
        cornerRadius = CornerRadius(15f)
    )
    
    // Snout
    val snoutWidth = 20f
    val snoutX = if (p.facingRight) headX + 30f else headX - 10f
    drawRoundRect(
        color = bSnout,
        topLeft = Offset(snoutX, headYObjOffset + 13f),
        size = Size(snoutWidth, 15f),
        cornerRadius = CornerRadius(8f)
    )
    
    // Eye
    val eyeX = if (p.facingRight) headX + 25f else headX + 15f
    // blink logic roughly
    if (p.stateTime.toInt() % 4 != 3) {
        drawCircle(
            color = Color.Black,
            radius = 3f,
            center = Offset(eyeX, headYObjOffset + 11f)
        )
    } else {
        drawLine(Color.Black, Offset(eyeX-3f, headYObjOffset+11f), Offset(eyeX+3f, headYObjOffset+11f), strokeWidth = 2f)
    }

    // Backpack
    val packX = if (p.facingRight) p.x - 10f else p.x + p.width - 15f
    drawRoundRect(
        color = bPack,
        topLeft = Offset(packX, drawY + 15f),
        size = Size(25f, drawHeight - 20f),
        cornerRadius = CornerRadius(8f)
    )
    
    // Legs
    val legOffset = if (p.state == Player.State.WALKING) kotlin.math.sin(p.stateTime * 15f) * 12f else 0f
    
    val frontLegX = if (p.facingRight) p.x + p.width - 25f else p.x + 15f
    val backLegX = if (p.facingRight) p.x + 15f else p.x + p.width - 25f
    
    if (p.isJumping) {
        // Jumping pose: legs spread out
        drawRoundRect(
            color = bSnout,
            topLeft = Offset(frontLegX + 15f, drawY + drawHeight - 15f),
            size = Size(10f, 15f),
            cornerRadius = CornerRadius(5f)
        )
        drawRoundRect(
            color = bSnout,
            topLeft = Offset(backLegX - 15f, drawY + drawHeight - 15f),
            size = Size(10f, 15f),
            cornerRadius = CornerRadius(5f)
        )
    } else {
        // Walking / Idle
        drawRoundRect(
            color = bSnout,
            topLeft = Offset(frontLegX + legOffset, drawY + drawHeight - 5f),
            size = Size(10f, 15f),
            cornerRadius = CornerRadius(5f)
        )
        drawRoundRect(
            color = bSnout,
            topLeft = Offset(backLegX - legOffset, drawY + drawHeight - 5f),
            size = Size(10f, 15f),
            cornerRadius = CornerRadius(5f)
        )
    }
}

@Composable
fun ControlsOverlay(engine: GameEngine) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left half for crouch
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            engine.moveCrouch = event.changes.any { it.pressed }
                        }
                    }
                },
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .size(140.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .border(3.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⬇", fontSize = 64.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("CROUCH", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        // Right half for jump
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    // Tap gestures for jump (trigger on down)
                    detectTapGestures(
                        onPress = {
                            engine.jump()
                        }
                    )
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .size(140.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .border(3.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⬆", fontSize = 64.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("JUMP", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
