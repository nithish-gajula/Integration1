package com.example.integration1

import ActivityUtils
import LOGGING
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

    init {
        loadUserData()
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

            val info = "userid : $userId \n" +
                    "userName :  $userName \n" +
                    "age :  $age \n" +
                    "email :  $email \n" +
                    "phoneNumber :  $phoneNumber \n" +
                    "loginTime :  $loginTime \n" +
                    "roomId :  $roomId \n" +
                    "adminStatus :  $adminStatus \n" +
                    "profileId :  $profileId "



            LOGGING.INFO(contextTAG,info)

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