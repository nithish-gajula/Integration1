package com.example.integration1

import CustomAdapter
import Item
import Section
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetDataFragment : Fragment() {
    private lateinit var adapter: ListAdapter
    private lateinit var listView: ListView
    private lateinit var totalAmount: TextView
    private lateinit var getIdET: EditText

    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val groupedItemsJson = JSONObject()

    private val contextTAG: String = "GetDataFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_get_data, container, false)
        listView = v.findViewById(R.id.lv_items)
        totalAmount = v.findViewById(R.id.total_Amount_id)
        val roomActivity = activity as RoomActivity
        getItems(roomActivity)

        // Set item click listener
        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as HashMap<*, *>
            val userName = selectedItem["position1"].toString()
            val description = selectedItem["position2"].toString()
            val date = selectedItem["position3"].toString()
            val amount = selectedItem["position4"].toString()
            val id = selectedItem["position5"].toString()

            delete(id, userName, date, amount, description, roomActivity)
        }

        return v
    }

    private fun getItems(roomActivity: RoomActivity) {

        val userId = userDataViewModel.userId
        val roomId = userDataViewModel.roomId

        val param = "?action=getItem&userId=$userId&roomId=$roomId"
        val url = resources.getString(R.string.spreadsheet_url)
        roomActivity.animationView.setAnimation(R.raw.card_loading)
        roomActivity.animationView.playAnimation()
        roomActivity.alertDialog.show()
        val stringRequest = StringRequest(
            Request.Method.GET, url + param,
            { response ->
                Log.i(contextTAG, "response = $response")
                parseItems(response, roomActivity)
            }
        ) { error ->
            Log.i(contextTAG, "error = $error")
        }
        val socketTimeOut = 50000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    /*


    private fun parseItems(jsonResponse: String, roomActivity: RoomActivity) {
        val list = ArrayList<HashMap<String, String?>>()
        val monthItemsMap = HashMap<String, ArrayList<HashMap<String, String?>>>()
        val imageNames = arrayOf(R.mipmap.meat, R.mipmap.shopping_cart, R.mipmap.rice)
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")
            var amountTemp = 0
            for (i in 0 until jsonArray.length()) {
                val jo = jsonArray.getJSONObject(i)
                val dataId = jo.getString("dataId")
                var date = jo.getString("date")
                date = GlobalFunctions.convertDateFormat(date)
                val amount = jo.getString("amount")
                var description = jo.getString("description")

                if (description.length >= 20) {
                    description = description.substring(0, 20) + ".."
                }

                amountTemp += amount.toInt()
                val item = HashMap<String, String?>()
                item["position1"] = userDataViewModel.userName
                item["position2"] = description
                item["position3"] = date
                item["position4"] = "₹ $amount"
                item["position5"] = dataId
                item["position6"] = dataId
                list.add(item)
            }
            totalAmount.text = getString(R.string.total_amount_GD, amountTemp)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        adapter = SimpleAdapter(
            activity,
            list,
            //R.layout.list_item_get_data,
            R.layout.common_list_item,
            arrayOf(
                "position1",
                "position2",
                "position3",
                "position4",
                "position5"
            ),
            intArrayOf(
                //R.id.lv_2_date_id,
                //R.id.lv_2_amount_id,
                //R.id.lv_2_description_id
                R.id.user_name_tv_id,
                R.id.description_tv_id,
                R.id.date_tv_id,
                R.id.amount_tv_id
                //R.id.profile_image
            )
        )
        listView.adapter = adapter
        roomActivity.alertDialog.dismiss()
    }



     */


    private fun parseItems(jsonResponse: String, roomActivity: RoomActivity) {
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val jo = jsonArray.getJSONObject(i)
                val dateFormats = convertDateFormat(jo.getString("date"))
                val monthKey = dateFormats.format2

                if (groupedItemsJson.has(monthKey)) {
                    val monthData =
                        groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                    val monthTotal =
                        groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

                    val newData = JSONObject().apply {
                        put("position1", "Nithish Gajula")
                        put("position2", limitDescription(jo.getString("description")))
                        put("position3", dateFormats.format1)
                        put("position4", "₹ ${jo.getString("amount")}")
                        put("position5", jo.getString("dataId"))
                    }
                    monthData.put(newData)
                    groupedItemsJson.getJSONObject(monthKey)
                        .put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
                } else {
                    val newDataArray = JSONArray()
                    val newData = JSONObject().apply {
                        put("position1", "Nithish Gajula")
                        put("position2", limitDescription(jo.getString("description")))
                        put("position3", dateFormats.format1)
                        put("position4", "₹ ${jo.getString("amount")}")
                        put("position5", jo.getString("dataId"))
                    }
                    newDataArray.put(newData)

                    val monthObject = JSONObject().apply {
                        put("MonthName", monthKey)
                        put("MonthData", newDataArray)
                        put("MonthTotal", jo.getString("amount").toDouble())
                    }

                    groupedItemsJson.put(monthKey, monthObject)
                }
            }

            val months = groupedItemsJson.keys().asSequence().toList()
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }

            categorizeItems(sortedMonths, roomActivity)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun categorizeItems(months: List<String>, roomActivity: RoomActivity) {
        val dataList = mutableListOf<Any>()
        try {
            for (i in months.indices) {
                val monthJsonObject = groupedItemsJson.getJSONObject(months[i])
                dataList.add(
                    Section(
                        monthJsonObject.getString("MonthName"),
                        monthJsonObject.getString("MonthTotal")
                    )
                )

                val monthData = monthJsonObject.getJSONArray("MonthData")
                for (j in 0 until monthData.length()) {
                    val itemData = monthData.getJSONObject(j)
                    dataList.add(
                        Item(
                            itemData.getString("position1"),
                            itemData.getString("position2"),
                            itemData.getString("position3"),
                            itemData.getString("position4"),
                            itemData.getString("position5")
                        )
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        adapter = CustomAdapter(requireContext(), dataList)
        listView.adapter = adapter

        roomActivity.alertDialog.dismiss()
    }

    private fun limitDescription(description: String): String {
        return if (description.length >= 20) "${description.substring(0, 20)}.." else description
    }

    private data class DateFormats(val format1: String, val format2: String)

    private fun convertDateFormat(dateString: String): DateFormats {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateString) ?: return DateFormats("", "")

        val outputDateFormat1 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val outputDateFormat2 = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val formattedDate1 = outputDateFormat1.format(date)
        val formattedDate2 = outputDateFormat2.format(date)

        return DateFormats(formattedDate1, formattedDate2)
    }

    private fun delete(
        id: String,
        userName: String,
        date: String,
        amount: String,
        description: String,
        roomActivity: RoomActivity
    ) {
        Log.i(contextTAG, "Entered in delete function")
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        val userNameD = view1.findViewById<TextView>(R.id.user_confirm_id)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        val cancel = view1.findViewById<Button>(R.id.cancel_confirm_id)
        val upload = view1.findViewById<Button>(R.id.confirm_confirm_id)
        val ll1 = view1.findViewById<LinearLayout>(R.id.ll1)
        val anm1 = view1.findViewById<LottieAnimationView>(R.id.lottie_animation_1)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        userNameD.text = getString(R.string.data_id_dialog_DD, userName)
        dateD.text = getString(R.string.date_dialog_DD, date)
        amountD.text = getString(R.string.amount_dialog_DD, amount)
        descriptionD.text = getString(R.string.description_dialog_DD, description)
        Log.i(contextTAG, "triggered $id $date $amount $description")
        dialog1.setCanceledOnTouchOutside(false)
        dialog1.show()

        upload.setOnClickListener {
            ll1.visibility = View.GONE
            anm1.visibility = View.VISIBLE
            anm1.setAnimation(R.raw.please_wait)
            anm1.playAnimation()
            val url = resources.getString(R.string.spreadsheet_url)
            Log.i(
                contextTAG,
                "userId=${userDataViewModel.userId}, roomId=${userDataViewModel.roomId}, rowID=$id"
            )
            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        anm1.setAnimation(R.raw.delete_file)
                        anm1.playAnimation()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog1.dismiss()
                            getItems(roomActivity)
                        }, 2000)
                        getIdET.setText("")
                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener {
                        anm1.setAnimation(R.raw.error)
                        anm1.playAnimation()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val param: MutableMap<String, String> =
                            HashMap()

                        //here we pass params
                        param["action"] = "deleteItem"
                        param["userId"] = userDataViewModel.userId
                        param["roomId"] = userDataViewModel.roomId
                        param["dataId"] = id
                        return param


                    }
                }
            val socketTimeOut = 50000 // u can change this .. here it is 50 seconds
            val retryPolicy: RetryPolicy =
                DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            stringRequest.setRetryPolicy(retryPolicy)
            val queue = Volley.newRequestQueue(activity)
            queue.add(stringRequest)
        }
        cancel.setOnClickListener { dialog1.dismiss() }
    }


}