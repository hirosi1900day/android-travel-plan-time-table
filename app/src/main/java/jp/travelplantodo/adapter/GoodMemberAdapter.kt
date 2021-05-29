package jp.travelplantodo.adapter

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import jp.travelplantodo.model.GoodFrendMember
import kotlinx.android.synthetic.main.activity_member_send.*


class GoodMemberAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 取得したJsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private var items = mutableListOf<GoodFrendMember>()

    var travelPlanId = ""

    // 表示リスト更新時に呼び出すメソッド
    fun refresh(list: List<GoodFrendMember>) {
        items.apply {
            this.clear() // items を 空にする
            this.addAll(list) // itemsにlistを全て追加する
        }
        notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し戻す
        return GoodMemberViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_good_member, parent, false)
        )
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義
    class GoodMemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがnameTextViewのCTextViewオブジェクトを取得し、代入
        val nameView: TextView = view.findViewById(R.id.nameView)
        val addingMemberButton: Button = view.findViewById(R.id.addingMember)
    }

    override fun getItemCount(): Int {
        // itemsプロパティに格納されている要素数を返す
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GoodMemberViewHolder) {
            this.updateItemViewHolder(holder, position)
        }
    }

    private fun updateItemViewHolder(holder: GoodMemberViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]
        holder.apply {

            // dateTimeViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            if (data!!.userName != null) {
                nameView.text = data!!.userName
            } else {
                nameView.text = ""
            }

            addingMemberButton.setOnClickListener {
                addingMemberButtonAction(
                    data.userUid, data.userName, travelPlanId, addingMemberButton
                )
            }
        }
    }

    private fun addingMemberButtonAction(
        userUid: String,
        userName: String,
        travelPlanId: String,
        view: View)
    {
        val addingPlanMemberData = HashMap<String, String>()

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val name = sp.getString(NameKEY, "")

        addingPlanMemberData["sendUserName"] = name.toString()
        addingPlanMemberData["userUid"] = userUid
        addingPlanMemberData["userName"] = userName
        addingPlanMemberData["travelPlanId"] = travelPlanId



        FirebaseFirestore.getInstance().collection(TemporaryMemberPATH)
            .document(userUid).collection(MemeberTravelPlanSelectPATH)
            .document(userUid + travelPlanId).set(addingPlanMemberData)
            .addOnSuccessListener {

                val builder = AlertDialog.Builder(context)

                builder.setTitle(userName + "を旅行計画に追加しました")

                builder.setPositiveButton("確認しました"){_, _ ->
                }

                val dialog = builder.create()
                dialog.show()
            }

            .addOnFailureListener {

            }

    }
}
