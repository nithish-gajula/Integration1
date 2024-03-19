package com.example.integration1

import ActivityUtils
import LOGGING
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Calendar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var joinRoomBTN: Button
    private lateinit var createRoomBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var requestQueue: RequestQueue

    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog
    private lateinit var customOverflowIcon: ImageView
    private val contextTAG: String = "MainActivity"

    private lateinit var toolbar: Toolbar


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (!permissions.all { it.value }) {
                // Handle permission denial here
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val userDataFileName = "userdata.json"
    private val reportedLogsFileName = "logs.txt"
    private val reportedReadmeLogsFileName = "logs.md"
    private val directoryName = "RoomBudget"
    private val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        directoryName
    )
    val userDataFile = File(directory, userDataFileName)
    val reportedLogsFile = File(directory, reportedLogsFileName)
    val reportedReadmeLogsFile = File(directory, reportedReadmeLogsFileName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joinRoomBTN = findViewById(R.id.join_room_btn_id)
        createRoomBTN = findViewById(R.id.create_room_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)


        // Check and request storage permissions if needed
        val permissionsToRequest = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!checkPermissions(permissionsToRequest)) {
            requestPermissions(permissionsToRequest)
        }

        checkOrCreateFiles()


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Display application icon in the toolbar
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setLogo(R.drawable.android_os)
        supportActionBar!!.setDisplayUseLogoEnabled(true)

        // Find the custom overflow icon ImageView
        customOverflowIcon = toolbar.findViewById(R.id.custom_overflow_icon)
        customOverflowIcon.setOnClickListener {
            // Handle custom overflow icon click event
            openCustomMenu()
        }

        val testing = findViewById<Button>(R.id.testing_btn_id)
        testing.setOnClickListener { startActivity(Intent(this, TestingActivity::class.java)) }

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)



        joinRoomBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, "joinRoomBTN clicked")
            resultTV.visibility = View.INVISIBLE
            joinRoomDialog()
        }

        createRoomBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, "createRoomBTN clicked")
            resultTV.visibility = View.INVISIBLE
            createRoomFunction()
        }



        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        if (!userDataViewModel.isDirExist || !userDataViewModel.isFileExist || userDataViewModel.navigateToLoginActivity) {
            LOGGING.INFO(contextTAG, "Checking userDataViewModel - navigating To LoginActivity ")
            ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
        } else if (!userDataViewModel.isRoomLengthLessThanOne) {
            LOGGING.INFO(contextTAG, "Checking userDataViewModel - navigating To RoomActivity ")
            ActivityUtils.navigateToActivity(this, Intent(this, RoomActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LOGGING.INFO(contextTAG, "onBackPressed clicked")
                finishAffinity()
            }
        })
    }

    private fun checkOrCreateFiles() {

        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (!userDataFile.exists()) {
            userDataFile.createNewFile()
        }
        if (!reportedLogsFile.exists()) {
            reportedLogsFile.createNewFile()
        }
        if (!reportedReadmeLogsFile.exists()) {
            reportedReadmeLogsFile.createNewFile()
        }

    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        requestPermissionLauncher.launch(permissions)
    }


    private fun openCustomMenu() {
        // Handle custom overflow menu action
        // You can integrate your existing menu item handling here
        val popupMenu = PopupMenu(this, customOverflowIcon)
        popupMenu.inflate(R.menu.toolbar_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    ActivityUtils.navigateToActivity(
                        this,
                        Intent(this, EditDetailsActivity::class.java)
                    )
                    true
                }

                R.id.menu_relaunch -> {
                    ActivityUtils.relaunch(this)
                    true
                }

                R.id.menu_contact_us -> {
                    ActivityUtils.navigateToActivity(
                        this,
                        Intent(this, ContactUsActivity::class.java)
                    )
                    true
                }

                R.id.menu_about -> {
                    ActivityUtils.showAboutDialog(this)
                    true
                }

                R.id.menu_logout -> {
                    ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }


    private fun joinRoomDialog() {

        LOGGING.INFO(contextTAG, "Entered in joinRoomDialog Function")

        val builderR = AlertDialog.Builder(this)
        val inflaterR = layoutInflater
        val viewR = inflaterR.inflate(R.layout.join_room_dialog_layout, null)
        val roomId = viewR.findViewById<EditText>(R.id.roomId_et_id)
        val cancelBTN = viewR.findViewById<Button>(R.id.cancel_btn_id)
        val joinBTN = viewR.findViewById<Button>(R.id.join_room_btn_id)
        val roomIdTIL = viewR.findViewById<TextInputLayout>(R.id.roomId_til_id)
        roomIdTIL.setStartIconTintList(null)
        builderR.setView(viewR)
        val alertDialogR = builderR.create()

        // Set click listener for the button
        joinBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, " joinBTN clicked ")
            val enteredText = roomId.text.toString()
            if (enteredText.isEmpty()) {
                LOGGING.INFO(contextTAG, " If condition ")
                roomId.error = getString(R.string.roomId_should_not_be_empty)
            } else {
                LOGGING.INFO(contextTAG, " Else Condition ")
                alertDialogR.dismiss()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.you_entered, enteredText)
                joinRoomFunction(enteredText)
            }

        }

        cancelBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, " CancelBTN clicked ")
            alertDialogR.dismiss()
        }
        alertDialogR.show()
    }

    private fun joinRoomFunction(roomID: String) {

        LOGGING.INFO(contextTAG, " Entered in joinRoomFunction")

        animationView.setAnimation(R.raw.profile_loading)
        animationView.playAnimation()
        alertDialog.show()
        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, " Got response = $response")
                extractRoomJoiningJsonData(response, roomID)
                Handler(Looper.getMainLooper()).postDelayed({
                    LOGGING.INFO(contextTAG, " Response Handler Started")
                    alertDialog.dismiss()
                    LOGGING.INFO(contextTAG, " Response Handler Ended")
                }, 2000)
            },
            { error ->
                LOGGING.INFO(contextTAG, " Got Error = $error")
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    LOGGING.INFO(contextTAG, " Error Handler Started")
                    alertDialog.dismiss()
                    LOGGING.INFO(contextTAG, " Error Handler Started")
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()

            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "joinRoom",
                    "roomId" to roomID,
                    "userId" to userDataViewModel.userId
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
        LOGGING.INFO(contextTAG, " Existing joinRoomFunction")
    }

    private fun extractRoomJoiningJsonData(jsonResponse: String, roomID: String) {
        LOGGING.INFO(contextTAG, " Entered extractRoomJoiningJsonData Function")

        try {
            LOGGING.INFO(contextTAG, " Existing try block")

            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            if (jsonArray.length() > 0) {

                val roomIdStatus: String
                val result: String

                val jsonItem = jsonArray.getJSONObject(0)
                roomIdStatus = jsonItem.getBoolean("roomID_status").toString()
                result = jsonItem.getString("result").toString()

                when {
                    result.toBoolean() -> {
                        LOGGING.INFO(contextTAG, " Room Joining result TRUE")
                        animationView.setAnimation(R.raw.protected_shield)
                        animationView.playAnimation()
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        storeRoomId(roomID)
                        Handler(Looper.getMainLooper()).postDelayed({
                            LOGGING.INFO(contextTAG, " Room Joining Handler started")
                            ActivityUtils.navigateToActivity(
                                this,
                                Intent(this, RoomActivity::class.java)
                            )
                            LOGGING.INFO(contextTAG, " Room Joining Handler Ended")
                        }, 2000)
                    }

                    !roomIdStatus.toBoolean() -> {
                        LOGGING.INFO(
                            contextTAG,
                            " roomIdStatus FALSE for ${getString(R.string.invalid_roomId)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        Toast.makeText(this, "Invalid Room ID", Toast.LENGTH_SHORT).show()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.invalid_roomId)

                    }

                    else -> {
                        LOGGING.INFO(
                            contextTAG,
                            " Room Joining result FALSE for ${getString(R.string.something_went_wrong)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                LOGGING.INFO(
                    contextTAG,
                    " Room Joining result FALSE for ${getString(R.string.no_data_found)}"
                )
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {

            e.printStackTrace()
        }
    }

    private fun createRoomFunction() {
        LOGGING.INFO(contextTAG, "Entered in createRoomFunction")

        animationView.setAnimation(R.raw.love_is_blind)
        animationView.playAnimation()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, "Got response = $response")
                extractRoomCreationJsonData(response)
                Handler(Looper.getMainLooper()).postDelayed({
                    LOGGING.INFO(contextTAG, "Response handler started")
                    alertDialog.dismiss()
                    LOGGING.INFO(contextTAG, "Response handler Ended")
                }, 2000)
            },
            { error ->
                LOGGING.INFO(contextTAG, "Got Error = $error")
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    LOGGING.INFO(contextTAG, "Error handler started")
                    alertDialog.dismiss()
                    LOGGING.INFO(contextTAG, "Error handler started")
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()

            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "createRoom",
                    "userId" to userDataViewModel.userId,
                    "roomId" to createRoomId(),
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }

    private fun createRoomId(): String {
        LOGGING.INFO(contextTAG, "Entered in createRoomId Function")
        val calendar = Calendar.getInstance()

        // Extract date components
        val year = calendar.get(Calendar.YEAR) % 100 // Take last two digits
        val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based, so add 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val random = generateRandomString()

        return "$minute$year${random[0]}$hour$month${random[1]}$day$second${random[2]}"
    }

    private fun generateRandomString(): String {
        LOGGING.INFO(contextTAG, "Entered in generateRandomString Function")
        val alphabet = getString(R.string.alphabets)
        val random = Random(System.currentTimeMillis())

        return (1..3)
            .map { alphabet[random.nextInt(0, alphabet.length)] }
            .joinToString("")
    }

    private fun extractRoomCreationJsonData(jsonResponse: String) {
        LOGGING.INFO(contextTAG, "Entered in extractRoomCreationJsonData Function")
        val userIdStatus: String
        val createRoomStatus: String
        val roomID: String
        val adminStatus: String

        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")


            if (jsonArray.length() > 0) {

                val jsonItem = jsonArray.getJSONObject(0)
                userIdStatus = jsonItem.getBoolean("userID_status").toString()
                createRoomStatus = jsonItem.getBoolean("result").toString()
                roomID = jsonItem.getString("roomID").toString()
                adminStatus = jsonItem.getBoolean("adminStatus").toString()


                when {
                    createRoomStatus.toBoolean() -> {
                        LOGGING.INFO(contextTAG, "createRoomStatus result is TRUE")
                        animationView.setAnimation(R.raw.done)
                        animationView.playAnimation()
                        storeRoomIdAndAdminStatus(roomID, adminStatus)
                        Toast.makeText(this, "Room Created", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            LOGGING.INFO(contextTAG, "Handler Started")
                            ActivityUtils.navigateToActivity(
                                this,
                                Intent(this, RoomActivity::class.java)
                            )
                            LOGGING.INFO(contextTAG, "Handler Ended")
                        }, 2000)
                    }

                    !userIdStatus.toBoolean() -> {
                        LOGGING.INFO(
                            contextTAG,
                            "createRoomStatus result is FALSE for ${getString(R.string.user_id_not_found)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.user_id_not_found)
                    }

                    else -> {
                        LOGGING.INFO(
                            contextTAG,
                            "createRoomStatus result is FALSE for ${getString(R.string.something_went_wrong)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                LOGGING.INFO(
                    contextTAG,
                    "createRoomStatus result is FALSE for ${getString(R.string.no_data_found)}"
                )
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {
            LOGGING.INFO(contextTAG, "JSONException ${e.message}")
            e.printStackTrace()
        }
    }

    private fun storeRoomIdAndAdminStatus(roomID: String, adminStatus: String) {
        LOGGING.INFO(contextTAG, "Entered storeRoomIdAndAdminStatus Function")
        try {

            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)

            userData.put("roomId", roomID)
            userData.put("adminStatus", adminStatus)

            FileWriter(ActivityUtils.userDataFile).use { fileWriter ->
                fileWriter.write(userData.toString())
                fileWriter.flush()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun storeRoomId(roomID: String) {
        LOGGING.INFO(contextTAG, "Entered storeRoomId Function")
        try {

            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)

            userData.put("roomId", roomID)

            FileWriter(ActivityUtils.userDataFile).use { fileWriter ->
                fileWriter.write(userData.toString())
                fileWriter.flush()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}