package com.example.weatherapp

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val keyAPI = "2cc6cbc08795a7a645e342454eb65497"
    private var zipCode = "10001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use coroutines to asynchronously fetch weather data
        fetchAPI()

        // Users should be able to change the city by tapping it and entering a new zip code
        binding.tvAddress.setOnClickListener{
            customDialog()
        }

        // Allow users to retry fetching data if error occurs (the city should also be reset to a valid zip code)
        binding.llRefresh.setOnClickListener {
            fetchAPI()
        }
    }

    private fun fetchAPI() {
        CoroutineScope(IO).launch {
            var response = ""
            // Use try blocks as safeguards against crashes
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?zip=$zipCode&units=metric&appid=$keyAPI").readText()
            } catch (e: Exception) {
                Log.d("Main","Error $e")
            }
            if (response.isNotEmpty()) {
                // Parse JSON data to populate the app with updated information
                updateFields(response)
            }
            else {
                Log.d("Main","Unable to get data")
            }
        }
    }
    private suspend fun updateFields(result: String) {
        withContext(Main) {
            val jsonObject = JSONObject(result)
            val city = jsonObject.getString("name")
            val country = jsonObject.getJSONObject("sys").getString("country")
            val weather =
                jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
            val temp = jsonObject.getJSONObject("main").getString("temp").toDouble().toInt()
            val tempMin = jsonObject.getJSONObject("main").getString("temp_min").toDouble().toInt()
            val tempMax = jsonObject.getJSONObject("main").getString("temp_max").toDouble().toInt()
            val sunrise = jsonObject.getJSONObject("sys").getLong("sunrise")
            val sunset = jsonObject.getJSONObject("sys").getLong("sunset")
            val wind = jsonObject.getJSONObject("wind").getString("gust")
            val pressure = jsonObject.getJSONObject("main").getString("pressure")
            val humidity = jsonObject.getJSONObject("main").getString("humidity")
            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm a").format(Date())

            binding.tvAddress.text = "$city, $country"
            binding.tvDate.text = "Updated at: $currentDate"

            binding.tvWeatherDescription.text = "$weather"
            binding.tvTemp.text = "$temp°C"
            binding.tvTempMin.text = "Low: $tempMin°C"
            binding.tvTempMax.text = "High: $tempMax°C"

            binding.tvSunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
            binding.tvSunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
            binding.tvWind.text = "$wind"
            binding.tvPressure.text = "$pressure"
            binding.tvHumidity.text = "$humidity"
        }

    }

    private fun customDialog(){
        val dialogBuilder = AlertDialog.Builder(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val newZipCode = EditText(this)
        newZipCode.hint = "10001"
        layout.addView(newZipCode)

        dialogBuilder.setPositiveButton("Submit", DialogInterface.OnClickListener {
                dialog, id -> changeZipCode(newZipCode.text.toString())
        })

        val alert = dialogBuilder.create()
        alert.setTitle("New Zip Code")
        alert.setView(layout)
        alert.show()
    }

    private fun changeZipCode(newZipCode: String) {
        zipCode = newZipCode
        fetchAPI()
    }
}