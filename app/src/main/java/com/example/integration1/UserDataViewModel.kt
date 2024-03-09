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

        Log.i(contextTAG, "Init Started")
        loadUserData()
        Log.i(contextTAG, "Init Completed")

    }

    fun getStoragePermissions(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(contextTAG, "storagePermissionGranted = $storagePermissionGranted")
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
        Log.i(contextTAG, "storagePermissionGranted = $storagePermissionGranted")
    }

    private fun loadUserData() {
        Log.i(contextTAG, "Entered in loadUserData Function")

        if (!ActivityUtils.directory.exists()) {
            isDirExist = false
        }
        if (!ActivityUtils.file.exists()) {
            isFileExist = false
        }

        try {
            val content = ActivityUtils.file.readText()
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

            Log.i(contextTAG,"userId :  $userId")
            Log.i(contextTAG,"userName :  $userName")
            Log.i(contextTAG,"age :  $age")
            Log.i(contextTAG,"email :  $email")
            Log.i(contextTAG,"phoneNumber :  $phoneNumber")
            Log.i(contextTAG,"loginTime :  $loginTime")
            Log.i(contextTAG,"roomId :  $roomId")
            Log.i(contextTAG,"adminStatus :  $adminStatus")
            Log.i(contextTAG,"profileId :  $profileId")

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