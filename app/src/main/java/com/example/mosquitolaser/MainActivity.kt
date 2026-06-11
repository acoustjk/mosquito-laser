package com.example.mosquitolaser

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mosquitolaser.audio.SoundManager

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var soundManager: SoundManager

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

        setContentView(buildUI())
        soundManager.startBgm(0)
    }

    private fun buildUI(): View {
        val root = FrameLayout(this)

        // Background canvas view
        val bgView = MenuBackgroundView(this)
        root.addView(bgView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // Content overlay
        val overlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(60, 0, 60, 0)
        }

        // Title
        val titleText = TextView(this).apply {
            text = "모기 레이저"
            textSize = 52f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setShadowLayer(20f, 0f, 0f, Color.argb(200, 0, 220, 255))
        }
        overlay.addView(titleText)

        // Subtitle
        val subtitleText = TextView(this).apply {
            text = "레이저로 모기를 잡아라!"
            textSize = 20f
            setTextColor(Color.argb(200, 150, 220, 255))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }
        overlay.addView(subtitleText)

        // Play button
        val playBtn = createMenuButton("🎮  게임 시작", Color.argb(255, 0, 180, 255), Color.argb(255, 0, 100, 200))
        playBtn.setOnClickListener {
            soundManager.stopBgm()
            startActivity(Intent(this, StageSelectActivity::class.java))
        }
        overlay.addView(playBtn)

        // Continue button (if there's progress)
        val lastStage = prefs.getInt("last_stage", -1)
        if (lastStage > 1) {
            val continueBtn = createMenuButton("▶  이어하기 (Stage $lastStage)", Color.argb(255, 0, 200, 100), Color.argb(255, 0, 120, 50))
            continueBtn.setOnClickListener {
                soundManager.stopBgm()
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("stage_number", lastStage)
                startActivity(intent)
            }
            overlay.addView(continueBtn)
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply { gravity = android.view.Gravity.CENTER }
        root.addView(overlay, params)

        return root
    }

    private fun createMenuButton(label: String, colorTop: Int, colorBot: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(40, 30, 40, 30)
            val margin = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 16) }
            layoutParams = margin

            val bg = object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) {
                    val rect = RectF(bounds.left.toFloat(), bounds.top.toFloat(),
                        bounds.right.toFloat(), bounds.bottom.toFloat())
                    val gradient = LinearGradient(0f, rect.top, 0f, rect.bottom,
                        colorTop, colorBot, Shader.TileMode.CLAMP)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
                    canvas.drawRoundRect(rect, 24f, 24f, paint)

                    // Border glow
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        color = Color.argb(150, 255, 255, 255)
                    }
                    canvas.drawRoundRect(rect, 24f, 24f, borderPaint)
                }
                override fun setAlpha(alpha: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                @Suppress("OVERRIDE_DEPRECATION")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
            background = bg

            setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> alpha = 0.7f
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> alpha = 1.0f
                }
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.startBgm(0)
    }

    override fun onPause() {
        super.onPause()
        soundManager.stopBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
