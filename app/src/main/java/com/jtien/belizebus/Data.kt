package com.jtien.belizebus

import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.*

enum class Kind{
    bus, ferry
}
enum class WeekDay{
    Sun, Mon, Tue, Wed, Thu, Fri, Sat
}
class DayAndTime(val date: Date){
    var weekDayIdx: Int = 0
        private set(value){field = value}
    var hour: Int = 0
        private set(value){field = value}
    var minute: Int = 0
        private set(value){field = value}
    init{
        val c = Calendar.getInstance()
        c.time = date
        this.weekDayIdx = c.get(Calendar.DAY_OF_WEEK) - 1
        this.hour = c.get(Calendar.HOUR_OF_DAY)
        this.minute = c.get(Calendar.MINUTE)
    }
    constructor(date: Date, hour: Int, minute: Int) : this(date) {
        val c = Calendar.getInstance()
        c.time = date
        c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH), hour, minute)
        this.weekDayIdx = c.get(Calendar.DAY_OF_WEEK) - 1
        this.hour = hour
        this.minute = minute
    }
    var weekDay: WeekDay
        get(){
            return WeekDay.values()[weekDayIdx]
        }
        private set(value){
            weekDay = value
        }
    var time: String = ""
        get(){
            return when {hour < 10 -> "0$hour" else -> "$hour"} + ":" + when {minute < 10 -> "0$minute" else -> "$minute"}
        }
    fun setWeekDayBy(idx: Int){
        weekDayIdx = idx
    }
    override fun toString(): String{
        return "${weekDay.toString()} $time"
    }
    companion object {
        fun getTodayWeekDayIdx(): Int{
            return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        }
    }
}
class Reference {
    companion object {
        val stations = arrayOf("Benque Viejo", "Belize City", "Belmopan", "Burrell Boom Junction"
                , "Corozal", "Dangriga", "Guinea Grass", "Independence", "Orange Walk"
                , "Placencia", "Punta Gorda", "San Ignacio", "Santa Elena Border")
                // , "San Felipe", "Sarteneja")
        val ports = arrayOf("Belize City","Caye Caulker", "San Pedro", "Chetumal")
        /*
        val RSPtype  = arrayOf("Regular", "Non-Stop", "Non-Stop/S/E", "Workers", "Workers Exp",
                "Direct", "UB BMP Run", "Express", "Express to S/E", "Express to O/W", "Shuttle", "O/W only", "O/W (Reg)", "Regular O/W")
        val companys1 = arrayOf("Ovences Perez", "Eugene Jex", "Michael Frazer", "Omar Tillett" , "Terrence Flowers", "Andrew chacon", "Tomas Chell",
                "Amelio Tillett Jr. B", "Joshua's Bus Service", "Arselito Sanchez", "Carlos Walker", "Silva’s", "Amelio Tillet Jr", "Maria's Bus Service",
                "Samuel Alamilla (Albions)", "Amelio Tillett Sr.", "Amelio Tillett Sr. A", "BBOC", "Eugen Jex", "Ovences Perez", "Chell's Bus Service", "Victor Camal ",
                "Valdemar Perez", "Cabrera's Bus Service", "Rudy Morales", "Valencia", "Marvin Sanchez", "Adamina Ken", "Ernesto Cruz", "Leon Enriquez", "Rene Tillett", "Albion's", "Joel Armstrong")
        val companys2 = arrayOf("J & J Bus Line", "Silva's Bus Line", "Guerra’s Bus Line", "Maria Rodriguez", "Westline", "Silva's Bus Line", "BBOC", "Shaw’s Bus Line", "Leon Enriquez")
        val companys3 = arrayOf("Bryant Williams", "Dolores & Rose Sho", "James Bus Line", "Dalmon Ritchie", "Glen Young and Sons", "Griga Line", "Alberto Coleman", "Cecil Gill",
                "Edmund Pandy", "Mateo Polanco")
        */
    }
}

class Bus(val idx: Int
          , val company: String
          , val rsp: String
          , var days: Array<Int> = emptyArray ()
          , var arrive: Array<String> = emptyArray()
          , var depart: Array<String> = emptyArray()
){
    //temp used
    var fromIdx = -1
    var toIdx = -1
    var fromStation = ""
    var toStation = ""
    var destinationStation = ""

    fun setFromToIdx(fromIdx: Int, toIdx: Int): Bus{
        this.fromIdx = fromIdx
        this.toIdx = toIdx
        return this
    }
    fun setStation(from: String, to: String, destination: String): Bus{
        this.fromStation = from
        this.toStation = to
        this.destinationStation = destination
        return this
    }
    fun setStation(stations: Array<String>): Bus{
        this.fromStation = stations[fromIdx]
        this.toStation = stations[toIdx]
        this.destinationStation = stations.last()
        return this
    }

    fun isAvaliableOn(day: Int): Boolean{
        return days.isEmpty() || days.contains(day)
    }

    fun isAvaliableOn(after: String, before: String): Boolean{
        var valid = true
        if (depart[fromIdx] == "" || (after != "" && depart[fromIdx] < after)) {
            valid = false
        }
        if (arrive[toIdx] == "" || (before != "" && arrive[toIdx] > before)) {
            valid = false
        }
        return valid
    }
}

class Sheet(val name: String
            , val stations: Array<String> = emptyArray<String>()
            , var buses: MutableList<Bus> = mutableListOf<Bus>()
){
    //temp used
    var fromIdx = -1
    var toIdx = -1
    fun setFromToIdx(fromIdx: Int, toIdx: Int): Sheet{
        this.fromIdx = fromIdx
        this.toIdx = toIdx
        return this
    }

}

//注意名稱一致性
//注意時間格式
//注意類型名稱統一性
//確定空白不會有問題
//必須要有編號
//am, pm 小寫
//pm上加 但排除12
//Hopkin
//palacencia

class Data{
    companion object {
        var assets: AssetManager? = null
        var sheets: MutableList<Sheet> = mutableListOf<Sheet>()
        var result: MutableList<Array<Bus>> = mutableListOf<Array<Bus>>()
        fun showLog(str: String){
            Log.v("MyLog", str)
        }
        fun initData() {
            Data.readCSV("BVO_BZE")?.let { sheets.add(it) }
            Data.readCSV("BZE_BVO")?.let { sheets.add(it) }
            Data.readCSV("BZE_PG")?.let { sheets.add(it) }
            Data.readCSV("BZE_SE")?.let { sheets.add(it) }
            Data.readCSV("PG_BZE")?.let { sheets.add(it) }
            Data.readCSV("SE_BZE")?.let { sheets.add(it) }
            Data.readCSV("ferry_leave")?.let { sheets.add(it) }
            Data.readCSV("ferry_return")?.let { sheets.add(it) }
            Data.readCSV("ferry_leave_int")?.let { sheets.add(it) }
            Data.readCSV("ferry_return_int")?.let { sheets.add(it) }
            /*
            sheets.add(Sheet("WESTERN", arrayOf("Benque Viejo", "San Ignacio", "Belmopan", "Belize City")))
            sheets[0].buses.add(
                Bus(7, "Andrew chacon", "Regular"
                , arrayOf(1,2,3,4,5,6)
                , arrayOf("02:15", "02:45", "04:00", "05:45")
                , arrayOf("02:30", "03:00", "04:15", "")
                )
            )

            sheets.add(Sheet("NORTHERN", arrayOf("Belize City", "Palloti Junction", "Burrell Boom Junction",
                    "Guinea Grass", "Orange Walk", "San Felipe", "Sarteneja", "Corozal", "Santa Elena Border")))

            sheets[1].buses.add(
                    Bus(11, "1Omar Tillett", "Regular"
                            , arrayOf(1,2,3,4,5,6)
                            , arrayOf("05:45", "06:15", "06:45", "", "07:30", "", "", "08:45", "09:15")
                            , arrayOf("06:00", "06:15", "06:45", "", "07:45", "", "", "09:00", ""))
            )
            */
        }

        fun searchRoute(from: String, to: String, after: String, before: String, day: Int) {
            Data.result = mutableListOf<Array<Bus>>()
            var sheetHasFromTo: Sheet? = null
            for (sheet in sheets) {
                val fromIdx = sheet.stations.indexOf(from)
                val toIdx = sheet.stations.indexOf(to)
                if (fromIdx >= 0 && toIdx >= 0 && fromIdx < toIdx) {
                    sheet.fromIdx = fromIdx
                    sheet.toIdx = toIdx
                    sheetHasFromTo = sheet
                    break
                }
            }
            if (sheetHasFromTo != null) {
                sheetHasFromTo.buses.forEach {
                    it.setFromToIdx(sheetHasFromTo.fromIdx, sheetHasFromTo.toIdx)
                    var last = sheetHasFromTo.stations.count()-1
                    while(it.arrive[last]==""){last-=1}
                    it.setStation(sheetHasFromTo.stations[it.fromIdx]
                            , sheetHasFromTo.stations[it.toIdx]
                            , sheetHasFromTo.stations[last])
                }
                sheetHasFromTo.buses.filter {
                    it.isAvaliableOn(day) && it.isAvaliableOn(after, before)
                }.forEach {
                    Data.result.add(arrayOf(it))
                }
            } else {
                var resultSheetFrom: Sheet? = null
                var resultSheetTo: Sheet? = null
                var sheetsHasFrom: MutableList<Sheet> = mutableListOf()
                var sheetsHasTo: MutableList<Sheet> = mutableListOf()
                for (sheet in sheets) {
                    val fromIdx = sheet.stations.indexOf(from)
                    if (fromIdx >= 0) {
                        sheet.fromIdx = fromIdx
                        sheetsHasFrom.add(sheet)
                    }
                }
                for (sheet in sheets) {
                    val toIdx = sheet.stations.indexOf(to)
                    if (toIdx >= 0) {
                        sheet.toIdx = toIdx
                        sheetsHasTo.add(sheet)
                    }
                }
                for (sheetFrom in sheetsHasFrom) {
                    for (sheetTo in sheetsHasTo) {
                        sheetFrom.stations.forEachIndexed { idxFromMid, stationFromMid ->
                            sheetTo.stations.forEachIndexed { idxToMid, stationToMid ->
                                if (idxFromMid > sheetFrom.fromIdx && idxToMid < sheetTo.toIdx && stationFromMid == stationToMid) {
                                    resultSheetFrom = sheetFrom.setFromToIdx(sheetFrom.fromIdx, idxFromMid)
                                    resultSheetTo = sheetTo.setFromToIdx(idxToMid, sheetTo.toIdx)
                                }
                            }
                        }
                    }
                }
                if (resultSheetFrom != null && resultSheetTo != null) {
                    resultSheetFrom!!.buses.forEach {
                        it.setFromToIdx(resultSheetFrom!!.fromIdx, resultSheetFrom!!.toIdx)
                        var last = resultSheetFrom!!.stations.count()-1
                        while(it.arrive[last]==""){last-=1}
                        it.setStation(resultSheetFrom!!.stations[it.fromIdx]
                                , resultSheetFrom!!.stations[it.toIdx]
                                , resultSheetFrom!!.stations[last])
                    }
                    resultSheetTo!!.buses.forEach {
                        it.setFromToIdx(resultSheetTo!!.fromIdx, resultSheetTo!!.toIdx)
                        var last = resultSheetTo!!.stations.count()-1
                        while(it.arrive[last]==""){last-=1}
                        it.setStation(resultSheetTo!!.stations[it.fromIdx]
                                , resultSheetTo!!.stations[it.toIdx]
                                , resultSheetTo!!.stations[last])
                    }
                    val buses1 = resultSheetFrom!!.buses.filter { it.isAvaliableOn(day) && it.isAvaliableOn(after, before) }
                    val buses2 = resultSheetTo!!.buses.filter { it.isAvaliableOn(day) && it.isAvaliableOn(after, before) }
                    for (bus1 in buses1) {
                        for (bus2 in buses2) {
                            if (bus1.arrive[bus1.toIdx] <= bus2.depart[bus2.fromIdx]) {
                                Data.result.add(arrayOf(bus1, bus2))
                                break
                            }
                        }
                    }
                }
            }
            Data.result.sortBy {
                it[0].depart[it[0].fromIdx]
            }
        }
        fun array2String(arr: Array<Int>): String{
            var string = "["
            for(num in arr){
                string += num.toString() + ","
            }
            string += "]"
            return string
        }
        fun array2String(arr: Array<String>): String{
            var string = "["
            for(str in arr){
                string += "$str,"
            }
            string += "]"
            return string
        }
        private fun translateTimeStr(str: String): String{
            var newStr = str.replace("\\s".toRegex(), "")
            newStr = newStr.replace("?", "")
            if(newStr.length < 3){return ""}//"" should be invalid, "0:0" will be valid
            if(newStr[1] == ':'){//make 0 in the begin
                newStr = "0$str"
            }else if(newStr[2] != ':'){// not a time string
                return ""
            }
            val len = newStr.length
            val nums = newStr.substring(0, 5).split(":")
            var h = nums[0].toInt()
            var m = nums[1].toInt()
            var mstr = "${m}"
            if(m<10)mstr = "0$mstr"

            if(newStr == "12:00nn"){
                newStr = "12:00"
            }else if(newStr[len-2] == 'a') {//am
                newStr = newStr.substring(0, len-2)
            }
            else if(newStr[len-2] == 'p'){//pm
                if(h < 12){
                    newStr = "${h+12}:$mstr"
                }else{
                    newStr = newStr.substring(0, len-2)
                }
            }else if(len>=8 && newStr.substring(5, 8)=="mid"){
                newStr = "24:$mstr"
            }
            return newStr
        }
        private fun readCSV(filename: String): Sheet? {
            var fileReader: BufferedReader? = null
            try {
                var line: String?
                fileReader = BufferedReader(InputStreamReader(assets?.open("csv/"+filename+".csv")))
                if(assets == null){throw Exception()}
                val input = assets?.open("csv/$filename.csv")

                fileReader = BufferedReader(InputStreamReader(input))

                // Read CSV header
                line = fileReader.readLine()
                val tokens = line.split(",")
                val stationList: MutableList<String> = mutableListOf()
                for (i in 4 until tokens.size step 2) {
                    stationList.add(tokens[i])
                }
                val sheet = Sheet(filename, stationList.toTypedArray())
                // Read the file line by line starting from the second line
                line = fileReader.readLine()
                while (line != null) {
                    try {
                        val tokens = line.split(",")
                        val arrive: MutableList<String> = mutableListOf()
                        val depart: MutableList<String> = mutableListOf()
                        for (i in 4 until tokens.size step 2) {
                            arrive.add(translateTimeStr(tokens[i]))
                            if (i + 1 < tokens.size) depart.add(translateTimeStr(tokens[i + 1]))
                            else depart.add("")
                        }
                        var daysStr = tokens[3]
                        if(daysStr==""){
                            daysStr = "0123456"
                        }
                        sheet.buses.add(Bus(tokens[0].toInt(), tokens[1], tokens[2],
                                daysStr.toCharArray().map{it.toInt()-('0'.toInt())}.toTypedArray(),
                                arrive.toTypedArray(),
                                depart.toTypedArray()
                        )
                        )
                    }catch (e: Exception) {
                        println("Input Character Error!")
                        e.printStackTrace()
                    }
                    line = fileReader.readLine()
                }
                return sheet
            } catch (e: Exception) {
                println("Reading CSV Error!")
                e.printStackTrace()
                return null
            } finally {
                try {
                    fileReader!!.close()
                } catch (e: IOException) {
                    println("Closing fileReader Error!")
                    e.printStackTrace()
                }

            }
        }
    }
}