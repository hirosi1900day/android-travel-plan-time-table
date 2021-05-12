package jp.travelplantodo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class MemberAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<Member>()

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<Member>) {
        this.items.apply {
            this.clear() // items を 空にする
            this.addAll(list) // itemsにlistを全て追加する
        }
        this.notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return MemberViewHolder(LayoutInflater.from(context).inflate(R.layout.list_home, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class MemberViewHolder(view: View): RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがrootViewのConstraintLayoutオブジェクトを取得し、代入
        val rootView: ConstraintLayout = view.findViewById(R.id.listMember)
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val nameView: TextView = view.findViewById(R.id.nameView)


    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return this.items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MemberViewHolder) {
            this.updateItemViewHolder(holder, position)
        }// {
        // 別のViewHolderをバインドさせることが可能となる
        // }
    }

    private fun updateItemViewHolder(holder: MemberViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        holder.apply {

            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if(data!!.name != null) {
                nameView.text = data!!.name
            }else {
                this.nameView.text = ""
            }
        }
    }

}