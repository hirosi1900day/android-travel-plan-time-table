package jp.travelplantodo

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
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseError
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.changeButton
import java.io.ByteArrayOutputStream


class SettingFragment: Fragment(), View.OnClickListener{

    companion object {
        private val PERMISSIONS_REQUEST_CODE_SETTING_FRAGMENT = 200
        private val CHOOSER_REQUEST_CODE_SETTING_FRAGMENT = 200
    }

    private var mPictureUri: Uri? = null

    var travelPlanId = ""
    var title = ""
    var body = ""
    var userName = ""
    var uid = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
        // fragment_api.xmlが反映されたViewを作成して、returnします
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // RecyclerViewの初期化
        getData()
        travelImageView.setOnClickListener (this)
        changeButton.setOnClickListener(this)

    }
    //fragmentAdapterよりtravelPlanIdを取得する
   fun getPlanId(travelPlanIdFromFragmentStatePagerAdapter: String) {
       travelPlanId = travelPlanIdFromFragmentStatePagerAdapter
   }

    override fun onClick(v: View) {
        if(v == travelImageView){
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE_SETTING_FRAGMENT
                    )
                    return
                }
            } else {
                showChooser()
            }
        }

        if(v == changeButton) {
            changeButtonFun(v)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSER_REQUEST_CODE_SETTING_FRAGMENT) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    activity?.contentResolver?.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = activity?.contentResolver
                val inputStream = contentResolver?.openInputStream(uri!!)
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
            travelImageView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    private fun getData() {

        FirebaseFirestore.getInstance().collection(TravelPlanIndexPath).document(travelPlanId).get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data as Map<*, *>?

                if (data!!["image"] != null) {
                    val imageString = data!!["image"] as String
                    val bytes = Base64.decode(imageString, Base64.DEFAULT)
                    val image =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                            .copy(Bitmap.Config.ARGB_8888, true)
                    travelImageView.setImageBitmap(image)
                }

                userName = data["name"] as String
                uid = data["uid"] as String
                title = data["title"] as String
                body = data["body"] as String

                travelPlanTitleSetting.setText(title)
                travelPlanContentSetting.setText(body)

            }
        }
    }

    private fun changeButtonFun(v: View) {
        // キーボードが出ていたら閉じる
        val im =
            activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val inputTitle = travelPlanTitleSetting.text.toString()

        val inputContent = travelPlanContentSetting.text.toString()

        val data = HashMap<String, String>()
        // 添付画像を取得する
        val drawable = travelImageView.drawable as? BitmapDrawable
        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            data["image"] = bitmapString
        }
        if(inputTitle.length == 0) {
            data["title"] = title
        }else {
            data["title"] = inputTitle
        }

        if (inputContent.length == 0) {
            data["body"] = body
        }else {
            data["body"] = inputContent
        }

        data["travelPlanId"] = travelPlanId

        data["name"] = userName
        data["uid"] = uid

        FirebaseFirestore.getInstance().collection(TravelPlanIndexPath).document(travelPlanId).set(data)
            .addOnSuccessListener {
                Snackbar.make(v, "保存に成功しました", Snackbar.LENGTH_LONG).show()

            }
            .addOnFailureListener {
                Snackbar.make(v, "保存に失敗しました", Snackbar.LENGTH_LONG).show()
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
        mPictureUri = activity?.contentResolver
            ?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.get_image))

        // EXTRA_INITIAL_INTENTSにカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE_SETTING_FRAGMENT)
    }
}

