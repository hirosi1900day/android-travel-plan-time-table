package jp.travelplantodo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_gift_send.*
import kotlinx.android.synthetic.main.activity_main.*


class GiftSendActivity : AppCompatActivity(), View.OnClickListener {


    var travelPlanId = ""
    private var mDatabaseReference = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift_send)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        travelPlanId = intent.getStringExtra(EXTRA_TRAVEL_PLAN_ID_Gift).toString()
        if (travelPlanId == null) {
            travelPlanId = ""
        }
        giftSendButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v == giftSendButton) {

            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val userName = sp.getString(NameKEY, "").toString()

            var userUid = FirebaseAuth.getInstance().currentUser!!.uid
            if (userUid == null) {
                userUid = ""
            }

            val storeName = giftStoreNameTextView.text.toString()
            val giftName = giftNameTextView.text.toString()

            if (storeName.length ==0 || giftName.length == 0) {
                return
            }
            val giftData = HashMap<String, String>()
            giftData["id"] = randomString(20)
            giftData["storeName"] = storeName
            giftData["giftName"] = giftName
            giftData["userName"] = userName
            giftData["uid"] = userUid
            giftData["groupId"] = travelPlanId

            progressBar.visibility = View.VISIBLE
            giftTableSaveToFirebase(giftData)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun giftTableSaveToFirebase(Data: HashMap<String, String>) {
        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(GifttablePATH)
            .document(Data["id"].toString()).set(Data)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.gift_send_success_message), Snackbar.LENGTH_LONG)
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.gift_send_error_message), Snackbar.LENGTH_LONG).show()
            }
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