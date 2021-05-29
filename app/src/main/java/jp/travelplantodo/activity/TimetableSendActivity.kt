package jp.travelplantodo.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_timetable_send.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime.parse
import java.time.LocalTime.parse
import java.util.logging.Level.parse

class TimetableSendActivity : AppCompatActivity(), View.OnClickListener {

    var travelPlanId = ""
    private var mDatabaseReference = FirebaseFirestore.getInstance()
    var timeTableHour = 0
    var timeTableMinuite = 0
    var timeTableYear = 0
    var timeTableMonth = 0
    var timeTableDate = 0
    var timetableDateTimeContent = ""
    var timetableContent = ""
    var timeDateNumber = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_send)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
            timetableContent = timeTableContent.text.toString()
            timetableDateTimeContent = timeTableDateTimeContent.text.toString()
            if (timetableContent.length ==0 || timetableDateTimeContent.length == 0) {
                return
            }

            val timeTableData = HashMap<String, String>()
            timeTableData["id"] = randomString(20)
            timeTableData["content"] = timetableContent
            timeTableData["dateTime"] = timetableDateTimeContent
            timeTableData["groupId"] = travelPlanId
            timeTableData["timeDateNumber"] = timeDateNumber.toString()


            progressBar.visibility = View.VISIBLE
            // キーボードが出てたら閉じる　ここを追加
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v!!.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            timeTableSaveToFirebase(timeTableData)
        }

        if(v == timeTableTimeButton) {
            showTimePickerDialog()
        }

        if (v == timeTableDateButton) {
            showDatePickerDialog()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun timeTableSaveToFirebase(Data: HashMap<String, String>) {

        if(Data["id"] != null) {
            mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
                TimetablePATH
            ).document(Data["id"].toString()).set(Data)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.time_table_send_success_message),
                        Snackbar.LENGTH_LONG
                    )
                    finish()
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    progressBar.visibility = View.GONE
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.time_table_send_error_message),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun saveInt(number: Int,key: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putInt(key, number)
        editor.commit()
    }

    private fun timeTableDateTimeContentDispley(year: Int, month: Int, date: Int, hour: Int, minuite: Int) {

        timeDateNumber = (year*100000000 + month*100000 + date*10000 + hour*100 + minuite)
        val years = year.toString()
        val months = month.toString()
        val dates = date.toString()
        val hours = hour.toString()
        val minuites = minuite.toString()
        timeTableDateTimeContent.text = "${years}/${months}/${dates}  ${hours}:${minuites}"
//        timeDateNumber = year.toInt()*10000 + month.toInt()*1000 + date.toInt()*100 + date.toInt()*10 + minuite.toInt()
    }


    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                Log.d("UI_PARTS", "$hour:$minute")
                timeTableHour = hour
                timeTableMinuite = minute
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

                timeDateNumber += (year*10000 + (month+1)*1000 + dayOfMonth*100)

                timeTableYear = year
                timeTableMonth = (month+1)
                timeTableDate = dayOfMonth
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

    private fun randomString(StrLength: Int): String {
        val source = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        val len: Int = source.length

        var num = 0 //事前にvar num の定義が残っている場合は、エラーがでるので、var を削除しましょう。

        var randomString = ""
        while (num < StrLength) {
            val rand = (1..len-1).random()
            var nextChar = source.substring(rand-1, rand)
            randomString += nextChar
            num++
        }
        return randomString
    }

}


