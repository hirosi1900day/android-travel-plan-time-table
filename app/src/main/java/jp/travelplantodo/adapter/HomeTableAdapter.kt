package jp.travelplantodo.adapter

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.R
import jp.travelplantodo.TimetablePATH
import jp.travelplantodo.TravelPlanIndexPath
import jp.travelplantodo.model.TimeTable
import kotlinx.android.synthetic.main.list_home.*

class HomeTableAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<TimeTable>()

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<TimeTable>) {
        items.apply {
            clear() // items を 空にする
            addAll(list) // itemsにlistを全て追加する
        }
        notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return HomeViewHolder(LayoutInflater.from(context).inflate(R.layout.list_home, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class HomeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val dateTimeView: TextView = view.findViewById(R.id.dateTimeView)
        val bodyTextHomeView: TextView = view.findViewById(R.id.bodyTextHomeView)

    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        Log.d("オブジェクト数","${items.size}")
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HomeViewHolder) {
            // 生成されたViewHolderがApiItemViewHolderだったら。。。
            updateItemViewHolder(holder, position)
            Log.d("postion","${position}")

        }
    }

    private fun updateItemViewHolder(holder: HomeViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        holder.apply {
            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if(data.time != null && data.body != null) {
                dateTimeView.text = data.time
                //タイムテーブルの内容を書き込む
                bodyTextHomeView.text = data.body!!
            }else {
                dateTimeView.text = ""
                bodyTextHomeView.text = ""
            }
        }
    }

    fun tableDelete(position: Int) {
        val data = items[position]
        // ダイアログを表示する
        val builder = AlertDialog.Builder(context)

        builder.setTitle("削除")
        builder.setMessage(data.body + "を削除しますか")

        builder.setPositiveButton("OK"){_, _ ->

            FirebaseFirestore.getInstance().collection(TravelPlanIndexPath).document(data.groupId).collection(
                TimetablePATH).document(data.id).delete()
        }

        builder.setNegativeButton("CANCEL", {_, _ -> null})

        builder.create().show()
    }

}