package jp.travelplantodoimport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jp.travelplantodo.R
import jp.travelplantodo.TimeTable
import kotlinx.android.synthetic.main.list_home.view.*

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
        // レイアウトファイルからidがrootViewのConstraintLayoutオブジェクトを取得し、代入
        val rootView: ConstraintLayout = view.findViewById(R.id.listHome)
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val dateTimeView: TextView = view.findViewById(R.id.dateTimeView)
        val bodyTextHomeView: TextView = view.findViewById(R.id.bodyTextHomeView)

    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HomeViewHolder) {
            // 生成されたViewHolderがApiItemViewHolderだったら。。。
            updateItemViewHolder(holder, position)
        }// {
        // 別のViewHolderをバインドさせることが可能となる
        // }
    }

    private fun updateItemViewHolder(holder: HomeViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        holder.apply {
            rootView.apply {
                // 偶数番目と奇数番目で背景色を変更させる
                setBackgroundColor(
                    ContextCompat.getColor(context,
                    if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray))
            }
            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            dateTimeView.text = data.time
            //タイムテーブルの内容を書き込む
            bodyTextHomeView.text = data.body
        }
    }
}