package com.example.integration1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class GetAllDataFragment : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var listView: ListView
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private val contextTAG : String = "GetAllDataFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_get_all_data, container, false)
        listView = v.findViewById(R.id.lv_items2)
        val roomActivity = activity as RoomActivity

        // Set item click listener
        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as HashMap<*, *>
            val userName = selectedItem["position1"].toString()
            val description = selectedItem["position2"].toString()
            val date = selectedItem["position3"].toString()
            val amount = selectedItem["position4"].toString()
            val id = selectedItem["position5"].toString()

            popUpDetails(id, userName, date, amount, description)
        }

        getItems(roomActivity)
        return v
    }

    private fun getItems(roomActivity: RoomActivity) {
        Log.i(contextTAG, "Entered in getItems Function")
        val roomId = userDataViewModel.roomId
        val param = "?action=getTotal&roomId=$roomId"
        val url = resources.getString(R.string.spreadsheet_url)
        roomActivity.animationView.setAnimation(R.raw.files_loading)
        roomActivity.animationView.playAnimation()
        roomActivity.alertDialog.show()
        val stringRequest = StringRequest(
            Request.Method.GET, url + param,
            { response -> parseItems(response, roomActivity) }
        ) { }
        val socketTimeOut = 50000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    private fun parseItems(jsonResponse: String, roomActivity: RoomActivity) {
        val list = ArrayList<HashMap<String, String?>>()
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")
            for (i in 0 until jsonArray.length()) {
                val jo1 = jsonArray.getJSONObject(i) // each user
                val jo2 = jo1.getJSONArray("records")

                for (j in 0 until jo2.length()) {
                    val jo = jo2.getJSONObject(j)

                    val dataID = jo.getString("dataId")
                    val name = jo.getString("userName")
                    var date = jo.getString("date")
                    date = GlobalFunctions.convertDateFormat(date)
                    val amount = jo.getString("amount")
                    var description = jo.getString("description")

                    if(description.length >= 20){
                        description = description.substring(0, 20) + ".."
                    }

                    val item = HashMap<String, String?>()
                    item["position1"] = name
                    item["position2"] = description
                    item["position3"] = date
                    item["position4"] = " ₹ $amount"
                    item["position5"] = dataID
                    list.add(item)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        adapter = SimpleAdapter(
            activity,
            list,
            R.layout.common_list_item,
            arrayOf("position1", "position2", "position3", "position4", "position5"),
            intArrayOf(
                R.id.user_name_tv_id,
                R.id.description_tv_id,
                R.id.date_tv_id,
                R.id.amount_tv_id
            )
        )
        listView.adapter = adapter
        roomActivity.alertDialog.dismiss()
    }

    private fun popUpDetails(
        id: String,
        userName: String,
        date: String,
        amount: String,
        description: String,
    ) {
        Log.i(contextTAG, "Entered in popUpDetails function")
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        val userNameD = view1.findViewById<TextView>(R.id.user_confirm_id)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        userNameD.text = getString(R.string.data_id_dialog_DD, userName)
        dateD.text = getString(R.string.date_dialog_DD, date)
        amountD.text = getString(R.string.amount_dialog_DD, amount)
        descriptionD.text = getString(R.string.description_dialog_DD, description)
        Log.i(contextTAG, "triggered $id $date $amount $description")
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.show()
    }
}