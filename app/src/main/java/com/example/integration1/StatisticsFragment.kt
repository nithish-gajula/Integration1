package com.example.integration1

import Item
import LOGGING
import Section
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsFragment : Fragment() {

    private val names = arrayOf("nithish", "prudhvi", "rahman")
    private lateinit var personsTIL : TextInputLayout
    private lateinit var personsATV : AutoCompleteTextView
    private lateinit var aaChartView: AAChartView

    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val groupedItemsJson = JSONObject()
    private lateinit var roomActivity: RoomActivity

    private val contextTAG: String = "StatisticsFragment"


    /*************************************
     * Implement following Charts, Also try Material Dat Visualization
     * Pie Chart, Bar Chart, Radar Chart
     * -----------Specific -------------
     * Specific Person - Specific Month
     * Specific Person - Overall
     * -----------Overall --------------
     * Everyone - Specific Month
     * Everyone - Overall
     **************************************/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_statistics, container, false)

        roomActivity = activity as RoomActivity
        getItems()

        /*
        personsTIL = v.findViewById(R.id.person_til)
        personsATV = v.findViewById(R.id.personid)
        aaChartView = v.findViewById(R.id.aa_chart_view)
        loadBarChart()

        val adapter = ArrayAdapter(requireContext(), R.layout.select_person_chart, names)

        // Apply the adapter to the AutoCompleteTextView
        personsATV.setAdapter(adapter)

        personsATV.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
        }

         */

        return v
    }

    private fun getItems() {
        LOGGING.INFO(contextTAG, "Entered in getItems Function")
        val roomId = userDataViewModel.roomId
        val param = "?action=getTotal&roomId=$roomId"
        val url = resources.getString(R.string.spreadsheet_url)
        roomActivity.animationView.setAnimation(R.raw.files_loading)
        roomActivity.animationView.playAnimation()
        roomActivity.alertDialog.show()
        val stringRequest = StringRequest(
            Request.Method.GET, url + param,
            { response -> parseItems(response) }
        ) { }

        val policy: RetryPolicy =
            DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    private fun parseItems(jsonResponse: String) {
        LOGGING.DEBUG(contextTAG, "Entered in parseItems function")
        //LOGGING.DEBUG(contextTAG, "jsonResponse :  $jsonResponse")
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val jo1 = jsonArray.getJSONObject(i)

                val jo2 = jo1.getJSONArray("records")

                for (j in 0 until jo2.length()) {
                    val jo = jo2.getJSONObject(j)

                    val dateFormats = convertDateFormat(jo.getString("date"))
                    val monthKey = dateFormats.format2

                    if (groupedItemsJson.has(monthKey)) {
                        val monthData =
                            groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                        val monthTotal =
                            groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", dateFormats.format1)
                            put("position3", jo.getString("amount"))
                        }

                        monthData.put(newData)
                        groupedItemsJson.getJSONObject(monthKey)
                            .put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
                    } else {
                        val newDataArray = JSONArray()
                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", dateFormats.format1)
                            put("position3", jo.getString("amount"))
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

            }

            val months = groupedItemsJson.keys().asSequence().toList()
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }

            LOGGING.INFO(contextTAG,groupedItemsJson.toString())
            LOGGING.INFO(contextTAG,sortedMonths.toString())

        } catch (e: JSONException) {
            roomActivity.alertDialog.dismiss()
            e.printStackTrace()
        }
        roomActivity.alertDialog.dismiss()
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


    /*
    private fun loadBarChart(){

        val aaChartModel : AAChartModel = AAChartModel()
            .chartType(AAChartType.Bar)
            .title("title")
            .subtitle("subtitle")
            .backgroundColor("#ffffff")
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .name("Tokyo")
                    .data(arrayOf(7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6)),
                AASeriesElement()
                    .name("NewYork")
                    .data(arrayOf(0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5)),
                AASeriesElement()
                    .name("London")
                    .data(arrayOf(0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0)),
                AASeriesElement()
                    .name("Berlin")
                    .data(arrayOf(3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8))
            )
            )

        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

     */
}