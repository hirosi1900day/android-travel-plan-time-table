package jp.travelplantodo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_travel_plan_send.*

class TravelPlanSendActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_plan_send)

        sendButton.setOnClickListener(this)
    }

    override fun onClick(v: View){
       if (v == sendButton){
           // キーボードが出てたら閉じる
           val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
           im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

           val dataBaseReference = FirebaseFirestore.getInstance()

           val data = HashMap<String, String>()
           val uid =  FirebaseAuth.getInstance().currentUser!!.uid
           if (uid != null) {
               data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
           }
           // タイトルと本文を取得する
           val title = titleText.text.toString()
           val body = bodyText.text.toString()

           if (title.isEmpty()) {
               // タイトルが入力されていない時はエラーを表示するだけ
               Snackbar.make(v, getString(R.string.input_title), Snackbar.LENGTH_LONG).show()
               return
           }

           if (body.isEmpty()) {
               // 質問が入力されていない時はエラーを表示するだけ
               Snackbar.make(v, getString(R.string.travel_plan_message), Snackbar.LENGTH_LONG).show()
               return
           }

           // Preferenceから名前を取る
           val sp = PreferenceManager.getDefaultSharedPreferences(this)
           val name = sp.getString(NameKEY, "")

           val travelPlanId = randomString(20)


           data["title"] = title
           data["body"] = body
           data["name"] = name!!
           data["travelPlanId"] = travelPlanId!!

           val memberData = HashMap<String, String>()
           memberData["uid"] = uid
           memberData["name"] = name!!
           memberData["travelPlanId"] = travelPlanId

           dataBaseReference.collection(TravelPlanIndexPath).document(travelPlanId).set(data)
               .addOnCompleteListener{
                   dataBaseReference.collection(UsersPATH).document(uid)
                       .collection(MemberPATH).document(travelPlanId).set(memberData) .addOnSuccessListener {
                       finish()
                       Log.d("確認","送信完了members")
                   }
                       .addOnFailureListener {
                           Snackbar.make(
                               findViewById(android.R.id.content),
                               getString(R.string.send_error_message),
                               Snackbar.LENGTH_LONG
                           ).show()
                           Log.d("確認","送信失敗memebers")
                       }
               }
               .addOnSuccessListener {
                   finish()
                   Log.d("確認","送信完了")
               }
               .addOnFailureListener {
                   Snackbar.make(
                       findViewById(android.R.id.content),
                       getString(R.string.send_error_message),
                       Snackbar.LENGTH_LONG
                   ).show()
                   Log.d("確認","送信失敗")
               }
           progressBar.visibility = View.VISIBLE

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
        Log.d("文字列作成","${randomString}")
        return randomString

    }






}


