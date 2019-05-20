package com.jtien.belizebus

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.*

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.animation.ValueAnimator
import android.util.TypedValue
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import java.util.*
import kotlin.concurrent.fixedRateTimer

class ResultActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    companion object {
        var instance: ResultActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)
        ResultActivity.instance = this

        ///init/recyclerview
        viewManager = LinearLayoutManager(this)
        viewAdapter = ResultListAdapter(Data.result)
        recyclerView = findViewById<RecyclerView>(R.id.result_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

        ///init/actionbar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.common_title)
        supportActionBar?.customView?.findViewById<TextView>(R.id.tvTitle)?.text = getString(R.string.bar_result)
        supportActionBar?.setTitle(R.string.bar_result)

        ///init/tag
        setTag()
    }

    fun setTag(){
        val result_tag_range_from = findViewById<TextView>(R.id.result_tag_range_from)
        val result_tag_range_mid = findViewById<TextView>(R.id.result_tag_range_mid)
        val result_tag_range_to = findViewById<TextView>(R.id.result_tag_range_to)
        val result_tag_station_from = findViewById<TextView>(R.id.result_tag_station_from)
        val result_tag_station_mid = findViewById<TextView>(R.id.result_tag_station_mid)
        val result_tag_station_to = findViewById<TextView>(R.id.result_tag_station_to)
        val bar_dot_mid = findViewById<View>(R.id.result_bar_dot_mid)

        if(MainActivity.instance?.departDayAndTime!=null){
            result_tag_range_mid.text = MainActivity.instance?.departDayAndTime?.weekDay.toString()
        }else if (MainActivity.instance?.arriveDayAndTime!=null){
            result_tag_range_mid.text = MainActivity.instance?.arriveDayAndTime?.weekDay.toString()
        }else{
            val weekDayIdx = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            var weekDayStr = WeekDay.values()[weekDayIdx].toString()
            var todayStr = getString(R.string.today)
            result_tag_range_mid.text = todayStr

            var toggle = false
            val va = ValueAnimator.ofInt(0, 0).setDuration(1000)
            va.setRepeatCount(ValueAnimator.INFINITE);
            va.addListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {
                    toggle = !toggle
                    if(toggle){
                        result_tag_range_mid.text = weekDayStr
                    }else{
                        result_tag_range_mid.text = todayStr
                    }
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {}
            })
            va.start()
        }

        result_tag_range_from.text = MainActivity.instance?.departDayAndTime?.time
        result_tag_range_to.text = MainActivity.instance?.arriveDayAndTime?.time
        result_tag_station_from.text = MainActivity.instance?.departStation
        result_tag_station_to.text = MainActivity.instance?.arriveStation
        if(Data.result.size>0 && Data.result[0].size==2){
            result_tag_station_mid.text = Data.result[0][0].toStation
            bar_dot_mid.visibility = View.VISIBLE
        }else if(Data.result.size>0 && Data.result[0].size==1){
            result_tag_station_mid.text = ""
            bar_dot_mid.visibility = View.INVISIBLE
        }

    }

    ///utility/toast
    fun showToast(string: String){
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show()
    }
}

class ResultListAdapter(private val myDataset: MutableList<Array<Bus>>) :
        RecyclerView.Adapter<ResultListSelectionViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ResultListSelectionViewHolder {
        // create a new view
        //val textView = LayoutInflater.from(parent.context)
        //        .inflate(R.layout.result_list_view_holder, parent, false) as TextView
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.result_list_view_holder, parent, false)
        // set the view's size, margins, paddings and layout parameters
        //...
        return ResultListSelectionViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ResultListSelectionViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val buses = myDataset[position]
        holder.setResult(buses)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

class ResultListSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener{
    val result_item_from = itemView?.findViewById<TextView>(R.id.result_item_time_from)
    val result_item_mid = itemView?.findViewById<TextView>(R.id.result_item_time_mid)
    val result_item_to = itemView?.findViewById<TextView>(R.id.result_item_time_to)
    val result_item_arrow1 = itemView?.findViewById<TextView>(R.id.result_item_time_arrow1)
    val result_item_arrow2 = itemView?.findViewById<TextView>(R.id.result_item_time_arrow2)
    var result_item_buses = itemView?.findViewById<LinearLayout>(R.id.result_item_buses)
    var result_item_toggle = itemView?.findViewById<Button>(R.id.result_item_toggle)
    var result_item_time = itemView?.findViewById<LinearLayout>(R.id.result_item_time)
    var visable = true
    var maxHeight: Int? = null
    var minHeight: Int? = null
    var buses: Array<Bus> = arrayOf()
    var flag = false

    private fun removeAppendedBus(){
        result_item_buses.removeView(result_item_buses.findViewById<LinearLayout>(R.id.result_item_buses_bus))
    }

    fun setResult(buses: Array<Bus>){
        val arrowString = ">>"
        if(buses.size==1){
            val bus = buses[0]
            result_item_from.text = bus.depart[bus.fromIdx]
            result_item_arrow1.text = ""
            result_item_mid.setBackgroundResource(R.color.colorField)
            result_item_mid.text = arrowString
            result_item_mid.setTextColor(ContextCompat.getColor(result_item_mid.context, R.color.colorText))
            result_item_arrow2.text = ""
            result_item_to.text = bus.arrive[bus.toIdx]
            //removeAppendedBus()
            //result_item_buses.addBusDetail(bus)
        }else if (buses.size==2){
            result_item_from.text = buses[0].depart[buses[0].fromIdx]
            result_item_arrow1.text = arrowString
            result_item_mid.setBackgroundResource(R.color.colorText)
            result_item_mid.text = buses[0].arrive[buses[0].toIdx]
            result_item_arrow2.text = arrowString
            result_item_to.text = buses[1].arrive[buses[1].toIdx]
            //removeAppendedBus()
            //removeAppendedBus()
            //result_item_buses.addBusDetail(buses[0])
            //result_item_buses.addBusDetail(buses[1])
        }
        this.buses = buses
        initHeightTarget()
        itemView.layoutParams.height = minHeight!!
        visable = false

        result_item_toggle.setOnClickListener {
            showBuses(!visable)
        }
    }

    private fun getViewHeight(handler: (Int)->Unit ){
        val viewTreeObserver = itemView.viewTreeObserver
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    itemView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    handler(itemView.height)
                }
            })
        }
    }

    private fun applyDP(value: Float): Int{
        val displaymetrics = itemView.context.resources.displayMetrics
        return TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, value, displaymetrics ).toInt()
    }

    private fun initHeightTarget(){
        val height_time = applyDP(90f)
        val height_bus = applyDP(120f)
        maxHeight = (height_time + height_bus * buses.size).toInt()
        minHeight = height_time.toInt()
    }

    fun initHeightTarget(handler: () -> Unit){
        val initState = result_item_buses.visibility
        result_item_buses.visibility = View.VISIBLE
        getViewHeight {
            maxHeight = it
            result_item_buses.visibility = View.GONE
            getViewHeight {
                minHeight = it
                result_item_buses.visibility = initState
                handler()
            }
        }
    }

    private fun showBuses(state: Boolean){
        showBuses(state, 200)
    }

    private fun showBuses(state: Boolean, mil: Long){
        if(minHeight == null || maxHeight == null){return}
        visable = state
        val initHeight = itemView.height
        var targetHeight = itemView.height
        if(visable){
            targetHeight = maxHeight!!
        }else{
            targetHeight = minHeight!!
        }
        val va = ValueAnimator.ofInt(initHeight, targetHeight).setDuration(mil)
        va.addUpdateListener(this)
        va.addListener(this)
        va.start()
        /*
        result_item_buses.animate()
                .translationY(result_item_buses.getHeight().toFloat() * when(visable) {true -> 1 false-> -1})
                .alpha(when(visable) {true -> 1f false-> 0f})
                .setDuration(200)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation);
                        result_item_buses.visibility = when(visable) {true -> View.VISIBLE false-> View.GONE}
                    }
                })
                */
    }
    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val value = animation!!.animatedValue as Int
        itemView.layoutParams.height = value
        itemView.requestLayout()
    }
    override fun onAnimationEnd(animation: Animator?) {
        if(!visable) {
            for (bus in buses) {
                result_item_buses.addBusDetail(bus)
            }
        }
    }
    override fun onAnimationStart(animation: Animator?) {
        if(visable){
            for(bus in buses){
                result_item_buses.addBusDetail(bus)
            }
        }
    }
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}

    private fun LinearLayout.addBusDetail(bus: Bus){
        // Layout inflater
        val layoutInflater = ResultActivity.instance!!.layoutInflater
        val view: View = layoutInflater.inflate(R.layout.result_item_buses_bus, this, false)
        //val view: View = layoutInflater.inflate(R.layout.result_item_buses_bus, null)
        var result_item_buses_bus_company = view.findViewById<TextView>(R.id.result_item_buses_bus_company)
        var result_item_buses_bus_rsp = view.findViewById<TextView>(R.id.result_item_buses_bus_rsp)
        var result_item_buses_bus_route_station_from = view.findViewById<TextView>(R.id.result_item_buses_bus_route_station_from)
        var result_item_buses_bus_route_station_to = view.findViewById<TextView>(R.id.result_item_buses_bus_route_station_to)
        var result_item_buses_bus_route_station_des = view.findViewById<TextView>(R.id.result_item_buses_bus_route_station_destination)
        var result_item_buses_bus_route_time_from = view.findViewById<TextView>(R.id.result_item_buses_bus_route_time_from)
        var result_item_buses_bus_route_time_to = view.findViewById<TextView>(R.id.result_item_buses_bus_route_time_to)

        result_item_buses_bus_company.text = " " + bus.company + " "
        if(bus.company==""){
            result_item_buses_bus_company.visibility = View.INVISIBLE
        }
        //" " + bus.destinationStation + " "
        //result_item_buses_bus_rsp.text = bus.rsp.substring(0..2)
        result_item_buses_bus_rsp.text = " " + bus.rsp + " "
        if(bus.rsp==""){
            result_item_buses_bus_rsp.visibility = View.INVISIBLE
        }

        result_item_buses_bus_route_time_from.text = bus.depart[bus.fromIdx]
        result_item_buses_bus_route_time_to.text = bus.arrive[bus.toIdx]
        result_item_buses_bus_route_station_from.text = bus.fromStation
        result_item_buses_bus_route_station_to.text = bus.toStation
        result_item_buses_bus_route_station_des.text = bus.destinationStation
        this.addView(view)
    }

}