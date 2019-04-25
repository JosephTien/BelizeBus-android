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
import android.R.attr.data



class MainActivity : AppCompatActivity(){

    lateinit var singleDateTimePicker: SingleDateAndTimePickerDialog.Builder
    lateinit var spinner_depart: Spinner
    lateinit var spinner_arrive: Spinner
    lateinit var btn_depart: Button
    lateinit var btn_arrive: Button
    lateinit var btn_submit: Button
    lateinit var btn_history: Button
    lateinit var btn_switch: Button
    lateinit var btn_clear: Button
    lateinit var btn_map: Button
    lateinit var departListener: SingleDateAndTimePickerDialog.Listener
    lateinit var arriveListener: SingleDateAndTimePickerDialog.Listener
    var departStation: String = ""
    var arriveStation: String = ""
    var departDayAndTime: DayAndTime? = null
    var arriveDayAndTime: DayAndTime? = null
    companion object {
        var instance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///init/assign
        MainActivity.instance = this
        spinner_depart = findViewById(R.id.spinner_depart)
        spinner_arrive = findViewById(R.id.spinner_arrive)
        btn_depart = findViewById<Button>(R.id.btn_depart)
        btn_arrive = findViewById<Button>(R.id.btn_arrive)
        btn_submit = findViewById<Button>(R.id.btn_submit)
        btn_history = findViewById<Button>(R.id.btn_history)
        btn_switch = findViewById<Button>(R.id.btn_switch)
        btn_clear = findViewById<Button>(R.id.btn_clear)
        btn_map    = findViewById<Button>(R.id.btn_map)

        ///init/assign/datetimepicker
        singleDateTimePicker = SingleDateAndTimePickerDialog.Builder(this)
                //.bottomSheet()

                .backgroundColor(genColor(R.color.colorField))
                .mainColor(genColor(R.color.colorPrimaryDark))
                .titleTextColor(genColor(R.color.colorText))
                .curved()
                .minutesStep(15)
                .bottomSheet()
                .displayAmPm(false)

        ///init/adapter
        val spinner_depart_adapter = ArrayAdapterWithHint(this, R.layout.spinner_item_current, arrayOf(getString(R.string.choose_start))+Reference.stations)
        spinner_depart.adapter = spinner_depart_adapter
        spinner_depart_adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner_depart.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val departIdx = spinner_depart.selectedItemPosition - 1
                departStation = when{
                    departIdx < 0 -> ""
                    else -> Reference.stations[departIdx]
                }
            }
        }
        val spinner_arrive_adapter = ArrayAdapterWithHint(this, R.layout.spinner_item_current, arrayOf(getString(R.string.choose_end)) + Reference.stations)
        spinner_arrive.adapter = spinner_arrive_adapter
        spinner_arrive_adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner_arrive.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val arriveIdx = spinner_arrive.selectedItemPosition - 1
                arriveStation = when{
                    arriveIdx < 0 -> ""
                    else -> Reference.stations[arriveIdx]
                }
            }
        }

        ///init/Listener
        departListener = object : SingleDateAndTimePickerDialog.DisplayListener, SingleDateAndTimePickerDialog.Listener {
            override fun onDateSelected(date: Date?) {
                departDayAndTime = DayAndTime(date!!)
                btn_depart.text = departDayAndTime.toString()
                if(arriveDayAndTime != null){
                    if(arriveDayAndTime!!.weekDayIdx != departDayAndTime!!.weekDayIdx){
                        arriveDayAndTime = DayAndTime(date!!,arriveDayAndTime!!.hour, arriveDayAndTime!!.minute)
                        btn_arrive.text = arriveDayAndTime.toString()
                        showToast(getString(R.string.msg_notSameDay))
                    }
                }
            }
            override fun onDisplayed(picker: SingleDateAndTimePicker?) {}
        }
        arriveListener = object : SingleDateAndTimePickerDialog.DisplayListener, SingleDateAndTimePickerDialog.Listener {
            override fun onDateSelected(date: Date?) {
                arriveDayAndTime = DayAndTime(date!!)
                btn_arrive.text = arriveDayAndTime.toString()
                if(departDayAndTime != null){
                    if(departDayAndTime!!.weekDayIdx != arriveDayAndTime!!.weekDayIdx) {
                        departDayAndTime = DayAndTime(date!!, departDayAndTime!!.hour, departDayAndTime!!.minute)
                        btn_depart.text = departDayAndTime.toString()
                        showToast(getString(R.string.msg_notSameDay))
                    }
                }
            }
            override fun onDisplayed(picker: SingleDateAndTimePicker?) {}
        }

        ///init/btn
        btn_depart.setOnClickListener {
            singleDateTimePicker
                .title("Depart After")
                .listener(departListener)
            if(departDayAndTime!=null && departDayAndTime!!.date != null){
                singleDateTimePicker
                    .defaultDate(departDayAndTime!!.date)
            }
            singleDateTimePicker.display()
        }
        btn_arrive.setOnClickListener {
            singleDateTimePicker
                .title("Arrive Before")
                .listener(arriveListener)
            if(arriveDayAndTime!=null && arriveDayAndTime!!.date != null){
                singleDateTimePicker
                        .defaultDate(arriveDayAndTime!!.date)
            }
            singleDateTimePicker.display()
        }
        btn_submit.setOnClickListener {
            val departIdx = spinner_depart.selectedItemPosition - 1
            val arriveIdx = spinner_arrive.selectedItemPosition - 1
            departStation = when {
                departIdx < 0 -> ""
                else -> Reference.stations[departIdx]
            }
            arriveStation = when {
                arriveIdx < 0 -> ""
                else -> Reference.stations[arriveIdx]
            }
            if (departStation == "" || arriveStation == "") {
                showToast(getString(R.string.msg_stationEmpty))
            }else if (departStation == arriveStation){
                showToast(getString(R.string.msg_stationNotSame))
            }else if(departDayAndTime!=null && arriveDayAndTime!=null &&
                    departDayAndTime!!.time >= arriveDayAndTime!!.time){
                showToast(getString(R.string.msg_notValidRange))
            }else{
                val departTime = when(departDayAndTime){null->"" else->departDayAndTime!!.time}
                val arriveTime = when(arriveDayAndTime){null->"" else->arriveDayAndTime!!.time}
                var weekDayIdx = DayAndTime.getTodayWeekDayIdx()
                if(departDayAndTime!=null){
                    weekDayIdx = departDayAndTime!!.weekDayIdx
                }else if (arriveDayAndTime!=null){
                    weekDayIdx = arriveDayAndTime!!.weekDayIdx
                }
                Data.searchRoute(departStation, arriveStation, departTime, arriveTime, weekDayIdx)
                val ResultIntent = Intent(this, ResultActivity::class.java)
                writeSearchHistory()
                startActivity(ResultIntent)
            }
        }
        btn_history.setOnClickListener {
            readSearchHistory()
        }
        btn_switch.setOnClickListener {
            if(departStation!="" && arriveStation!=""){
                val departIdx = spinner_arrive.selectedItemPosition - 1
                val arriveIdx = spinner_depart.selectedItemPosition - 1
                spinner_arrive.setSelection(arriveIdx+1)
                spinner_depart.setSelection(departIdx+1)
            }
        }
        btn_clear.setOnClickListener {
            spinner_arrive.setSelection(0)
            spinner_depart.setSelection(0)
            btn_depart.text = getString(R.string.btn_after)
            btn_arrive.text = getString(R.string.btn_before)
            departDayAndTime = null
            arriveDayAndTime = null
        }
        btn_map.setOnClickListener {
            if(ConnectivityHelper.isConnectedToNetwork(applicationContext)){
                requestMap()
            }else{
                showToast(getString(R.string.msg_noInternetConnection))
            }

        }

        ///init/actionbar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.abs_layout)
        supportActionBar?.setTitle(R.string.bar_search)

        ///init/data
        /*Do it in welcome
        Data.assets = assets
        Data.initData()
        */
    }
    val REQUEST_CODE_MAP = 0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === REQUEST_CODE_MAP && resultCode === Activity.RESULT_OK) {
            val departIdx = data!!.getIntExtra("station_depart", -1)
            val arriveIdx = data!!.getIntExtra("station_arrive", -1)
            if(departIdx>=0){
                spinner_depart.setSelection(departIdx)
            }
            if(arriveIdx>=0){
                spinner_arrive.setSelection(arriveIdx)
            }
        }
    }
    private fun requestMap(){
        var intent = Intent()
        intent.setClass(this, MapsActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_MAP)
    }
    private fun readSearchHistory(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val departIdx = sharedPref.getInt("station_depart", -1)
        val arriveIdx = sharedPref.getInt("station_arrive", -1)
        if(departIdx>=0){
            spinner_depart.setSelection(departIdx)
        }
        if(arriveIdx>=0){
            spinner_arrive.setSelection(arriveIdx)
        }
    }
    private fun writeSearchHistory(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("station_depart", spinner_depart.selectedItemPosition)
            putInt("station_arrive", spinner_arrive.selectedItemPosition)
            commit()
        }
    }

    fun showTest(){
        Log.v("MyLog", ""+Data.sheets[0].buses[0].idx)
        Log.v("MyLog", ""+Data.sheets[0].buses[0].company)
        Log.v("MyLog", ""+Data.sheets[0].buses[0].rsp)
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].days))
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].arrive))
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].depart))
    }

    fun uiChange(){
        btn_submit.visibility = View.INVISIBLE
        if (departStation == "" || arriveStation == "") {
            showToast(getString(R.string.msg_stationEmpty))
        }else if (departStation == arriveStation){
            showToast(getString(R.string.msg_stationNotSame))
        }else if(departDayAndTime!=null && arriveDayAndTime!=null &&
                departDayAndTime!!.time >= arriveDayAndTime!!.time){
            showToast(getString(R.string.msg_notValidRange))
        }else{
            btn_submit.visibility = View.VISIBLE
        }
    }

    fun isInternetAvailable(): Boolean {
        try {
            val ipAddr = InetAddress.getByName("google.com")
            return true

        } catch (e: Exception) {
            return false
        }

    }

    ///utility/toast
    fun showToast(string: String){
        Toast.makeText(applicationContext,
                string,
                Toast.LENGTH_SHORT).show()
    }
    fun genColor(rid: Int): Int{
        return ContextCompat.getColor(this, rid)
    }
}

///class/adapter
class ArrayAdapterWithHint<T>(val cont: Context, val res: Int, val objs: Array<T>): ArrayAdapter<T>(cont, res, objs) {
    override fun isEnabled(position: Int): Boolean {
        return position > 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = super.getDropDownView(position, convertView, parent) as TextView
        if (position == 0){
            //view.setBackgroundResource(R.drawable.spinner_item_disable)
            //view.setTextColor(Color.WHITE)
            view.text = "-- " + view.text + " --"
            view.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            //set once
            (parent as ListView).setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorField))
        }

        return view
    }
}

object ConnectivityHelper {
    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isConnected = false
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            isConnected = activeNetwork != null && activeNetwork.isConnected
        }

        return isConnected
    }
}
//Todo: company length
//Todo: Type length
//Todo: Current Pos
//Todo: History
//Todo: 備注
