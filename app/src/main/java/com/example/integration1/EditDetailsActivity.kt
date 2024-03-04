package com.example.integration1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout

class EditDetailsActivity : AppCompatActivity() {

    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneNumberET: EditText
    private lateinit var ageET: EditText
    private lateinit var saveBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var resetPasswordTV: TextView
    private lateinit var requestQueue: RequestQueue
    private var contextTAG: String = "EditDetails"
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_details)

        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        nameET = findViewById(R.id.name_et_id)
        emailET = findViewById(R.id.email_et_id)
        phoneNumberET = findViewById(R.id.phone_no_et_id)
        ageET = findViewById(R.id.age_et_id)
        saveBTN = findViewById(R.id.save_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        resetPasswordTV = findViewById(R.id.reset_password_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)
        val nameTIL = findViewById<TextInputLayout>(R.id.name_til_id)
        val emailTIL = findViewById<TextInputLayout>(R.id.email_til_id)
        val phoneNumberTIL = findViewById<TextInputLayout>(R.id.phone_no_til_id)
        val ageTIL = findViewById<TextInputLayout>(R.id.age_til_id)
        nameTIL.setStartIconTintList(null)
        emailTIL.setStartIconTintList(null)
        phoneNumberTIL.setStartIconTintList(null)
        ageTIL.setStartIconTintList(null)

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        nameET.setText(userDataViewModel.userName)
        emailET.setText(userDataViewModel.email)
        phoneNumberET.setText(userDataViewModel.phoneNumber)
        ageET.setText(userDataViewModel.age)

        saveBTN.setOnClickListener { savaDataFunction() }
        resetPasswordTV.setOnClickListener { ActivityUtils.navigateToActivity(this, Intent(this, ForgotPasswordActivity::class.java)) }

    }

    private fun savaDataFunction() {
        val name = nameET.text.toString().trim()
        val email = emailET.text.toString().trim()
        val phoneNumber = phoneNumberET.text.toString().trim()
        val age = ageET.text.toString().trim()



        if (name.isEmpty() || name.any { it.isDigit() }) {
            nameET.error = getString(R.string.enter_valid_name)
            return
        }
        if (phoneNumber.length != 10) {
            phoneNumberET.error = getString(R.string.enter_valid_phone_number)
            return
        }
        if (age.toIntOrNull() !in 10..100) {
            ageET.error = getString(R.string.enter_valid_age)
            return
        }

        animationView.setAnimation(R.raw.profile_loading)
        animationView.playAnimation()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
            },
            { error ->
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()

            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "editDetails",
                    "userId" to userDataViewModel.userId,
                    "roomId" to userDataViewModel.roomId,
                    "userName" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "age" to age
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }
}