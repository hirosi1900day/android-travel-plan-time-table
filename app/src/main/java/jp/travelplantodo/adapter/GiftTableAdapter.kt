package jp.travelplantodo.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.GifttablePATH
import jp.travelplantodo.model.GiftTable
import jp.travelplantodo.R
import jp.travelplantodo.TimetablePATH
import jp.travelplantodo.TravelPlanIndexPath

class GiftTableAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<GiftTable>()

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<GiftTable>) {
        items.apply {
           clear() // items を 空にする
           addAll(list) // itemsにlistを全て追加する
        }
       notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return GiftViewHolder(LayoutInflater.from(context).inflate(R.layout.list_gift, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class GiftViewHolder(view: View): RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val storeName: TextView = view.findViewById(R.id.storeNameView)
        val giftName: TextView = view.findViewById(R.id.giftNameView)
    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GiftViewHolder) {
            // 生成されたViewHolderがApiItemViewHolderだったら。。。
            updateItemViewHolder(holder, position)
        }
    }

    private fun updateItemViewHolder(holder: GiftViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
            holder.apply {
            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if(data.storeName != null && data.giftName != null) {
                storeName.text = data.storeName
                //タイムテーブルの内容を書き込む
                giftName.text = data.giftName
            }else {
                storeName.text = ""
                giftName.text = ""
            }
        }
    }

    fun tableDelete(position: Int) {
        val data = items[position]


        val builder = AlertDialog.Builder(context)

        builder.setTitle("削除")
        builder.setMessage("削除しますか")

        builder.setPositiveButton("OK"){_, _ ->

            FirebaseFirestore.getInstance().collection(TravelPlanIndexPath).document(data.groupId).collection(
                GifttablePATH
            ).document(data.id).delete()
        }

        builder.setNegativeButton("CANCEL", null)

        builder.create().show()
    }
}