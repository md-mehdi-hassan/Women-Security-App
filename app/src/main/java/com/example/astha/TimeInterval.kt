package com.example.astha

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.astha.databinding.ActivityTimeIntervalBinding

class TimeInterval : AppCompatActivity() {
    private lateinit var binding: ActivityTimeIntervalBinding
    private lateinit var timerConfirmButton : Button
    private var time : Int = 1
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeIntervalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val numberPicker = binding.numberPicker
        numberPicker.minValue = 1
        numberPicker.maxValue = 60
        timerConfirmButton = findViewById<Button>(R.id.NumberConfirmButton)
        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->

            time = newVal

        }
        timerConfirmButton.setOnClickListener{
            val text = "Changed from to $time"
            Toast.makeText(this@TimeInterval, text, Toast.LENGTH_SHORT).show()
            saveTimeToSharedPerf(time)

        }


    }
    fun saveTimeToSharedPerf(time: Int){
        sharedPreferences= this.getSharedPreferences("RokkhaSharedPrefFile", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("time_val",time)
        editor.apply()
        editor.commit()
    }
}