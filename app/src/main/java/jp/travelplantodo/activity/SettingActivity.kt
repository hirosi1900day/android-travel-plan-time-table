package jp.travelplantodo.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
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
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.ByteArrayOutputStream

class SettingActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private lateinit var mDataBaseReference: FirebaseFirestore
    private var mPictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        val myId = sp.getString(MyIdKEY,"")
        nameText.setText(name)
        idText.setText(myId)

        mDataBaseReference = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid


        if(uid != null) {

            mDataBaseReference.collection(UsersPATH).document(uid!!).get().addOnSuccessListener { document ->
                if (document != null) {
                    val data = document.data as Map<*, *>?

                    if (data!!["image"] != null) {
                        val imageString = data!!["image"] as String
                        val bytes = Base64.decode(imageString, Base64.DEFAULT)
                        val image =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                                .copy(Bitmap.Config.ARGB_8888, true)
                        imageView.setImageBitmap(image)
                    }
                }
            }
        }

        // UIの初期設定
        title = getString(R.string.settings_titile)

        imageView.setOnClickListener(this)

        changeButton.setOnClickListener{v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val name2 = nameText.text.toString()

            val myId2 = idText.text.toString()
            //名前変更実装
            if (name2.length > 0 && name != null) {
                if(myId2.length >= 10 && myId != null) {
                    Change(v)
                    myIdChange(v, name2)
                }else {
                    Snackbar.make(v, getString(R.string.id_error_text), Snackbar.LENGTH_LONG).show()
                }
            }else {
                Snackbar.make(v, getString(R.string.name_text), Snackbar.LENGTH_LONG).show()
            }

        }

        logoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v, getString(R.string.logout_complete_message), Snackbar.LENGTH_LONG).show()
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onClick(v: View) {
        if(v == imageView){
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

                    return
                }
            } else {
                showChooser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSER_REQUEST_CODE) {

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
            imageView.setImageBitmap(resizedImage)

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

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    private fun myIdChange(v: View?, userName: String) {
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていない場合は何もしない
            if (v != null) {
                Snackbar.make(v, getString(R.string.no_login_user), Snackbar.LENGTH_LONG).show()
            }
        } else {
            val newMyId = idText.text.toString()
            val myIdData = HashMap<String,String>()
            val userInformationData = HashMap<String,String>()
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val pastMyId = sp.getString(MyIdKEY,"")
            myIdData["myId"] = newMyId
            userInformationData["userUid"] = user.uid
            userInformationData["userName"] = userName
            val userMyIdRef = mDataBaseReference.collection(UsersPATH).document(user.uid).collection(
                IdPATH
            ).document(user.uid)
            mDataBaseReference.collection(IdPATH).document(pastMyId!!).delete()

            val myIdUserRef = mDataBaseReference.collection(IdPATH).document(newMyId)


            myIdUserRef.set(userInformationData)

            userMyIdRef.set(myIdData)

            val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = sp2.edit()
            editor.putString(MyIdKEY, newMyId)
            editor.commit()
            if (v != null) {
                Snackbar.make(v, getString(R.string.change_disp_myid), Snackbar.LENGTH_LONG).show()
            }

        }
    }

    private fun Change(v: View?) {

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていない場合は何もしない
            if (v != null) {
                Snackbar.make(v, getString(R.string.no_login_user), Snackbar.LENGTH_LONG).show()
            }
        } else {

            val data = HashMap<String, String>()
            // 添付画像を取得する
            val drawable = imageView.drawable as? BitmapDrawable
            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }
            // 変更した表示名をFirebaseに保存する
            val name2 = nameText.text.toString()
            val userRef = mDataBaseReference.collection(UsersPATH).document(user.uid)

            data["name"] = name2
            userRef.set(data)

            // 変更した表示名をPreferenceに保存する
            val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = sp2.edit()
            editor.putString(NameKEY, name2)
            editor.commit()
            if (v != null) {
                Snackbar.make(v, getString(R.string.change_disp_name), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}