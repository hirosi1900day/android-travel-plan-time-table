package jp.travelplantodo.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import jp.travelplantodo.*
import jp.travelplantodo.model.AddingMember


class AddingMemberAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private var items = mutableListOf<AddingMember>()

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<AddingMember>) {
        items.apply {
            this.clear() // items を 空にする
            this.addAll(list) // itemsにlistを全て追加する
        }
        notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return AddingMemberViewHolder(LayoutInflater.from(context).inflate(R.layout.list_member_adding, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class AddingMemberViewHolder(view: View): RecyclerView.ViewHolder(view){
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val nameView: TextView = view.findViewById(R.id.nameView)
        val titleView: TextView = view.findViewById(R.id.titleView)
        val addingMemberButton: Button = view.findViewById(R.id.addingMember)


    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddingMemberViewHolder) {
            this.updateItemViewHolder(holder, position)
        }
    }

    private fun updateItemViewHolder(holder: AddingMemberViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        holder.apply {

            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if(data!!.sendUserName != null) {
                nameView.text = data!!.sendUserName
            }else {
                nameView.text = ""
            }

            if(data!!.TravelPlanId != null) {
                FirebaseFirestore.getInstance().collection(TravelPlanIndexPath).document(data.TravelPlanId).get()
                    .addOnSuccessListener {
                    document ->
                    val data = document.data as Map<*, *>?
                    if(data!!["title"] != null){
                        titleView.text = data["title"].toString()
                    }else {
                        titleView.text = ""
                    }
                }
            }else {
                titleView.text = ""
            }
            addingMemberButton.setOnClickListener{addingMemberButtonAction(data.userUid, data.TravelPlanId, data.userName, addingMemberButton)
            }
        }
    }

    private fun addingMemberButtonAction(userUid: String, travelPlanId: String, userName: String, view:View) {
        val memberData = HashMap<String,String>()
        memberData["name"] = userName
        memberData["uid"] = userUid
        memberData["travelPlanId"] = travelPlanId

        val mDatabase = FirebaseFirestore.getInstance()

        mDatabase.collection(UsersPATH).document(userUid).collection(TravelPlanMemberPATH).document(travelPlanId).set(memberData)
            .addOnSuccessListener {

                mDatabase.collection(TravelPlanIndexPath).document(travelPlanId).collection(
                    TravelRoomMemberPATH
                ).document(userUid).set(memberData).addOnSuccessListener {

                    mDatabase.collection(TemporaryMemberPATH).document(userUid).collection(
                        MemeberTravelPlanSelectPATH
                    ).document(userUid + travelPlanId)
                        .delete()

                    val builder = AlertDialog.Builder(context)

                    builder.setTitle(userName + "を旅行計画に追加しました")

                    builder.setPositiveButton("確認しました") { _, _ ->

                    }

                    val dialog = builder.create()
                    dialog.show()
                }
            }
    }
}