package jp.travelplantodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_gift_send.*



class GiftSendActivity : AppCompatActivity(), View.OnClickListener {


    var travelPlanId = ""
    private var mDatabaseReference = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift_send)
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
            giftData["storeName"] = storeName
            giftData["giftName"] = giftName
            giftData["userName"] = userName
            giftData["uid"] = userUid
            giftData["groupId"] = travelPlanId

            progressBar.visibility = View.VISIBLE
            giftTableSaveToFirebase(giftData)
        }

    }


    private fun giftTableSaveToFirebase(Data: HashMap<String, String>) {
        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            GifttablePATH).document().set(Data)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), getString(R.string. gift_send_success_message), Snackbar.LENGTH_LONG)
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), getString(R.string. gift_send_error_message), Snackbar.LENGTH_LONG).show()
            }
    }
}