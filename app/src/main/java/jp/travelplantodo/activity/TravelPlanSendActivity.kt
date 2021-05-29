package jp.travelplantodo.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_travel_plan_send.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.changeButton
import java.io.ByteArrayOutputStream

class TravelPlanSendActivity : AppCompatActivity(), View.OnClickListener {

    private var mPictureUri: Uri? = null

    companion object {
        private val PERMISSIONS_REQUEST_CODE_SEND = 250
        private val CHOOSER_REQUEST_CODE_SEND = 250
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_plan_send)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sendButton.setOnClickListener(this)
        travelSendImageView.setOnClickListener(this)
    }

    override fun onClick(view: View){
       if (view == sendButton){
           // キーボードが出てたら閉じる
           val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
           im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
           saveTravelPlan(view)
       }

        if(view == travelSendImageView){
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE_SEND
                    )
                    return
                }
            } else {
                showChooser()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    private fun saveTravelPlan(v: View) {
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

        val drawable = travelSendImageView.drawable as? BitmapDrawable
        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            data["image"] = bitmapString
        }

        val memberData = HashMap<String, String>()
        memberData["uid"] = uid
        memberData["name"] = name!!
        memberData["travelPlanId"] = travelPlanId

        val TravelPlanRoomMemberData = HashMap<String, String>()
        TravelPlanRoomMemberData["uid"] = uid
        TravelPlanRoomMemberData["name"] = name!!


        dataBaseReference.collection(TravelPlanIndexPath).document(travelPlanId).set(data)
            .addOnCompleteListener{

                //トラベルルームにmember情報を保存する
                dataBaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
                    TravelRoomMemberPATH
                ).document(uid).set(TravelPlanRoomMemberData)

                //uidにトラベルプランの情報を保存する
                dataBaseReference.collection(UsersPATH).document(uid)
                    .collection(TravelPlanMemberPATH).document(travelPlanId).set(memberData) .addOnSuccessListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSER_REQUEST_CODE_SEND) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            travelSendImageView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.get_image))

        // EXTRA_INITIAL_INTENTSにカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE_SEND)
    }


    //travelPlanId作成のため任意な文字列を生成する。
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


