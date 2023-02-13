@file:Suppress("DEPRECATION")

package com.dinesh.weatherwise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val API: String = "233e338012e4b7d58a75a60108eaf568"
    var LAT : String = ""
    var LON : String = ""

    lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        val buttonRefresh = findViewById<ImageView>(R.id.refresh)

        buttonRefresh.setOnClickListener {
            recreate()
        }





        //Open Instagram
        val aboutme = findViewById<LinearLayout>(R.id.aboutme)
        aboutme.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.instagram.com/dinesh_ela_2405")
            startActivity(openURL)
        }

        //Advertisement
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){task->
                    val location:Location?=task.result
                    if (location==null){
                        Toast.makeText(this,"Can't Retrieve Location",Toast.LENGTH_SHORT).show()
                    }else{
                        LAT = location.latitude.toString()
                        LON = location.longitude.toString()

                        WeatherTask().execute()

                    }
                }
            } else {
                Toast.makeText(this,"Turn On location",Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }
    private fun checkPermissions():Boolean{
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
        ==PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext,"App Won't work without Location Permission",Toast.LENGTH_SHORT).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
    }

        @SuppressLint("StaticFieldLeak")
        inner class WeatherTask : AsyncTask<String, Void, String>() {

            @Deprecated("Deprecated in Java")
            override fun onPreExecute() {
                super.onPreExecute()
                findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.mainContainer).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.GONE
            }

            @Deprecated("Deprecated in Java")
            override fun doInBackground(vararg p0: String?): String? {
                val response: String? = try {
                    URL("https://api.openweathermap.org/data/2.5/weather?lat=$LAT&lon=$LON&appid=$API&units=metric")
                        .readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                return response
            }

            @Deprecated("Deprecated in Java")
            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                try {
                    val jsonObj = JSONObject(result)
                    val main = jsonObj.getJSONObject("main")
                    val sys = jsonObj.getJSONObject("sys")
                    val wind = jsonObj.getJSONObject("wind")
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                    val updatedAt: Long = jsonObj.getLong("dt")
                    val updatedAtText =
                        "Updated at " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
                            .format(Date(updatedAt * 1000))
                    val temp = "%.1f°C".format(main.getString("temp").toFloat())
                    val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                    val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                    val pressure = main.getString("pressure") + "hPa"
                    val humidity = main.getString("humidity") + "%"
                    val sunrise: Long = sys.getLong("sunrise")
                    val sunset: Long = sys.getLong("sunset")
                    val windSpeed = wind.getString("speed") + "m/s"
                    val weatherDescription = weather.getString("description")
                    val status = weather.getString("main")
                    val address = jsonObj.getString("name") + ", " + sys.getString("country")


                    findViewById<TextView>(R.id.address).text = address
                    findViewById<TextView>(R.id.updated_at).text = updatedAtText
                    findViewById<TextView>(R.id.temp).text = temp
                    findViewById<TextView>(R.id.status).text = weatherDescription.capitalize(Locale.ROOT)
                    findViewById<TextView>(R.id.sunrise).text =
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(sunrise * 1000)
                    findViewById<TextView>(R.id.sunset).text =
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(sunset * 1000)
                    findViewById<TextView>(R.id.wind).text = windSpeed
                    findViewById<TextView>(R.id.pressure).text = pressure
                    findViewById<TextView>(R.id.humidity).text = humidity
                    findViewById<TextView>(R.id.minTemp).text = tempMin
                    findViewById<TextView>(R.id.maxTemp).text = tempMax

                    val image = findViewById<ImageView>(R.id.status_image)

                    when (status) {
                        "Clouds" -> {
                            when (weatherDescription) {
                                "few clouds" -> image.setImageResource(R.drawable.fewclouds)
                                "scattered clouds" -> image.setImageResource(R.drawable.scatteredclouds)
                                else -> image.setImageResource(R.drawable.brokenclouds)
                            }
                        }
                        "Clear" -> {
                            image.setImageResource(R.drawable.clearsky)
                        }
                        "Mist",
                        "Smoke",
                        "Haze",
                        "Dust",
                        "Fog",
                        "Sand",
                        "Ash",
                        "Squall",
                        "Tornado" -> {
                            image.setImageResource(R.drawable.mist)
                        }
                        "Snow" -> {
                            image.setImageResource(R.drawable.snow);
                        }
                        "Drizzle" -> {
                            image.setImageResource(R.drawable.showerrain);
                        }
                        "Thunderstorm" -> {
                            image.setImageResource(R.drawable.thunderstrom);
                        }
                        "Rain" -> {
                            when(weatherDescription){
                                "light rain",
                                "moderate rain",
                                "heavy intensity rain",
                                "very heavy rain",
                                "extreme rain"-> image.setImageResource(R.drawable.rain)

                                "light intensity shower rain",
                                "shower rain",
                                "heavy intensity shower rain",
                                "ragged shower rain"->image.setImageResource(R.drawable.showerrain)

                                else ->image.setImageResource(R.drawable.snow)
                            }
                        }
                    }

//                binding.loader.visibility = View.GONE
//                binding.mainContainer.visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.mainContainer).visibility = View.VISIBLE
                } catch (e: Exception) {
                    findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                    findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
                }
            }
        }
    }