package com.example.astha

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.telephony.SmsManager
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*


class Alert : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var timer: Timer? = null
    private var isSendingLocation = false

    private lateinit var  alertbutton: Button
    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var user: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private var isSMSPermissionGranted = false
    private var isLocationPermissionGranted = false

    var latitude:Double = 0.0
    var longitude:Double = 0.0

    lateinit var formatedDate:String
    lateinit var formatedTime:String

    lateinit var place : Place


    val priority = Priority.PRIORITY_HIGH_ACCURACY
    val cancellationTokenSource = CancellationTokenSource()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        drawNavUI()
        alertBtnLocationSend()
        showLocationPrompt()
        assignPermission()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.e("Status: ","On")
                } else {
                    Log.e("Status: ","Off")
                }
            }
        }
    }
    private fun drawNavUI(){
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> startActivity(Intent(this, Alert::class.java))
                R.id.contacts -> startActivity(Intent(this, Helper::class.java))
                R.id.time_interval -> startActivity(Intent(this,TimeInterval::class.java))
                R.id.track_location -> startActivity(Intent(this,MapsActivity::class.java))
                R.id.Hire_Guard -> startActivity(Intent(this,BodyGuardHire::class.java))
                R.id.logout -> logout()
            }
            true


        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun assignPermission(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            isSMSPermissionGranted = permissions[Manifest.permission.SEND_SMS] ?: isSMSPermissionGranted
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
        }
    }
    private fun showLocationPrompt() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location,
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Cast to a resolvable exception.
                            val resolvable: ResolvableApiException = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult()
                            resolvable.startResolutionForResult(
                                this, LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.


                    }
                }
            }
        }
    }

    private fun alertBtnLocationSend(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        alertbutton = findViewById<Button>(R.id.buttonalert)
        alertbutton.setOnClickListener {
            if (!isSendingLocation) {
                startLocationSending()
            } else {
                stopLocationSending()
            }
        }
    }

    private fun startLocationSending() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ||ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissionPage()
            return
        }

        isSendingLocation = true
        alertbutton.text = "Stop"
        val timeInterval : Long = getTimeToSharedPerf()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                if (ActivityCompat.checkSelfPermission(
                        this@Alert,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@Alert,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                    .addOnSuccessListener { location:Location? ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                            val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            formatedDate = simpleDate.format(Date())
                            val simpleTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                            formatedTime = simpleTime.format(Date())
                            //Location Data with Date and Time
                            place = Place(latitude,longitude, formatedTime, formatedDate)
                            Places.places.add(place)

                            sendLocationToContacts(latitude,longitude)
                        }
                        else{
                            Toast.makeText(this@Alert,"Please try opening Google Maps And Retry",Toast.LENGTH_LONG).show()

                        }
                    }
            }
        }, 0, timeInterval)
    }
    private fun sendLocationToContacts(latitude:Double, longitude:Double) {
        val message =
            "I am in danger!! Here's my location: https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}"
        val dataBase: SqliteDatabase = SqliteDatabase(this)
        val allContacts = dataBase.listContacts()
        val smsMgr: SmsManager = SmsManager.getDefault()
        for (contact in allContacts) {
            try {
                smsMgr.sendTextMessage(
                    "0" + contact.phno,
                    null,
                    message,
                    null,
                    null
                )
                Handler(mainLooper).post {
                    Toast.makeText(
                        this,
                        "Alert sent to " + contact.name,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Handler(mainLooper).post {
                    Toast.makeText(
                        this,
                        "Message sent failed " + contact.name,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getTimeToSharedPerf(): Long {
        sharedPreferences= this.getSharedPreferences("AsthaSharedPrefFile", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("time_val")){
            var time = sharedPreferences.getInt("time_val",1)
            time *= 60 * 1000
            return time.toLong()
        }
        val text = "NO Time Interval Set"
        Toast.makeText(this@Alert, text, Toast.LENGTH_SHORT).show()
        return 60000

    }
    private fun stopLocationSending() {
        // debugging
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        var jsonPlaces : String? = gsonPretty.toJson(Places.places)
        println(jsonPlaces)

        isSendingLocation = false
        alertbutton.text = "Alert" // change button text back to "Alert"
        cancellationTokenSource.token
        timer?.cancel() // stop the location sending timer
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationSending()
                }
            }
        }
    }


    private fun requestPermission(){
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        isSMSPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED


        val permissionRequest : MutableList<String> = ArrayList()

        if (!isSMSPermissionGranted){
            permissionRequest.add(Manifest.permission.SEND_SMS)
        }
        if (!isLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }
    private fun requestPermissionPage() {
        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)


    }
    private fun logout(){
        user = FirebaseAuth.getInstance()
        user.signOut()
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        finish()

    }


}