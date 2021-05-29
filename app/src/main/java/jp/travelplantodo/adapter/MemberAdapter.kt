package jp.travelplantodo.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import jp.travelplantodo.model.Member


class MemberAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<Member>()

    var travelPlanId = ""

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
        return MemberViewHolder(LayoutInflater.from(context).inflate(R.layout.list_memeber, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class MemberViewHolder(view: View): RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val nameView: TextView = view.findViewById(R.id.nameView)


    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
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

    fun tableDelete(position: Int) {
        val data = items[position]

        Log.d("確認Datauid", "${data.uid}")
        Log.d("確認FirebaseUid","${FirebaseAuth.getInstance().currentUser?.uid}")
        Log.d("確認travelPlanIdAdapter","${travelPlanId}")


        if(data.uid != FirebaseAuth.getInstance().currentUser?.uid) {

            val builder = AlertDialog.Builder(context)

            builder.setTitle("削除")
            builder.setMessage("削除しますか")

            builder.setPositiveButton("OK") { _, _ ->


                FirebaseFirestore.getInstance().collection(TravelPlanIndexPath)
                    .document(travelPlanId).collection(
                        TravelRoomMemberPATH
                    ).document(data.uid).delete()

                FirebaseFirestore.getInstance().collection(UsersPATH).document(data.uid)
                    .collection(
                        TravelPlanMemberPATH
                    ).document(travelPlanId).delete()
            }


            builder.setNegativeButton("CANCEL", null)

            builder.create().show()
        }
    }
}