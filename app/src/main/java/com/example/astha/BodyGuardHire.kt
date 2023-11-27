package com.example.astha

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class BodyGuardHire : AppCompatActivity (), PaymentResultListener {

    lateinit var pay: Button
    lateinit var card: CardView
    lateinit var success: TextView
    lateinit var failed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_guard_hire)

        pay = findViewById(R.id.pay)
        pay.setOnClickListener {
            makePayment()
        }
    }

    private fun makePayment() {
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name","Astha")
            options.put("description","Hire Body guard")

            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#3399cc");
            options.put("currency","INR");
            options.put("amount","1000000")//pass amount in currency subunits


            val prefill = JSONObject()
            prefill.put("email","")
            prefill.put("contact","")

            options.put("prefill",prefill)
            co.open(this,options)
        }catch (e: Exception){
            Toast.makeText(this,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this," Payment Successful $p0",Toast.LENGTH_LONG).show()
        pay = findViewById(R.id.pay)
        pay.visibility = View.GONE
        card = findViewById(R.id.cardView)
        card.visibility = View.GONE
        success = findViewById(R.id.success)
        success.visibility = View.VISIBLE
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this," Error $p1",Toast.LENGTH_LONG).show()
        failed = findViewById(R.id.failed)
        failed.visibility = View.VISIBLE

    }
}