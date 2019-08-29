package com.jtien.belizebus

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
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
import android.content.DialogInterface


class MainActivity : AppCompatActivity(){

    lateinit var spinner_depart: Spinner
    lateinit var spinner_arrive: Spinner
    lateinit var btn_submit: Button
    lateinit var btn_history: Button
    lateinit var btn_switch: Button
    lateinit var btn_clear: Button
    lateinit var singleDateTimePicker: SingleDateAndTimePickerDialog.Builder
    lateinit var departListener: SingleDateAndTimePickerDialog.Listener
    lateinit var arriveListener: SingleDateAndTimePickerDialog.Listener
    lateinit var btn_map: Button
    lateinit var btn_full: Button
    lateinit var btn_depart: Button
    lateinit var btn_arrive: Button
    lateinit var btn_info: Button

    var departStation: String = ""
    var arriveStation: String = ""
    var departDayAndTime: DayAndTime? = null
    var arriveDayAndTime: DayAndTime? = null
    var terminalArr = emptyArray<String>()

    companion object {
        var instance: MainActivity? = null
        var mode_full = false
        var mode_kind = Kind.bus
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.instance = this
        if(mode_full){
            setContentView(R.layout.main)
            setBasic()
            setExtra()
            setFull()
        }else{
            setContentView(R.layout.main_simple)
            setBasic()
            setExtra()
            setSimp()
        }

        ///init/actionbar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.common_title)
        if(mode_kind==Kind.bus){
            supportActionBar?.customView?.findViewById<TextView>(R.id.tvTitle)?.text = getString(R.string.bar_search)+" "+getString(R.string.bus)
        }else{
            supportActionBar?.customView?.findViewById<TextView>(R.id.tvTitle)?.text = getString(R.string.bar_search)+" "+getString(R.string.ferry)
        }
        supportActionBar?.setTitle(R.string.bar_search)

        ///init/data
        /*Do it in welcome
        Data.assets = assets
        Data.initData()
        */
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.page_bus -> {
            if(mode_kind!=Kind.bus){
                if(mode_full){
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                mode_full = false
                mode_kind = Kind.bus
                startActivity(intent)
                finish()
            }
            true
        }
        R.id.page_ferry -> {
            if(mode_kind!=Kind.ferry){
                if(mode_full){
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                mode_full = false
                mode_kind = Kind.ferry
                startActivity(intent)
                finish()
            }
            true
        }
        R.id.page_ado -> {
            var intent = Intent()
            intent.setClass(this, MainAdoActivity::class.java)
            startActivity(intent)
            finish()
            true
        }
        R.id.page_taxi -> {
            //showToast(getString(R.string.tobecontinue))
            if(ConnectivityHelper.isConnectedToNetwork(applicationContext)) {
                val url = "http://belizetaxi.jtien.info"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    val REQUEST_CODE_MAP = 0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === REQUEST_CODE_MAP && resultCode === Activity.RESULT_OK) {
            val departStr = data!!.getStringExtra("station_depart")
            val arriveStr = data!!.getStringExtra("station_arrive")
            val departIdx = terminalArr.indexOf(departStr)+1
            val arriveIdx = terminalArr.indexOf(arriveStr)+1
            if(departIdx>=0){
                spinner_depart.setSelection(departIdx)
            }
            if(arriveIdx>=0){
                spinner_arrive.setSelection(arriveIdx)
            }
        }
    }
    override fun onBackPressed() {
        if(mode_full){
            mode_full = false
        }
        super.onBackPressed()
    }

    ///------------------------------------------------------------------------------------
    ///init/setUi
    private fun setBasic(){
        ///init/assign
        spinner_depart = findViewById(R.id.spinner_depart)
        spinner_arrive = findViewById(R.id.spinner_arrive)
        btn_submit = findViewById<Button>(R.id.btn_submit)
        btn_history = findViewById<Button>(R.id.btn_history)
        btn_switch = findViewById<Button>(R.id.btn_switch)
        btn_clear = findViewById<Button>(R.id.btn_clear)

        ///init/adapter
        if(mode_kind == Kind.bus){
            terminalArr = Reference.stations
        }
        else if(mode_kind == Kind.ferry){
            terminalArr = Reference.ports
        }
        val spinner_depart_adapter = ArrayAdapterWithHint(this, R.layout.main_spinner_item_current, arrayOf(getString(R.string.choose_start))+terminalArr)
        spinner_depart.adapter = spinner_depart_adapter
        spinner_depart_adapter.setDropDownViewResource(R.layout.main_spinner_item)
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
        val spinner_arrive_adapter = ArrayAdapterWithHint(this, R.layout.main_spinner_item_current, arrayOf(getString(R.string.choose_end)) + terminalArr)
        spinner_arrive.adapter = spinner_arrive_adapter
        spinner_arrive_adapter.setDropDownViewResource(R.layout.main_spinner_item)
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

        ///init/btn
        btn_submit.setOnClickListener {

            val departIdx = spinner_depart.selectedItemPosition - 1
            val arriveIdx = spinner_arrive.selectedItemPosition - 1
            departStation = when {
                departIdx < 0 -> ""
                mode_kind == Kind.bus -> Reference.stations[departIdx]
                mode_kind == Kind.ferry -> Reference.ports[departIdx]
                else -> Reference.stations[departIdx]

            }
            arriveStation = when {
                arriveIdx < 0 -> ""
                mode_kind == Kind.bus -> Reference.stations[arriveIdx]
                mode_kind == Kind.ferry -> Reference.ports[arriveIdx]
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
            if(mode_full){
                btn_depart.text = getString(R.string.btn_after)
                btn_arrive.text = getString(R.string.btn_before)
            }
            spinner_arrive.setSelection(0)
            spinner_depart.setSelection(0)
            departDayAndTime = null
            arriveDayAndTime = null
        }
    }

    private fun setSimp(){
        ///init/assign
        btn_full = findViewById<Button>(R.id.btn_full)

        ///init/btn
        btn_full.setOnClickListener {
            mode_full = true
            //recreate()
            var intent = Intent()
            intent.setClass(this, MainActivity::class.java)
            startActivity(intent)
            /*
            if(ConnectivityHelper.isConnectedToNetwork(applicationContext)) {
                val url = "http://www.belizewatertaxi.com/destinations"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            */
        }

        ///init/hide
        btn_clear.visibility = View.INVISIBLE
        btn_history.visibility = View.INVISIBLE
        btn_switch.visibility = View.INVISIBLE
        btn_switch.setOnClickListener {  }

        //setBackground
        /*
        if(mode_kind==Kind.ferry){
            val main_layout = findViewById<LinearLayout>(R.id.main_layout)
            val color = genColor(R.color.colorBackground_water)
            main_layout.setBackgroundColor(color)
        }
        */
    }
    private fun setFull(){
        ///init/assign
        btn_depart = findViewById<Button>(R.id.btn_depart)
        btn_arrive = findViewById<Button>(R.id.btn_arrive)
        btn_map = findViewById<Button>(R.id.btn_map)
        btn_info = findViewById<Button>(R.id.btn_info)

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
        if(mode_kind==Kind.bus){
            //btn_info.visibility = View.INVISIBLE
            btn_info.setOnClickListener{
                val url = "https://www.belmopanonline.com/belize-bus-schedules"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }else{
            btn_info.setOnClickListener{
                val companies = arrayOf("Ocean Ferry", "San Pedro Water Taxi")
                var builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.companies))
                builder.setItems(companies, DialogInterface.OnClickListener { dialog, which ->
                    if(which==0){
                        val url = "http://www.oceanferrybelize.com"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }else if(which==1){
                        val url = "http://www.belizewatertaxi.com"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                })
                builder.show()
            }
        }


        btn_map.setOnClickListener {
            if(ConnectivityHelper.isConnectedToNetwork(applicationContext)){
                requestMap()
            }else{
                showToast(getString(R.string.msg_noInternetConnection))
            }
        }
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
    }

    private fun setExtra(){
        /*
        var btn_record = findViewById<Button>(R.id.btn_record)
        btn_record.setOnClickListener {
            var intent = Intent()
            intent.setClass(this, RecordActivity::class.java)
            startActivity(intent)
        }
        */
    }
    ///------------------------------------------------------------------------------------
    ///usage/
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

    ///utility/genColor
    fun genColor(rid: Int): Int{
        return ContextCompat.getColor(this, rid)
    }

    ///utility/toast
    fun showToast(string: String){
        Toast.makeText(applicationContext,
                string,
                Toast.LENGTH_SHORT).show()
    }

    ///utility/testing
    fun showTest(){
        Log.v("MyLog", ""+Data.sheets[0].buses[0].idx)
        Log.v("MyLog", ""+Data.sheets[0].buses[0].company)
        Log.v("MyLog", ""+Data.sheets[0].buses[0].rsp)
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].days))
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].arrive))
        Log.v("MyLog", ""+Data.array2String(Data.sheets[0].buses[0].depart))
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
            //view.setBackgroundResource(R.drawable.main_spinner_item_disable)
            //view.setTextColor(Color.WHITE)
            var text = "-- " + view.text + " --"
            view.text = text.replace("...", "")
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
//Todo: History
//Todo: 備注
