package com.jtien.belizebus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.view.View

class WelcomeActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)
        //hideSystemUI()
        supportActionBar?.hide()
        mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 3000)//3秒跳轉
        Data.assets = assets
        Data.initData()
    }
    private val GOTO_MAIN_ACTIVITY = 0
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            when (msg.what) {
                GOTO_MAIN_ACTIVITY -> {
                    val intent = Intent()
                    intent.setClass(this@WelcomeActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {

                }
            }

        }

    }
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}