package com.jtien.belizebus

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import java.net.InetAddress
import java.util.*
import android.net.ConnectivityManager
import android.net.Uri
import android.view.Menu
import android.view.MenuItem


class MainAdoActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_ado)

        ///init/actionbar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.common_title)
        supportActionBar?.customView?.findViewById<TextView>(R.id.tvTitle)?.text = getString(R.string.btn_ado)
        supportActionBar?.setTitle(R.string.bar_search)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.page_bus -> {
            MainActivity.mode_full = false
            MainActivity.mode_kind = Kind.bus
            startActivity(Intent().setClass(this, MainActivity::class.java))
            finish()
            true
        }
        R.id.page_ferry -> {
            MainActivity.mode_full = false
            MainActivity.mode_kind = Kind.ferry
            startActivity(Intent().setClass(this, MainActivity::class.java))
            finish()
            true
        }
        R.id.page_taxi -> {
            Toast.makeText(applicationContext,
                    getString(R.string.tobecontinue),
                    Toast.LENGTH_SHORT).show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

}
