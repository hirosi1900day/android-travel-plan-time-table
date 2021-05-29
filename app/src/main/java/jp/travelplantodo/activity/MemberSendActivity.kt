package jp.travelplantodo.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_member_send.*
import kotlinx.android.synthetic.main.activity_member_send.progressBar

const val EXTRA_TRAVEL_PLAN_ID_GoodMember = "jp.travelplantodo.activity.GoodMember"

class MemberSendActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var travelPlanId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_send)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        travelPlanId = intent.getStringExtra(EXTRA_TRAVEL_PLAN_ID_Member).toString()
        if (travelPlanId == null) {
            travelPlanId = ""
        }

        sendButton.setOnClickListener(this)
        goodMemberButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        progressBar.visibility = View.VISIBLE
        if(v == sendButton) {
            sendButtonFunction(v!!)
        }

        //親しい友達リストから追加する
        if(v == goodMemberButton) {
            goodMemberButtonFunction()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun goodMemberButtonFunction() {
        val intent = Intent(this, GoodMemberActivity::class.java)
        intent.putExtra(EXTRA_TRAVEL_PLAN_ID_GoodMember, travelPlanId)
        startActivity(intent)
        progressBar.visibility = View.GONE
    }

    private fun sendButtonFunction(v: View) {
        val memberID = memberIdView.text.toString()
        if(memberID.length != 0){

            var userUid = ""
            var userName = ""

            val userIdRef = FirebaseFirestore.getInstance().collection(IdPATH).document(memberID)
            userIdRef.get().addOnSuccessListener { document ->
                val data = document.data as Map<*, *>?
                if(document.data == null) {
                    Snackbar.make(v!!, "一致するユーザーが見つかりませんでした", Snackbar.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }else {
                    Log.d("確認memberSendActivity", "${userUid}")
                    userUid = data!!["userUid"].toString()
                    userName = data!!["userName"].toString()
                    Snackbar.make(v!!, userName + "に追加申請を送りました", Snackbar.LENGTH_LONG)
                        .show()

                    val addingPlanMemberData = HashMap<String, String>()

                    val sp = PreferenceManager.getDefaultSharedPreferences(this)
                    val name = sp.getString(NameKEY, "")

                    addingPlanMemberData["sendUserName"] = name.toString()
                    addingPlanMemberData["userUid"] = userUid
                    addingPlanMemberData["userName"] = userName
                    addingPlanMemberData["travelPlanId"] = travelPlanId

                    FirebaseFirestore.getInstance().collection(TemporaryMemberPATH)
                        .document(userUid).collection(MemeberTravelPlanSelectPATH).document(userUid + travelPlanId).set(addingPlanMemberData)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(
                                v!!,
                                getString(R.string.member_send_success),
                                Snackbar.LENGTH_LONG
                            )

                            val sendUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                            addGoodMember(userName, userUid, name.toString(), sendUserUid)
                        }

                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(
                                v!!,
                                getString(R.string.member_send_error),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    progressBar.visibility = View.GONE
                }
            }

        }else {
            Snackbar.make(v!!, getString(R.string.member_send_error_message), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun addGoodMember(userName: String, userUid: String, sendUserName: String, sendUserUid: String) {
        // ダイアログを表示する
        val goodMemberData = HashMap<String, String>()
        goodMemberData["userUid"] = userUid
        goodMemberData["userName"] = userName
        goodMemberData["sendUserUid"] = sendUserUid
        goodMemberData["sendUserName"] = sendUserName

        FirebaseFirestore.getInstance().collection(GoodMemberPATH).document(sendUserUid).collection(
            GoodMemberInformationPATH).document(userUid).get()
            .addOnSuccessListener {
                document ->

                Log.d("document確認","${document}")
                if(document.data == null) {
                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("親しい友達に追加しますか？")
                    builder.setMessage(userName + "を親しい友達に追加しますか？")

                    builder.setPositiveButton("OK"){_, _ ->
                        FirebaseFirestore.getInstance().collection(GoodMemberPATH).document(sendUserUid).collection(
                            GoodMemberInformationPATH).document(userUid).set(goodMemberData)

                        finish()
                    }

                    builder.setNegativeButton("CANCEL"){_, _ ->
                        finish()
                    }

                    val dialog = builder.create()
                    dialog.show()
                    progressBar.visibility = View.GONE
                }else {
                    progressBar.visibility = View.GONE
                }
            }
    }
}