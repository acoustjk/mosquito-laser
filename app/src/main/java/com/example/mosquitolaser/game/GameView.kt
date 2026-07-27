package com.example.mosquitolaser.game

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.mosquitolaser.audio.SoundManager
import com.example.mosquitolaser.game.effects.ParticleSystem
import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle
import com.example.mosquitolaser.game.stages.StageData
import com.example.mosquitolaser.game.stages.WorldTheme
import kotlin.math.*

/**
 * Main game rendering view. Uses SurfaceView for smooth 60fps rendering.
 * Handles all drawing: background, laser, mirrors, mosquitoes, UI overlay.
 */
class GameView(context: Context, val stage: StageData, val soundManager: SoundManager) :
    SurfaceView(context), SurfaceHolder.Callback, Runnable {

    val engine = GameEngine(stage)
    private val particleSystem = ParticleSystem()

    // Game loop
    private var gameThread: Thread? = null
    private var running = false
    private var lastTimeMs = 0L

    // Canvas dimensions
    private var w = 0f
    private var h = 0f

    // ── Angle Dial state ──────────────────────────────────────────────────────
    // The dial sits at the bottom-centre of the screen.
    // It is only interactive when a mirror is selected.
    private var dialCenterX = 0f
    private var dialCenterY = 0f
    private val dialRadius get() = minOf(w, h) * 0.14f
    private val dialPanelHeight get() = dialRadius * 2.8f

    // Touch tracking for the dial
    private var dialTouching = false          // finger is on the dial
    private var dialStartAngle = 0f           // angle at finger-down
    private var dialStartMirrorAngle = 0f     // mirror angle at finger-down
    private var lastDialAngleDeg = 0f         // last computed finger angle (degrees)

    // ── Dial Paint objects ───────────────────────────────────────────────────
    private val dialBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(210, 10, 12, 30)
    }
    private val dialRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(120, 100, 140, 255)
    }
    private val dialAllowedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        color = Color.argb(160, 80, 200, 255)
    }
    private val dialNeedlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        color = Color.argb(255, 255, 240, 80)
    }
    private val dialKnobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(255, 255, 240, 80)
    }
    private val dialTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val dialHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 180, 200, 255)
        textAlign = Paint.Align.CENTER
    }
    private val dialLimitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(100, 255, 100, 100)
        pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
    }

    // Mosquito wing animation
    private var wingAngle = 0f
    private var wingDir = 1f

    // Callbacks
    var onStageClear: (() -> Unit)? = null
    var onStageFail: ((GameEngine.FailReason) -> Unit)? = null

    // Paint objects
    private val bgPaint = Paint()
    private val laserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    private val laserGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
    }
    private val laserGlow2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }
    private val mirrorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }
    private val mirrorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val mirrorGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }
    private val mosquitoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val mosquitoWingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 150
    }
    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val obstacleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val hudSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 200, 200, 200)
        textSize = 28f
    }
    private val hudBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 0, 0, 30)
    }
    private val conditionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 220, 50)
        textSize = 26f
    }
    private val selectedRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }
    private val allowedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(120, 255, 255, 255)
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }
    private val forbiddenZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 255, 0, 0)
    }
    private val forbiddenZoneStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(180, 255, 50, 50)
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val laserSourcePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val laserSourceGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }
    private val timerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 56f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    // Background stars (random positions, cached)
    private val stars = (0..120).map {
        Triple(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat() * 3f + 1f)
    }

    init {
        holder.addCallback(this)
        setLayerType(LAYER_TYPE_HARDWARE, null)

        engine.onMosquitoKilled = { mosquito ->
            post {
                soundManager.playMosquitoDie()
                particleSystem.emitMosquitoDeath(mosquito.x, mosquito.y, w, h)
            }
        }
        engine.onStageClear = {
            post {
                soundManager.playStageClear()
                onStageClear?.invoke()
            }
        }
        engine.onStageFail = { reason ->
            post {
                soundManager.playStageFail()
                onStageFail?.invoke(reason)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        lastTimeMs = SystemClock.elapsedRealtime()
        gameThread = Thread(this, "GameThread").also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        w = width.toFloat()
        h = height.toFloat()
        engine.canvasW = w
        engine.canvasH = h
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        gameThread?.join()
    }

    override fun run() {
        while (running) {
            val now = SystemClock.elapsedRealtime()
            val delta = (now - lastTimeMs).coerceAtMost(50)
            lastTimeMs = now

            engine.update(delta)
            wingAngle += wingDir * delta * 0.4f
            if (wingAngle > 30f) wingDir = -1f
            if (wingAngle < -30f) wingDir = 1f

            // Emit sizzle sparks for mosquitoes currently being burned by laser
            for (mosquito in engine.mosquitoes) {
                if (mosquito.isAlive && mosquito.isBeingHitByLaser) {
                    particleSystem.emitLaserSpark(mosquito.x, mosquito.y, w, h, Color.YELLOW)
                }
            }

            particleSystem.update(delta)

            val canvas = holder.lockCanvas() ?: continue
            try {
                drawFrame(canvas)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            // Target ~60fps
            val frameTime = SystemClock.elapsedRealtime() - now
            if (frameTime < 16) Thread.sleep(16 - frameTime)
        }
    }

    private fun drawFrame(canvas: Canvas) {
        // Background
        drawBackground(canvas)

        // Forbidden zones
        drawForbiddenZones(canvas)

        // Obstacles
        drawObstacles(canvas)

        // Laser
        drawLaser(canvas)

        // Mirrors
        drawMirrors(canvas)

        // Laser source
        drawLaserSource(canvas)

        // Mosquitoes
        drawMosquitoes(canvas)

        // Particles
        particleSystem.draw(canvas)

        // HUD
        drawHUD(canvas)

        // Angle dial (drawn last so it appears on top)
        drawAngleDial(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val theme = stage.worldTheme
        val (colorTop, colorBot) = when (theme) {
            WorldTheme.NIGHT -> Color.parseColor("#050510") to Color.parseColor("#0A0A25")
            WorldTheme.CITY -> Color.parseColor("#0A0A1A") to Color.parseColor("#1A0A2A")
            WorldTheme.JUNGLE -> Color.parseColor("#041008") to Color.parseColor("#081A0C")
            WorldTheme.FACTORY -> Color.parseColor("#0F0A08") to Color.parseColor("#1A1008")
            WorldTheme.VOLCANO -> Color.parseColor("#180800") to Color.parseColor("#2A0A00")
            WorldTheme.SPACE -> Color.parseColor("#000008") to Color.parseColor("#00000F")
        }

        val gradient = LinearGradient(0f, 0f, 0f, h, colorTop, colorBot, Shader.TileMode.CLAMP)
        bgPaint.shader = gradient
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Stars
        for ((sx, sy, sz) in stars) {
            starPaint.color = when {
                sz > 3f -> Color.argb(200, 255, 255, 200)
                sz > 2f -> Color.argb(150, 200, 200, 255)
                else -> Color.argb(100, 180, 180, 220)
            }
            val sparkle = (1f + sin(SystemClock.elapsedRealtime() * 0.002f + sx * 10f)) * 0.5f
            canvas.drawCircle(sx * w, sy * h, sz * (0.7f + sparkle * 0.3f), starPaint)
        }

        // World-specific atmosphere
        when (theme) {
            WorldTheme.VOLCANO -> {
                // Orange glow at bottom
                val volcanoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(w / 2f, h, h * 0.6f,
                        Color.argb(80, 255, 100, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, w, h, volcanoPaint)
            }
            WorldTheme.SPACE -> {
                // Nebula effect
                val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(w * 0.7f, h * 0.3f, h * 0.5f,
                        Color.argb(30, 100, 0, 150), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, w, h, nebulaPaint)
            }
            else -> {}
        }
    }

    private fun drawForbiddenZones(canvas: Canvas) {
        for (zone in stage.condition.forbiddenZones) {
            val rect = RectF(zone.left * w, zone.top * h, zone.right * w, zone.bottom * h)
            canvas.drawRect(rect, forbiddenZonePaint)
            canvas.drawRect(rect, forbiddenZoneStrokePaint)

            // X pattern
            val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(80, 255, 0, 0)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, xPaint)
            canvas.drawLine(rect.right, rect.top, rect.left, rect.bottom, xPaint)
        }
    }

    private fun drawObstacles(canvas: Canvas) {
        for (obstacle in stage.obstacles) {
            val rect = obstacle.toPixelRect(w, h)
            val bgColor = if (obstacle.isSemiTransparent)
                Color.argb(100, 100, 150, 200)
            else
                Color.argb(220, 60, 70, 100)
            obstaclePaint.color = bgColor

            val shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                Color.argb(255, 80, 90, 130), Color.argb(255, 40, 50, 80),
                Shader.TileMode.CLAMP
            )
            if (!obstacle.isSemiTransparent) {
                obstaclePaint.shader = shader
            }
            canvas.drawRoundRect(rect, 8f, 8f, obstaclePaint)

            obstacleStrokePaint.color = if (obstacle.isSemiTransparent)
                Color.argb(150, 150, 200, 255) else Color.argb(180, 100, 120, 180)
            canvas.drawRoundRect(rect, 8f, 8f, obstacleStrokePaint)

            if (obstacle.isSemiTransparent) {
                // Grid lines to indicate semi-transparent
                val gridPaint = Paint().apply {
                    this.color = Color.argb(60, 150, 200, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                    pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
                }
                var gy = rect.top + 20f
                while (gy < rect.bottom) {
                    canvas.drawLine(rect.left, gy, rect.right, gy, gridPaint)
                    gy += 20f
                }
            }
        }
    }

    private fun drawLaser(canvas: Canvas) {
        val result = engine.laserResult ?: return
        val laserColor = stage.laserSource.color

        // Determine if forbidden zone is hit -> red laser
        val effectiveColor = if (result.hitForbiddenZone) Color.RED else laserColor

        for (seg in result.segments) {
            val x1 = seg.x1 * w
            val y1 = seg.y1 * h
            val x2 = seg.x2 * w
            val y2 = seg.y2 * h

            // Outer glow
            laserGlowPaint.color = effectiveColor
            laserGlowPaint.alpha = 60
            canvas.drawLine(x1, y1, x2, y2, laserGlowPaint)

            // Mid glow
            laserGlow2Paint.color = effectiveColor
            laserGlow2Paint.alpha = 120
            canvas.drawLine(x1, y1, x2, y2, laserGlow2Paint)

            // Core laser
            laserPaint.color = Color.WHITE
            laserPaint.alpha = 230
            canvas.drawLine(x1, y1, x2, y2, laserPaint)

            // Bounce point sparks
            if (seg.bounceIndex > 0) {
                val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = effectiveColor
                    style = Paint.Style.FILL
                    maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawCircle(x1, y1, 6f, sparkPaint)
            }
        }
    }

    private fun drawLaserSource(canvas: Canvas) {
        val src = stage.laserSource
        val px = src.x * w
        val py = src.y * h
        val laserColor = src.color

        // Outer glow
        laserSourceGlowPaint.color = laserColor
        canvas.drawCircle(px, py, 28f, laserSourceGlowPaint)

        // Body
        laserSourcePaint.color = Color.argb(255, 30, 30, 60)
        canvas.drawCircle(px, py, 18f, laserSourcePaint)

        // Aperture ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = laserColor
            strokeWidth = 3f
        }
        canvas.drawCircle(px, py, 18f, ringPaint)

        // Direction arrow
        val arrowRad = Math.toRadians(src.angle.toDouble())
        val ax = px + cos(arrowRad).toFloat() * 12f
        val ay = py + sin(arrowRad).toFloat() * 12f
        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = laserColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(ax, ay, 5f, arrowPaint)
    }

    private fun drawMirrors(canvas: Canvas) {
        for (mirror in engine.mirrors) {
            drawMirror(canvas, mirror)
        }
    }

    private fun drawMirror(canvas: Canvas, mirror: Mirror) {
        val px = mirror.x * w
        val py = mirror.y * h
        val mirrorAngleRad = Math.toRadians(mirror.angle.toDouble())
        val halfLen = mirror.size * min(w, h) / 2f
        val cos = cos(mirrorAngleRad).toFloat()
        val sin = sin(mirrorAngleRad).toFloat()

        val x1 = px - cos * halfLen
        val y1 = py - sin * halfLen
        val x2 = px + cos * halfLen
        val y2 = py + sin * halfLen

        val isSelected = mirror.id == engine.selectedMirrorId
        val isMovable = mirror.isMovable

        // Mirror glow
        mirrorGlowPaint.color = when {
            isSelected -> Color.argb(150, 255, 255, 100)
            isMovable -> Color.argb(80, 150, 220, 255)
            else -> Color.argb(40, 200, 200, 200)
        }
        canvas.drawLine(x1, y1, x2, y2, mirrorGlowPaint)

        // Mirror surface (metallic silver)
        val metalGrad = LinearGradient(x1, y1, x2, y2,
            Color.argb(255, 220, 230, 255),
            Color.argb(255, 150, 160, 200),
            Shader.TileMode.CLAMP
        )
        mirrorPaint.shader = metalGrad
        mirrorPaint.color = Color.WHITE
        mirrorPaint.strokeWidth = if (isSelected) 8f else 5f
        canvas.drawLine(x1, y1, x2, y2, mirrorPaint)

        // Highlight strip
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(180, 255, 255, 255)
        }
        val hx1 = x1 + sin * 2f
        val hy1 = y1 - cos * 2f
        val hx2 = x2 + sin * 2f
        val hy2 = y2 - cos * 2f
        canvas.drawLine(hx1, hy1, hx2, hy2, highlightPaint)

        // Center dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isMovable) Color.argb(200, 100, 200, 255) else Color.argb(150, 200, 200, 200)
        }
        canvas.drawCircle(px, py, 7f, dotPaint)

        // Movable indicator - rotation arrows
        if (isMovable && !isSelected) {
            val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.argb(150, 100, 200, 255)
            }
            canvas.drawCircle(px, py, 20f, indicatorPaint)
        }

        // Selected: show allowed arc + selection ring
        if (isSelected) {
            selectedRingPaint.color = Color.argb(200, 255, 255, 100)
            canvas.drawCircle(px, py, 28f, selectedRingPaint)

            // Show allowed rotation arc
            val arcRect = RectF(px - 45f, py - 45f, px + 45f, py + 45f)
            canvas.drawArc(arcRect, mirror.minAngle, mirror.maxAngle - mirror.minAngle,
                false, allowedArcPaint)
        }

        // Lock icon for non-movable
        if (!isMovable) {
            val lockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 150, 150, 180)
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🔒", px, py - 24f, lockPaint)
        }
    }

    private fun drawMosquitoes(canvas: Canvas) {
        for (mosquito in engine.mosquitoes) {
            if (mosquito.isAlive) drawMosquito(canvas, mosquito)
        }
    }

    private fun drawMosquito(canvas: Canvas, mosquito: Mosquito) {
        val px = mosquito.x * w
        val py = mosquito.y * h
        val baseSize = mosquito.hitRadius * min(w, h)

        // Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawOval(px - baseSize, py + baseSize * 0.6f, px + baseSize, py + baseSize * 0.8f, shadowPaint)

        // Wings (animated)
        mosquitoWingPaint.color = Color.argb(140, 150, 220, 255)
        mosquitoWingPaint.style = Paint.Style.FILL

        canvas.save()
        canvas.translate(px, py)
        canvas.rotate(wingAngle)
        // Left wing
        canvas.drawOval(-baseSize * 2.2f, -baseSize * 0.5f, -baseSize * 0.3f, -baseSize * 1.5f, mosquitoWingPaint)
        // Right wing
        canvas.drawOval(baseSize * 0.3f, -baseSize * 0.5f, baseSize * 2.2f, -baseSize * 1.5f, mosquitoWingPaint)
        canvas.restore()

        // Body
        val bodyGrad = RadialGradient(px, py - baseSize * 0.2f, baseSize * 0.8f,
            Color.argb(255, 60, 20, 10),
            Color.argb(255, 30, 10, 5),
            Shader.TileMode.CLAMP
        )
        mosquitoPaint.shader = bodyGrad
        canvas.drawOval(px - baseSize * 0.5f, py - baseSize * 0.8f,
            px + baseSize * 0.5f, py + baseSize * 0.8f, mosquitoPaint)

        // Abdomen stripes
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 180, 0)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        for (i in 0..2) {
            val sy = py - baseSize * 0.3f + i * baseSize * 0.35f
            val sw = baseSize * (0.45f - i * 0.05f)
            canvas.drawLine(px - sw, sy, px + sw, sy, stripePaint)
        }

        // Head
        val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 50, 20, 10)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(px, py - baseSize * 0.9f, baseSize * 0.35f, headPaint)

        // Eyes
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawCircle(px - baseSize * 0.15f, py - baseSize * 0.95f, baseSize * 0.12f, eyePaint)
        canvas.drawCircle(px + baseSize * 0.15f, py - baseSize * 0.95f, baseSize * 0.12f, eyePaint)

        // Proboscis (needle)
        val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 150, 100, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawLine(px, py - baseSize * 1.25f, px, py - baseSize * 1.9f, needlePaint)

        // Movement indicator
        if (mosquito.movementType != Mosquito.MovementType.STATIC) {
            val movePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(100, 255, 200, 0)
                style = Paint.Style.STROKE
                strokeWidth = 2f
                pathEffect = DashPathEffect(floatArrayOf(6f, 4f), (SystemClock.elapsedRealtime() % 100f))
            }
            canvas.drawCircle(px, py, baseSize * 1.4f + mosquito.moveRange * min(w, h) * 0.5f, movePaint)
        }

        // 3-Second Laser Burn Gauge & Heat Aura
        if (mosquito.burnTimeMs > 0L) {
            val progress = mosquito.burnProgress

            // Heat Aura Glow
            val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = if (mosquito.isBeingHitByLaser)
                    Color.argb((150 * progress).toInt(), 255, 100, 0)
                else
                    Color.argb((80 * progress).toInt(), 255, 180, 0)
                maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(px, py, baseSize * (1.2f + progress * 0.8f), auraPaint)

            // Background Arc Track
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                color = Color.argb(100, 50, 50, 50)
            }
            val arcR = baseSize * 1.8f
            val arcRect = RectF(px - arcR, py - arcR, px + arcR, py + arcR)
            canvas.drawArc(arcRect, -90f, 360f, false, trackPaint)

            // Filled Progress Arc (Orange -> Bright Yellow)
            val burnArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                color = if (mosquito.isBeingHitByLaser)
                    Color.argb(255, 255, (100 + 155 * progress).toInt(), 0)
                else
                    Color.argb(180, 255, 150, 0)
            }
            canvas.drawArc(arcRect, -90f, 360f * progress, false, burnArcPaint)

            // Countdown / Burn Percent Text
            val remainingSecs = ((mosquito.requiredBurnTimeMs - mosquito.burnTimeMs) / 1000f).coerceAtLeast(0f)
            val burnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.YELLOW
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(6f, 0f, 0f, Color.RED)
            }
            canvas.drawText(String.format("%.1fs", remainingSecs), px, py - baseSize * 2.2f, burnTextPaint)
        }
    }

    private fun drawHUD(canvas: Canvas) {
        // HUD background
        val hudHeight = 100f
        canvas.drawRect(0f, 0f, w, hudHeight, hudBgPaint)

        // Stage number
        hudPaint.textSize = 36f
        hudPaint.color = Color.WHITE
        canvas.drawText("Stage ${stage.stageNumber}", 20f, 45f, hudPaint)

        // World name
        hudSmallPaint.textSize = 22f
        val worldName = getWorldName(stage.worldNumber)
        canvas.drawText(worldName, 20f, 75f, hudSmallPaint)

        // Mosquito count
        val killed = engine.mosquitoesKilled
        val total = engine.totalMosquitoes
        hudPaint.textSize = 34f
        hudPaint.textAlign = Paint.Align.CENTER
        hudPaint.color = if (killed == total && total > 0) Color.GREEN else Color.WHITE
        canvas.drawText("🦟 $killed / $total", w * 0.42f, 45f, hudPaint)

        // Live score
        val currentScore = engine.totalScore
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🏆 ${String.format("%,d", currentScore)}", w * 0.42f, 78f, scorePaint)
        hudPaint.textAlign = Paint.Align.LEFT

        // Timer
        if (stage.condition.hasTimeLimit()) {
            val secs = engine.remainingSeconds
            val urgency = secs <= 10
            timerPaint.color = if (urgency) {
                val blink = ((SystemClock.elapsedRealtime() / 300) % 2 == 0L)
                if (blink) Color.RED else Color.argb(200, 255, 100, 100)
            } else Color.argb(220, 255, 220, 100)
            timerPaint.textAlign = Paint.Align.RIGHT
            timerPaint.textSize = if (urgency) 50f else 40f
            canvas.drawText(String.format("%02d", secs), w - 20f, 50f, timerPaint)
            timerPaint.textSize = 20f
            timerPaint.color = Color.argb(180, 200, 200, 200)
            canvas.drawText("SEC", w - 20f, 75f, timerPaint)
        }

        // Reflections indicator
        if (stage.condition.hasMinReflections()) {
            val refCount = engine.reflectionCount
            val minRef = stage.condition.minReflections
            val refColor = if (refCount >= minRef) Color.GREEN else Color.argb(200, 255, 200, 50)
            val refPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = refColor
                textSize = 24f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("↗ $refCount / $minRef", 20f, hudHeight + 30f, refPaint)
        }

        // Mirror limit indicator
        if (stage.condition.hasMirrorLimit()) {
            val movedCount = engine.mirrorsMoved
            val maxMirrors = stage.condition.maxMovableMirrors
            val mColor = if (movedCount <= maxMirrors) Color.argb(200, 100, 200, 255) else Color.RED
            val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = mColor
                textSize = 24f
                textAlign = Paint.Align.LEFT
            }
            val offset = if (stage.condition.hasMinReflections()) 60f else 30f
            canvas.drawText("🪞 $movedCount / $maxMirrors", 20f, hudHeight + offset, mPaint)
        }
    }

    private fun getWorldName(world: Int) = when (world) {
        1 -> "🌙 월드 1: 입문"
        2 -> "🌆 월드 2: 도시"
        3 -> "🌿 월드 3: 정글"
        4 -> "🏭 월드 4: 공장"
        5 -> "🌋 월드 5: 화산"
        6 -> "👾 월드 6: 우주"
        else -> "월드 $world"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ANGLE DIAL
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawAngleDial(canvas: Canvas) {
        val selectedMirror = engine.mirrors.find { it.id == engine.selectedMirrorId }

        // Always update dial centre (needed for touch hit-test)
        dialCenterX = w / 2f
        dialCenterY = h - dialRadius - 32f

        // Panel background
        val panelTop = h - dialPanelHeight
        val panelRect = RectF(0f, panelTop, w, h)
        canvas.drawRoundRect(panelRect, 24f, 24f, dialBgPaint)

        if (selectedMirror == null) {
            // No mirror selected — show hint
            dialHintPaint.textSize = 28f
            canvas.drawText("🕴 거울을 탭하여 선택하세요", dialCenterX, h - dialRadius * 0.8f, dialHintPaint)
            return
        }

        val curAngle = selectedMirror.angle
        val minA = selectedMirror.minAngle
        val maxA = selectedMirror.maxAngle
        val r = dialRadius

        // Allowed-range arc (background track)
        val arcRect = RectF(dialCenterX - r, dialCenterY - r, dialCenterX + r, dialCenterY + r)
        dialAllowedArcPaint.alpha = 60
        dialAllowedArcPaint.color = Color.argb(60, 80, 200, 255)
        canvas.drawArc(arcRect, minA, maxA - minA, false, dialAllowedArcPaint)

        // Filled arc from minAngle to currentAngle
        dialAllowedArcPaint.alpha = 180
        dialAllowedArcPaint.color = Color.argb(200, 80, 200, 255)
        canvas.drawArc(arcRect, minA, curAngle - minA, false, dialAllowedArcPaint)

        // Outer ring
        dialRingPaint.strokeWidth = 2f
        canvas.drawCircle(dialCenterX, dialCenterY, r, dialRingPaint)

        // Limit lines (min / max)
        for (limitAngle in listOf(minA, maxA)) {
            val rad = Math.toRadians(limitAngle.toDouble())
            canvas.drawLine(
                dialCenterX + cos(rad).toFloat() * (r * 0.7f),
                dialCenterY + sin(rad).toFloat() * (r * 0.7f),
                dialCenterX + cos(rad).toFloat() * r,
                dialCenterY + sin(rad).toFloat() * r,
                dialLimitLinePaint
            )
        }

        // Needle
        val needleRad = Math.toRadians(curAngle.toDouble())
        val nx = dialCenterX + cos(needleRad).toFloat() * (r * 0.82f)
        val ny = dialCenterY + sin(needleRad).toFloat() * (r * 0.82f)
        canvas.drawLine(dialCenterX, dialCenterY, nx, ny, dialNeedlePaint)

        // Knob at needle tip
        dialKnobPaint.maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(nx, ny, 10f, dialKnobPaint)
        dialKnobPaint.maskFilter = null
        canvas.drawCircle(nx, ny, 7f, dialKnobPaint)

        // Centre dot
        dialKnobPaint.color = Color.argb(180, 80, 200, 255)
        canvas.drawCircle(dialCenterX, dialCenterY, 8f, dialKnobPaint)
        dialKnobPaint.color = Color.argb(255, 255, 240, 80)

        // Angle value text
        dialTextPaint.textSize = 38f
        canvas.drawText("${curAngle.toInt()}°", dialCenterX, dialCenterY + r + 48f, dialTextPaint)

        // Mirror ID label
        dialHintPaint.textSize = 22f
        canvas.drawText("거울 #${selectedMirror.id}  [${minA.toInt()}° ~ ${maxA.toInt()}°]",
            dialCenterX, panelTop + 28f, dialHintPaint)

        // Drag hint arrows (←  →)
        dialHintPaint.textSize = 26f
        canvas.drawText("◀  드래그하여 회전  ▶", dialCenterX, dialCenterY + r + 80f, dialHintPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TOUCH HANDLING  (two zones: game field + dial)
    // ─────────────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val ex = event.x
        val ey = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val inDialZone = ey > h - dialPanelHeight

                if (inDialZone && engine.selectedMirrorId != -1) {
                    // Start dial rotation
                    dialTouching = true
                    lastDialAngleDeg = Math.toDegrees(
                        atan2((ey - dialCenterY).toDouble(), (ex - dialCenterX).toDouble())
                    ).toFloat()
                    val mirror = engine.mirrors.find { it.id == engine.selectedMirrorId }
                    dialStartMirrorAngle = mirror?.angle ?: 0f
                    dialStartAngle = lastDialAngleDeg
                } else if (!inDialZone) {
                    // Tap on game field — select / deselect mirror
                    val normX = ex / w
                    val normY = ey / h
                    val selected = engine.onTouchDown(normX, normY)
                    if (selected) soundManager.playMirrorRotate()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (dialTouching) {
                    val fingerAngle = Math.toDegrees(
                        atan2((ey - dialCenterY).toDouble(), (ex - dialCenterX).toDouble())
                    ).toFloat()

                    // Delta from the initial touch point, applied to initial mirror angle
                    var delta = fingerAngle - dialStartAngle
                    // Wrap delta to [-180, 180] to avoid jumps when crossing ±180°
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f

                    val newAngle = dialStartMirrorAngle + delta
                    engine.onDialAngleChanged(newAngle)
                    lastDialAngleDeg = fingerAngle
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dialTouching = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        engine.pause()
        running = false
        gameThread?.join()
    }

    fun resumeGame() {
        engine.resume()
        running = true
        lastTimeMs = SystemClock.elapsedRealtime()
        gameThread = Thread(this, "GameThread").also { it.start() }
    }
}
