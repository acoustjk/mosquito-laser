package com.example.mosquitolaser

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mosquitolaser.audio.SoundManager
import com.example.mosquitolaser.game.GameEngine
import com.example.mosquitolaser.game.GameView
import com.example.mosquitolaser.game.stages.StageRepository

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var soundManager: SoundManager
    private lateinit var prefs: SharedPreferences
    private var stageNumber: Int = 1
    private var overlayShown = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        prefs = getSharedPreferences("mosquito_laser", MODE_PRIVATE)
        soundManager = SoundManager(this)
        stageNumber = intent.getIntExtra("stage_number", 1).coerceIn(1, 60)

        val stage = StageRepository.getStage(stageNumber)
        soundManager.startBgm(stage.worldNumber)

        gameView = GameView(this, stage, soundManager)
        gameView.onStageClear = { handler.post { showClearOverlay() } }
        gameView.onStageFail = { reason -> handler.post { showFailOverlay(reason) } }

        // Root layout
        val root = FrameLayout(this)
        root.addView(gameView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // Pause button
        val pauseBtn = buildPauseButton()
        val pauseParams = FrameLayout.LayoutParams(100, 100).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 20, 20, 0)
        }
        root.addView(pauseBtn, pauseParams)

        setContentView(root)
    }

    private fun buildPauseButton(): View {
        return TextView(this).apply {
            text = "⏸"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(Color.argb(180, 255, 255, 255))
            background = object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) {
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(80, 0, 0, 30) }
                    canvas.drawCircle(bounds.width() / 2f, bounds.height() / 2f, bounds.width() / 2f, p)
                }
                override fun setAlpha(a: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                @Suppress("OVERRIDE_DEPRECATION")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
            setOnClickListener { showPauseOverlay() }
        }
    }

    private fun showClearOverlay() {
        if (overlayShown) return
        overlayShown = true

        val engine = gameView.engine
        val stars = engine.calculateStars()
        val totalScore = engine.totalScore

        // Save progress, high score & stars
        prefs.edit().apply {
            putBoolean("cleared_$stageNumber", true)

            val maxCleared = prefs.getInt("max_stage_cleared", 0)
            if (stageNumber > maxCleared) putInt("max_stage_cleared", stageNumber)

            val prevStars = prefs.getInt("stars_$stageNumber", 0)
            if (stars > prevStars) putInt("stars_$stageNumber", stars)

            val prevScore = prefs.getInt("score_$stageNumber", 0)
            if (totalScore > prevScore) putInt("score_$stageNumber", totalScore)

            putInt("last_stage", minOf(stageNumber + 1, 60))
            apply()
        }

        val overlay = buildResultOverlay(true)
        (window.decorView as FrameLayout).addView(overlay)
    }

    private fun showFailOverlay(reason: GameEngine.FailReason) {
        if (overlayShown) return
        overlayShown = true
        val overlay = buildResultOverlay(false, reason)
        (window.decorView as FrameLayout).addView(overlay)
    }

    private fun showPauseOverlay() {
        gameView.engine.pause()
        val overlay = buildPauseOverlay()
        val container = window.decorView as FrameLayout
        container.addView(overlay)
    }

    private fun buildResultOverlay(success: Boolean, reason: GameEngine.FailReason? = null): View {
        val root = FrameLayout(this)

        // Semi-transparent backdrop
        val backdrop = View(this).apply {
            setBackgroundColor(Color.argb(160, 0, 0, 0))
            setOnClickListener { } // consume touches
        }
        root.addView(backdrop, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = buildResultCardBg(success)
        }

        // Title
        val titleText = TextView(this).apply {
            text = if (success) "🎉 스테이지 클리어!" else "💥 실패!"
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (success) Color.argb(255, 100, 255, 150) else Color.argb(255, 255, 100, 100))
            gravity = Gravity.CENTER
            setShadowLayer(20f, 0f, 0f, if (success) Color.argb(200, 0, 255, 100) else Color.argb(200, 255, 0, 0))
        }
        card.addView(titleText)

        val engine = gameView.engine

        if (success) {
            val starsEarned = engine.calculateStars()
            val starBanner = TextView(this).apply {
                text = when (starsEarned) {
                    3 -> "⭐ ⭐ ⭐"
                    2 -> "⭐ ⭐ ⎯"
                    else -> "⭐ ⎯ ⎯"
                }
                textSize = 36f
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 8)
                setShadowLayer(16f, 0f, 0f, Color.YELLOW)
            }
            card.addView(starBanner)

            // Score breakdown text
            val scoreText = TextView(this).apply {
                val base = engine.baseStageScore
                val speed = engine.speedBonus
                val eff = engine.efficiencyBonus
                val total = engine.totalScore

                text = buildString {
                    append("기본 점수: +$base\n")
                    append("속도 보너스: +$speed\n")
                    append("전략/효율 보너스: +$eff\n")
                    append("\n🏆 최종 점수: ${String.format("%,d", total)}")
                }
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.argb(240, 255, 240, 150))
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 16)
            }
            card.addView(scoreText)
        }

        // Reason (if failed)
        if (reason != null) {
            val reasonText = TextView(this).apply {
                text = when (reason) {
                    GameEngine.FailReason.TIME_UP -> "⏱ 시간 초과!"
                    GameEngine.FailReason.FORBIDDEN_ZONE -> "🚫 금지 구역 통과!"
                }
                textSize = 18f
                setTextColor(Color.argb(200, 255, 200, 100))
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 12)
            }
            card.addView(reasonText)
        }

        // Stats summary
        val statsText = TextView(this).apply {
            text = buildString {
                append("🦟 ${engine.mosquitoesKilled} / ${engine.totalMosquitoes} 격추 | 🪞 ${engine.mirrorsMoved}회 이동")
            }
            textSize = 14f
            setTextColor(Color.argb(180, 200, 200, 200))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 24)
        }
        card.addView(statsText)

        // Buttons
        if (success && stageNumber < 60) {
            val nextBtn = buildOverlayButton("▶ 다음 스테이지", Color.argb(255, 0, 180, 80))
            nextBtn.setOnClickListener {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("stage_number", stageNumber + 1)
                startActivity(intent)
                finish()
            }
            card.addView(nextBtn)
        }

        val retryBtn = buildOverlayButton("🔄 다시 시도", Color.argb(255, 50, 100, 200))
        retryBtn.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("stage_number", stageNumber)
            startActivity(intent)
            finish()
        }
        card.addView(retryBtn)

        val menuBtn = buildOverlayButton("🏠 스테이지 선택", Color.argb(200, 80, 80, 100))
        menuBtn.setOnClickListener {
            startActivity(Intent(this, StageSelectActivity::class.java))
            finish()
        }
        card.addView(menuBtn)

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(40, 0, 40, 0)
        }
        root.addView(card, cardParams)

        // Animate in
        card.alpha = 0f
        card.scaleX = 0.8f
        card.scaleY = 0.8f
        card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()

        return root
    }

    private fun buildPauseOverlay(): View {
        val root = FrameLayout(this)
        val backdrop = View(this).apply {
            setBackgroundColor(Color.argb(160, 0, 0, 20))
        }
        root.addView(backdrop, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = buildResultCardBg(null)
        }

        val title = TextView(this).apply {
            text = "⏸ 일시정지"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        card.addView(title)

        val resumeBtn = buildOverlayButton("▶ 계속하기", Color.argb(255, 0, 180, 80))
        resumeBtn.setOnClickListener {
            (root.parent as ViewGroup).removeView(root)
            gameView.engine.resume()
        }
        card.addView(resumeBtn)

        val retryBtn = buildOverlayButton("🔄 처음부터", Color.argb(255, 50, 100, 200))
        retryBtn.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("stage_number", stageNumber)
            startActivity(intent)
            finish()
        }
        card.addView(retryBtn)

        val menuBtn = buildOverlayButton("🏠 스테이지 선택", Color.argb(200, 80, 80, 100))
        menuBtn.setOnClickListener {
            startActivity(Intent(this, StageSelectActivity::class.java))
            finish()
        }
        card.addView(menuBtn)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(40, 0, 40, 0)
        }
        root.addView(card, params)
        return root
    }

    private fun buildOverlayButton(label: String, bgColor: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(32, 20, 32, 20)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 10) }
            layoutParams = params
            background = object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) {
                    val r = RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat())
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
                    canvas.drawRoundRect(r, 16f, 16f, p)
                }
                override fun setAlpha(a: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                @Suppress("OVERRIDE_DEPRECATION")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> alpha = 0.7f
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> alpha = 1f
                }
                false
            }
        }
    }

    private fun buildResultCardBg(success: Boolean?): android.graphics.drawable.Drawable {
        val colorTop = when (success) {
            true -> Color.parseColor("#0A2A1A")
            false -> Color.parseColor("#2A0A0A")
            null -> Color.parseColor("#0A0A2A")
        }
        val colorBot = when (success) {
            true -> Color.parseColor("#050F0A")
            false -> Color.parseColor("#0F0505")
            null -> Color.parseColor("#05050F")
        }
        val borderColor = when (success) {
            true -> Color.argb(120, 0, 200, 100)
            false -> Color.argb(120, 200, 50, 50)
            null -> Color.argb(80, 100, 100, 200)
        }
        return object : android.graphics.drawable.Drawable() {
            override fun draw(canvas: Canvas) {
                val r = RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat())
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(0f, 0f, 0f, r.height(),
                        colorTop, colorBot, Shader.TileMode.CLAMP)
                }
                canvas.drawRoundRect(r, 24f, 24f, bg)
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = borderColor
                }
                canvas.drawRoundRect(r, 24f, 24f, border)
            }
            override fun setAlpha(a: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            @Suppress("OVERRIDE_DEPRECATION")
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
        soundManager.stopBgm()
    }

    override fun onResume() {
        super.onResume()
        if (!overlayShown) {
            gameView.resumeGame()
            soundManager.startBgm(StageRepository.getStage(stageNumber).worldNumber)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }

    override fun onBackPressed() {
        if (!overlayShown) showPauseOverlay()
        else super.onBackPressed()
    }
}
