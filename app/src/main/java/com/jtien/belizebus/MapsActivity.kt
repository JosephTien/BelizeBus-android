package com.jtien.belizebus

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.widget.TextView
import android.view.WindowManager
import android.widget.LinearLayout
import com.google.android.gms.maps.model.*
import android.content.Intent


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.hide()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private val terminals_samp = arrayOf(
            Terminal("Benque Viejo", LatLng(17.078188, -89.137215), Kind.bus),
            Terminal("Belize City", LatLng(17.494178, -88.193227), Kind.bus),
            Terminal("Belmopan", LatLng(17.249722, -88.774723), Kind.bus),
            /* not bus station*/ Terminal("Burrell Boom Junction", LatLng(17.568479, -88.407648), Kind.bus),
            /* not bus station*/ Terminal("Corozal", LatLng(18.392959, -88.388322), Kind.bus),
            Terminal("Dangriga", LatLng(16.961170, -88.221012), Kind.bus),
            /* not bus station*/ Terminal("Guinea Grass", LatLng(17.959253, -88.589738), Kind.bus),
            Terminal("Independence", LatLng(16.534899, -88.423808), Kind.bus),
            Terminal("Orange Walk", LatLng(18.081563, -88.561811), Kind.bus),
            /* not bus station*/ Terminal("Placenia", LatLng(16.525696, -88.369829), Kind.bus),
            Terminal("Punta Gorda", LatLng(16.101556, -88.801661), Kind.bus),
            Terminal("San Ignacio", LatLng(17.158880, -89.069430), Kind.bus),
            Terminal("Santa Elena Border", LatLng(18.486174, -88.400268), Kind.bus),
            Terminal("Belize City", LatLng(17.494116, -88.184718), Kind.ferry, "(Water Taxi)"),//water taxi
            Terminal("Belize City", LatLng(17.495196, -88.186644), Kind.ferry, "(Ocean Ferry)"),//ocean ferry
            Terminal("Caye Caulker", LatLng(17.743445, -88.023459), Kind.ferry),
            Terminal("San Pedro", LatLng(17.918149, -87.961981), Kind.ferry),
            Terminal("Chetumal", LatLng(18.493027, -88.299298), Kind.ferry)
    )
    private var terminals = terminals_samp
    private var choosenTerminals: Array<Int> = arrayOf()
    private fun setReturnResult(){
        val intent = Intent()
        val bundleBack = Bundle()
        val n = choosenTerminals.count()
        if(n < 1){
            //bundleBack.putInt("station_depart", 0)
            bundleBack.putString("station_depart", "")
        }else{
            //bundleBack.putInt("station_depart", choosenStations[0]+1)
            bundleBack.putString("station_depart", terminals[choosenTerminals[0]].name)
        }
        if(n < 2){
            //bundleBack.putInt("station_arrive", 0)
            bundleBack.putString("station_arrive", "")
        }else{
            //bundleBack.putInt("station_arrive", choosenStations[1]+1)
            bundleBack.putString("station_arrive", terminals[choosenTerminals[1]].name)
        }
        intent.putExtras(bundleBack)
        setResult(Activity.RESULT_OK, intent)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        terminals = terminals_samp.filter { it.kind == MainActivity.mode_kind }.toTypedArray()
        terminals.forEachIndexed { index, terminal ->
            val marker = MarkerOptions()
                    .position(terminal.latlng)
                    .title(terminal.name)
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(terminal.name, terminal.note)))
            terminals[index].marker = mMap.addMarker(marker)
            terminals[index].marker?.tag = index
        }

        val boundsBuilder = LatLngBounds.builder()
        for(terminal in terminals){
            boundsBuilder.include(terminal.latlng)
        }
        val bounds = boundsBuilder.build()
        mMap.setLatLngBoundsForCameraTarget(bounds)

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (height * 0.1).toInt() // offset from edges of the map 10% of screen
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        mMap.moveCamera(cu)
        mMap.setMinZoomPreference(mMap.cameraPosition.zoom)

        mMap.setOnMarkerClickListener(this)
    }




    override fun onMarkerClick(marker: Marker): Boolean {
        var name = terminals[marker.tag as Int].name
        var note = terminals[marker.tag as Int].note
        if(choosenTerminals.count()>0 && choosenTerminals.last()==marker.tag){
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(name, note)))
            choosenTerminals = choosenTerminals.dropLast(1).toTypedArray()
            return true
        }
        if(choosenTerminals.count()==2){
            choosenTerminals = arrayOf()
            terminals.forEach {
                //it.marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                //it.marker?.hideInfoWindow()
                it.marker?.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(it.name, it.note)))
            }
        }
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        //marker.showInfoWindow()
        if(choosenTerminals.count()==0){
            name = "① $name"
        }else{
            name = "② $name"
        }
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(name, note)))
        choosenTerminals = choosenTerminals.plus(marker.tag as Int)
        setReturnResult()
        return true
    }
    private fun getMarkerIcon(name: String, note: String): Bitmap {
        val layout = LayoutInflater.from(this).inflate(R.layout.maps_marker, null)
        val view = layout.findViewById<TextView>(R.id.map_marker_name)
        var str = name
        if(note!=""){
            str+="\n$note"
        }
        view.text = str
        val marker = layout.findViewById<LinearLayout>(R.id.map_marker)
        return loadBitmapFromView(marker)!!
    }
    private fun loadBitmapFromView(v: View): Bitmap? {
        if (v.measuredHeight <= 0) {
            v.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            return b
        }
        return null
    }
    //------------------------------------------------------------------------------------------------
}

class Terminal(val name: String, val latlng: LatLng, val kind: Kind, val note: String = ""){
    var marker: Marker? = null
}
/*
class Boundry(){
    companion object{
        var left: Double = Double.MAX_VALUE
        var right: Double = Double.MIN_VALUE
        var down: Double = Double.MAX_VALUE
        var up: Double = Double.MIN_VALUE
        var center: LatLng = LatLng(0.0, 0.0)
        fun reset(){
            left = Double.MIN_VALUE
            right = Double.MAX_VALUE
            up = Double.MAX_VALUE
            down = Double.MIN_VALUE
        }
        fun push(latLng: LatLng){
            if(latLng.latitude<left){
                left = latLng.latitude
            }
            if(latLng.latitude>right){
                right = latLng.latitude
            }
            if(latLng.longitude<down){
                down = latLng.longitude
            }
            if(latLng.longitude>up){
                up = latLng.longitude
            }
            if(left != Double.MAX_VALUE
            && right != Double.MIN_VALUE
            && down != Double.MAX_VALUE
            && up != Double.MIN_VALUE){
                center = LatLng((left+ right) / 2, (down+ up) / 2)
            }
        }
    }
}
*/