package com.godston.emojitiles

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.godston.emojitiles.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    val SPLASH_SCREEN = 10000
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var spinAnimation1: Animation
    private lateinit var spinAnimation2: Animation
    private lateinit var spinAnimation3: Animation
    private lateinit var spinAnimation4: Animation

    private lateinit var splashCV1: CardView
    private lateinit var splashCV2: CardView
    private lateinit var splashCV3: CardView
    private lateinit var splashCV4: CardView

    private lateinit var splashIV1: ImageView
    private lateinit var splashIV2: ImageView
    private lateinit var splashIV3: ImageView
    private lateinit var splashIV4: ImageView

    private lateinit var splashTV1: TextView
    private lateinit var splashTV2: TextView
    private lateinit var splashProgress: ProgressBar

    private lateinit var charSequense: CharSequence
    private var index: Int = 0
    private var delay: Long = 50
    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        actionBar!!.hide()

        splashCV1 = binding.splashCv1
        splashCV2 = binding.splashCv2
        splashCV3 = binding.splashCv3
        splashCV4 = binding.splashCv4

        splashIV1 = binding.splashIv1
        splashIV2 = binding.splashIv2
        splashIV3 = binding.splashIv3
        splashIV4 = binding.splashIv4

        splashTV1 = binding.splashTv1
        splashTV1.setTextColor(Color.parseColor("#FFFFFF"))
        splashTV2 = binding.splashTv2

        splashProgress = binding.splashProgressBar
        splashProgress.max = SPLASH_SCREEN
        val currentProgress = 10000
        ObjectAnimator.ofInt(splashProgress, "progress", currentProgress)
            .setDuration(SPLASH_SCREEN.toLong())
            .start()


        spinAnimation()
        Handler().postDelayed({
            animateText("Tiles matching game")
        }, 4000)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN.toLong())

        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            splashTV1,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        objectAnimator.duration = 500
        objectAnimator.repeatCount = ValueAnimator.INFINITE
        objectAnimator.repeatMode = ValueAnimator.REVERSE
        objectAnimator.start()
    }

    private fun spinAnimation() {
        Handler().postDelayed({
            spinAnimation1 = AnimationUtils.loadAnimation(this, R.anim.spin_animation1)
            spinAnimation2 = AnimationUtils.loadAnimation(this, R.anim.spin_animation2)
            spinAnimation3 = AnimationUtils.loadAnimation(this, R.anim.spin_animation3)
            spinAnimation4 = AnimationUtils.loadAnimation(this, R.anim.spin_animation4)

            splashCV1.startAnimation(spinAnimation1)
            splashCV2.startAnimation(spinAnimation2)
            splashCV3.startAnimation(spinAnimation3)
            splashCV4.startAnimation(spinAnimation4)
        }, -100)
        Handler().postDelayed({
            splashIV1.setImageResource(R.drawable.emoji_01)
        }, 3000)
        Handler().postDelayed({
            splashIV2.setImageResource(R.drawable.emoji_02)
        }, 3500)
        Handler().postDelayed({
            splashIV1.setImageResource(R.drawable.emoji_rose)
            splashIV2.setImageResource(R.drawable.emoji_rose)
        }, 4000)
        Handler().postDelayed({
            splashIV3.setImageResource(R.drawable.emoji_02)
        }, 4500)
        Handler().postDelayed({
            splashIV4.setImageResource(R.drawable.emoji_01)
        }, 5000)
        Handler().postDelayed({
            splashIV3.setImageResource(R.drawable.emoji_rose)
            splashIV4.setImageResource(R.drawable.emoji_rose)
        }, 5500)
        Handler().postDelayed({
            splashIV1.setImageResource(R.drawable.emoji_01)
        }, 6000)
        Handler().postDelayed({
            splashIV4.setImageResource(R.drawable.emoji_01)
        }, 6500)
        Handler().postDelayed({
            splashIV1.alpha = 0.6F
            splashIV4.alpha = 0.6F
        }, 7000)
        Handler().postDelayed({
            splashIV2.setImageResource(R.drawable.emoji_02)
        }, 7500)
        Handler().postDelayed({
            splashIV3.setImageResource(R.drawable.emoji_02)
        }, 8000)
        Handler().postDelayed({
            splashIV2.alpha = 0.6F
            splashIV3.alpha = 0.6F
        }, 8500)
    }

    var runnable: Runnable = object : Runnable {
        override fun run() {
            splashTV2.text = charSequense.subSequence(0, index++)
            splashTV2.setTextColor(Color.parseColor("#FFFFFF"))
            if (index <= charSequense.length) {
                handler.postDelayed(this, delay)
            }
        }
    }

    fun animateText(cs: CharSequence) {
        charSequense = cs
        index = 0
        splashTV2.text = ""
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, delay)
    }
}
