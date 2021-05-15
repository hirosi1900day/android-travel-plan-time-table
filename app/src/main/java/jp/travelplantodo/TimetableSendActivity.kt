package jp.travelplantodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_timetable_send.*

class TimetableSendActivity : AppCompatActivity(), View.OnClickListener {

    var travelPlanId = ""
    private var mDatabaseReference = FirebaseFirestore.getInstance()
    var timeTableHour: String = ""
    var timeTableMinuite: String = ""
    var timeTableYear: String = ""
    var timeTableMonth: String = ""
    var timeTableDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_send)
        travelPlanId = intent.getStringExtra(EXTRA_TRAVEL_PLAN_ID_Tiem_Table).toString()
        if (travelPlanId == null) {
            travelPlanId = ""
        }
        timeTableSendButton.setOnClickListener(this)
        timeTableDateButton.setOnClickListener(this)
        timeTableTimeButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if(v == timeTableSendButton) {
            val timetableContent = timeTableContent.text.toString()
            val timetableDateTimeContent = timeTableDateTimeContent.text.toString()
            if (timetableContent.length ==0 || timetableDateTimeContent.length == 0) {
                return
            }
            val timeTableData = HashMap<String, String>()
            timeTableData["content"] = timetableContent
            timeTableData["dateTime"] = timetableDateTimeContent
            timeTableData["groupId"] = travelPlanId

            progressBar.visibility = View.VISIBLE
            timeTableSaveToFirebase(timeTableData)
        }

        if(v == timeTableTimeButton) {
            showTimePickerDialog()
        }

        if (v == timeTableDateButton) {
            showDatePickerDialog()
        }
    }


    private fun timeTableSaveToFirebase(Data: HashMap<String, String>) {
        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            TimetablePATH).document().set(Data).addOnSuccessListener {
            progressBar.visibility = View.GONE
            Snackbar.make(findViewById(android.R.id.content), getString(R.string. time_table_send_success_message), Snackbar.LENGTH_LONG)
            finish()
        }
            .addOnFailureListener {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.time_table_send_error_message), Snackbar.LENGTH_LONG).show()
            }

    }

    private fun saveInt(number: Int,key: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putInt(key, number)
        editor.commit()
    }

    private fun timeTableDateTimeContentDispley(year: String, month: String, date: String, hour: String, minuite: String) {
        timeTableDateTimeContent.text = "${year}/${month}/${date}  ${hour}:${minuite}"
    }


    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                Log.d("UI_PARTS", "$hour:$minute")
                timeTableHour = hour.toString()
                timeTableMinuite = minute.toString()
                timeTableDateTimeContentDispley(timeTableYear, timeTableMonth, timeTableDate,timeTableHour, timeTableMinuite)
            },
            12, 0, true)
        timePickerDialog.show()

    }

    private fun showDatePickerDialog() {

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        var preferencesyear = sp.getInt(YearKEY, 2021)
        var preferencesMonth = sp.getInt(MonthKEY, 4)
        var preferencesDate = sp.getInt(DateKEY, 1)


        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener() {view, year, month, dayOfMonth->
                Log.d("UI_PARTS", "$year/${month+1}/$dayOfMonth")
                timeTableYear = year.toString()
                timeTableMonth = (month+1).toString()
                timeTableDate = dayOfMonth.toString()
                saveInt(year, YearKEY)
                saveInt((month), MonthKEY)
                saveInt(dayOfMonth, DateKEY)
                timeTableDateTimeContentDispley(timeTableYear, timeTableMonth, timeTableDate,timeTableHour, timeTableMinuite)
            },
            preferencesyear,
            preferencesMonth,
            preferencesDate)
        datePickerDialog.show()

    }
}


