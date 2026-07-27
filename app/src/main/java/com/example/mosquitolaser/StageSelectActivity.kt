package com.example.mosquitolaser

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mosquitolaser.game.stages.StageRepository

class StageSelectActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private val worldColors = listOf(
        intArrayOf(Color.parseColor("#1A1A4A"), Color.parseColor("#0D0D3A")), // Night
        intArrayOf(Color.parseColor("#1A1A2A"), Color.parseColor("#101020")), // City
        intArrayOf(Color.parseColor("#0A1A0A"), Color.parseColor("#050D05")), // Jungle
        intArrayOf(Color.parseColor("#1A1005"), Color.parseColor("#100A00")), // Factory
        intArrayOf(Color.parseColor("#1A0800"), Color.parseColor("#100400")), // Volcano
        intArrayOf(Color.parseColor("#000018"), Color.parseColor("#00000F"))  // Space
    )

    private val worldAccents = listOf(
        Color.parseColor("#4466FF"), // Night - blue
        Color.parseColor("#9944FF"), // City - purple
        Color.parseColor("#44FF88"), // Jungle - green
        Color.parseColor("#FF8844"), // Factory - orange
        Color.parseColor("#FF4422"), // Volcano - red
        Color.parseColor("#22AAFF")  // Space - cyan
    )

    private val worldNames = listOf(
        "🌙 월드 1: 입문", "🌆 월드 2: 도시", "🌿 월드 3: 정글",
        "🏭 월드 4: 공장", "🌋 월드 5: 화산", "👾 월드 6: 우주"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        prefs = getSharedPreferences("mosquito_laser", MODE_PRIVATE)

        val maxUnlocked = prefs.getInt("max_stage_cleared", 0)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#050510"))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 60, 24, 60)
        }

        // Title
        val titleText = TextView(this).apply {
            text = "스테이지 선택"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
            setShadowLayer(15f, 0f, 0f, Color.argb(200, 0, 200, 255))
        }
        container.addView(titleText)

        // Worlds
        for (world in 1..6) {
            val worldFirstStage = (world - 1) * 10 + 1
            val isWorldUnlocked = maxUnlocked >= worldFirstStage - 1 || world == 1

            val worldCard = buildWorldCard(world, isWorldUnlocked, maxUnlocked)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            container.addView(worldCard, cardParams)
        }

        scrollView.addView(container)
        setContentView(scrollView)
    }

    private fun buildWorldCard(world: Int, isUnlocked: Boolean, maxUnlocked: Int): View {
        val accent = worldAccents[world - 1]
        val stages = StageRepository.getWorldStages(world)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = buildCardBackground(world, isUnlocked, accent)
        }

        // World title
        val worldTitle = TextView(this).apply {
            text = if (isUnlocked) worldNames[world - 1] else "🔒 ${worldNames[world - 1]}"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (isUnlocked) Color.WHITE else Color.argb(150, 200, 200, 200))
            setPadding(0, 0, 0, 16)
        }
        card.addView(worldTitle)

        if (!isUnlocked) {
            val lockedText = TextView(this).apply {
                text = "이전 스테이지를 클리어하여 해금하세요"
                textSize = 14f
                setTextColor(Color.argb(120, 200, 200, 200))
            }
            card.addView(lockedText)
            return card
        }

        // Stage grid (2 rows of 5)
        val grid = GridLayout(this).apply {
            columnCount = 5
            rowCount = 2
        }

        for ((index, stage) in stages.withIndex()) {
            val stageNum = stage.stageNumber
            val isCleared = prefs.getBoolean("cleared_$stageNum", false)
            val isStageUnlocked = stageNum == 1 || maxUnlocked >= stageNum - 1

            val stageBtn = buildStageButton(stageNum, isCleared, isStageUnlocked, accent)
            val btnParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(index % 5, 1, 1f)
                rowSpec = GridLayout.spec(index / 5, 1f)
                setMargins(6, 6, 6, 6)
            }
            grid.addView(stageBtn, btnParams)
        }

        card.addView(grid)

        // Progress bar
        val cleared = stages.count { prefs.getBoolean("cleared_${it.stageNumber}", false) }
        val progress = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 0)
        }
        val progressText = TextView(this).apply {
            text = "$cleared / ${stages.size}"
            textSize = 14f
            setTextColor(Color.argb(180, accent.red(), accent.green(), accent.blue()))
            setPadding(0, 0, 12, 0)
        }
        progress.addView(progressText)

        // Progress bar track
        val progressBar = FrameLayout(this).apply {
            val trackPaint = Paint()
            background = object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) {
                    val r = RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat())
                    trackPaint.color = Color.argb(60, 255, 255, 255)
                    canvas.drawRoundRect(r, 4f, 4f, trackPaint)
                    val pct = cleared.toFloat() / stages.size
                    val filled = RectF(0f, 0f, bounds.width() * pct, bounds.height().toFloat())
                    trackPaint.color = accent
                    canvas.drawRoundRect(filled, 4f, 4f, trackPaint)
                }
                override fun setAlpha(a: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                @Suppress("OVERRIDE_DEPRECATION")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
        }
        val progressBarParams = LinearLayout.LayoutParams(0, 12, 1f)
        progress.addView(progressBar, progressBarParams)
        card.addView(progress)

        return card
    }

    private fun buildStageButton(stageNum: Int, isCleared: Boolean, isUnlocked: Boolean, accent: Int): View {
        val stars = prefs.getInt("stars_$stageNum", 0)

        return TextView(this).apply {
            text = if (!isUnlocked) {
                "🔒"
            } else if (stars > 0) {
                "$stageNum\n${"★".repeat(stars)}"
            } else {
                stageNum.toString()
            }
            textSize = if (stars > 0) 14f else 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(when {
                !isUnlocked -> Color.argb(80, 200, 200, 200)
                isCleared -> Color.WHITE
                else -> Color.argb(200, 255, 255, 255)
            })
            setPadding(0, 14, 0, 14)

            val bgColor = when {
                !isUnlocked -> Color.argb(60, 50, 50, 70)
                isCleared -> accent
                else -> Color.argb(100, Color.red(accent), Color.green(accent), Color.blue(accent))
            }

            background = object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) {
                    val r = RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat())
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
                    canvas.drawRoundRect(r, 12f, 12f, p)
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1.5f
                        color = Color.argb(80, 255, 255, 255)
                    }
                    canvas.drawRoundRect(r, 12f, 12f, borderPaint)
                }
                override fun setAlpha(a: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                @Suppress("OVERRIDE_DEPRECATION")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }

            if (isUnlocked) {
                setOnClickListener {
                    val intent = Intent(this@StageSelectActivity, GameActivity::class.java)
                    intent.putExtra("stage_number", stageNum)
                    startActivity(intent)
                }
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> alpha = 0.7f
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> alpha = 1.0f
                    }
                    false
                }
            }
        }
    }

    private fun buildCardBackground(world: Int, isUnlocked: Boolean, accent: Int): android.graphics.drawable.Drawable {
        val colors = worldColors[world - 1]
        return object : android.graphics.drawable.Drawable() {
            override fun draw(canvas: Canvas) {
                val r = RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat())
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(0f, 0f, 0f, r.height(),
                        colors[0], colors[1], Shader.TileMode.CLAMP)
                }
                canvas.drawRoundRect(r, 20f, 20f, bg)

                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = if (isUnlocked) Color.argb(120, accent.red(), accent.green(), accent.blue())
                            else Color.argb(40, 150, 150, 150)
                }
                canvas.drawRoundRect(r, 20f, 20f, border)
            }
            override fun setAlpha(a: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            @Suppress("OVERRIDE_DEPRECATION")
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

    private fun Int.red() = (this shr 16) and 0xFF
    private fun Int.green() = (this shr 8) and 0xFF
    private fun Int.blue() = this and 0xFF
}
