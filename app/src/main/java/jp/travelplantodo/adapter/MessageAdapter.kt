package jp.travelplantodo.adapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.model.MessageTable
import jp.travelplantodo.R
import jp.travelplantodo.UsersPATH
import kotlinx.android.synthetic.main.activity_setting.*


class MessageAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<MessageTable>()

    val uid = FirebaseAuth.getInstance().currentUser!!.uid

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<MessageTable>) {
        this.items.apply {
            this.clear() // items を 空にする
            this.addAll(list) // itemsにlistを全て追加する
        }
        this.notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return when(viewType) {
            // ViewTypeがVIEW_TYPE_EMPTY（つまり、お気に入り登録が0件）の場合
            VIEW_TYPE_PARTNER -> MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.list_message, parent, false))
            // 上記以外（つまり、1件以上のお気に入りが登録されている場合
            else -> MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.list_mymessage, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].userUid == uid) VIEW_TYPE_MYMESSAGE else VIEW_TYPE_PARTNER
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class MessageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val userImageView: ImageView = view.findViewById(R.id.userImageView)
        val nameView: TextView = view.findViewById(R.id.nameView)
        val messageview: TextView = view.findViewById(R.id.messageView)
    }


    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageViewHolder) {
            this.updateItemViewHolder(holder, position)
        }
    }

    private fun updateItemViewHolder(holder: MessageViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        var imageString = ""
            FirebaseFirestore.getInstance().collection(UsersPATH).document(data.userUid).get()
                .addOnSuccessListener { document ->
                    Log.d("確認document", "${document}")
                    if (document != null) {
                        val data = document.data as Map<*, *>?
                        if (data!!["image"] != null) {
                            imageString = data!!["image"] as String
                            if (imageString != "") {
                                val bytes = Base64.decode(imageString, Base64.DEFAULT)
                                val image =
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                                        .copy(Bitmap.Config.ARGB_8888, true)
                                holder.apply {
                                    userImageView.setImageBitmap(image)
                                }
                                Log.d("確認imageStrinf", "${imageString}")
                            }
                        }
                    }
                }
        Log.d("確認uid","${data.userUid}")

        holder.apply {

            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if(data!!.userName != null) {
                nameView.text = data!!.userName
            }else {
                this.nameView.text = ""
            }

            if(data!!.message != null) {
                messageview.text = data!!.message
            }else {
                messageview.text = ""
            }
        }
    }

    companion object {
        // 相手のメッセージ場合
        private const val VIEW_TYPE_PARTNER = 0
        // 自分のメッセージの場合
        private const val VIEW_TYPE_MYMESSAGE = 1
    }

}