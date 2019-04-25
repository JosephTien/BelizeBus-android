/* BACKUP
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import com.github.florent37.singledateandtimepicker.dialog.DoubleDateAndTimePickerDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog

    fun showDateTimePicker(){
        //TimePickerFragment().show(supportFragmentManager, "timePicker")
        //DatePickerFragment().show(supportFragmentManager, "datePicker")
        var doubleDateTimePicker = DoubleDateAndTimePickerDialog.Builder(this)
                //.bottomSheet()
                .backgroundColor(resources.getColor(R.color.colorField))
                .mainColor(resources.getColor(R.color.colorPrimaryDark))
                .titleTextColor(Color.WHITE)
                .curved()
                .minutesStep(15)
                //.title("Double")
                .tab0Text("Depart")
                .tab1Text("Arrive")
                .listener(object : DoubleDateAndTimePickerDialog.Listener {
                    override fun onDateSelected(dates: List<Date>) {

                    }
                })
        //doubleDateTimePicker.display()
        class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        //return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        MainActivity.instance?.hour = hourOfDay
        MainActivity.instance?.minute = minute
        MainActivity.instance?.setDataTime()
    }
}


class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of TimePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val m = when {month+1 < 10 -> "0${month+1}" else -> "${month+1}"}
        val d = when {dayOfMonth < 10 -> "0${dayOfMonth}" else -> "${dayOfMonth}"}
        val dtStart = "${year}-${m}-${d}"
        val format = SimpleDateFormat("yyyy-MM-dd")
        var weekDay = 0
        try {
            val date = format.parse(dtStart)
            weekDay = getDayOfWeek(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        MainActivity.instance?.weekDay = weekDay
    }

    fun getDayOfWeek(date: Date): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c.get(Calendar.DAY_OF_WEEK)
    }
}
*/