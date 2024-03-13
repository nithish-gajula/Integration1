package com.example.integration1

import ActivityUtils
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class UserDataViewModel : ViewModel() {


    var isDirExist = true // false - Not exist, true - exist(default)
    var isFileExist = true // false - Not exist, true - exist(default)
    var isRoomLengthLessThanOne = false // false(default)
    var navigateToLoginActivity = false // false(default)

    lateinit var userId: String
    lateinit var userName: String
    lateinit var age: String
    lateinit var email: String
    lateinit var phoneNumber: String
    private lateinit var loginTime: String
    lateinit var roomId: String
    private lateinit var adminStatus: String
    lateinit var profileId: String

    private val contextTAG: String = "UserDataViewModel"


//    private val _storagePermissionGranted = MutableLiveData<Boolean>()
//    val storagePermissionGranted: LiveData<Boolean>
//        get() = _storagePermissionGranted

    // Define storagePermissionGranted as a MutableLiveData
    private val storagePermissionGranted = MutableLiveData<Boolean>()

    // Function to update the value
    private fun updateStoragePermissionGranted(newValue: Boolean) {
        storagePermissionGranted.value = newValue
    }


    init {

        LOGGING.INFO(contextTAG, "Init Started")
        loadUserData()
        LOGGING.INFO(contextTAG, "Init Completed")

    }

    fun getStoragePermissions(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            LOGGING.INFO(contextTAG, "storagePermissionGranted = $storagePermissionGranted")
            //_storagePermissionGranted.value = false
            updateStoragePermissionGranted(false)
            // You may also use requestPermissions from Fragment if your UI is Fragment-based
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        } else {
            //_storagePermissionGranted.value = true
            updateStoragePermissionGranted(true)
        }
        LOGGING.INFO(contextTAG, "storagePermissionGranted = $storagePermissionGranted")
    }

    private fun loadUserData() {
        LOGGING.INFO(contextTAG, "Entered in loadUserData Function")

        if (!ActivityUtils.directory.exists()) {
            isDirExist = false
        }
        if (!ActivityUtils.userDataFile.exists()) {
            isFileExist = false
        }

        try {
            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)



            userId = userData.getString("id")
            userName = userData.getString("userName")
            age = userData.getString("age")
            email = userData.getString("email")
            phoneNumber = userData.getString("phoneNumber")
            loginTime = userData.getString("loginTime")
            roomId = userData.getString("roomId")
            adminStatus = userData.getString("adminStatus")
            profileId = userData.getString("profileId")

            LOGGING.INFO(contextTAG,"userId :  $userId")
            LOGGING.INFO(contextTAG,"userName :  $userName")
            LOGGING.INFO(contextTAG,"age :  $age")
            LOGGING.INFO(contextTAG,"email :  $email")
            LOGGING.INFO(contextTAG,"phoneNumber :  $phoneNumber")
            LOGGING.INFO(contextTAG,"loginTime :  $loginTime")
            LOGGING.INFO(contextTAG,"roomId :  $roomId")
            LOGGING.INFO(contextTAG,"adminStatus :  $adminStatus")
            LOGGING.INFO(contextTAG,"profileId :  $profileId")

            if (roomId.length <= 1) {
                isRoomLengthLessThanOne = true
            }

        } catch (e: JSONException) {
            navigateToLoginActivity = true
        } catch (e: IOException) {
            e.printStackTrace()
            navigateToLoginActivity = true
        }
    }


}