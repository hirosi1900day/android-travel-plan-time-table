package jp.travelplantodo

import android.app.ActionBar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodoimport.HomeTableAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment: Fragment(), View.OnClickListener {


    private lateinit var mTimeTableArrayList: ArrayList<TimeTable>
    private val homeTableAdapter by lazy { HomeTableAdapter(requireContext()) }
    private val travelPlanId = ""

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_home, container, false)
        // fragment_api.xmlが反映されたViewを作成して、returnします
        if (getArguments() != null) {
            travelPlanId= getArguments()?.getString(TravelPlanId).toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // RecyclerViewの初期化
        recyclerViewHome.apply {
            adapter = homeTableAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
        }
        mTimeTableArrayList = ArrayList<TimeTable>()
        updateData()

        fabHome.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (view == fabHome) {

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        // フラグメントマネージャーの取得
        val manager: FragmentManager? = fragmentManager // アクティビティではgetSupportFragmentManager()?
        // フラグメントトランザクションの開始
        val transaction: FragmentTransaction = manager!!.beginTransaction()
        // レイアウトをfragmentに置き換え（追加）
        transaction.replace(R.id.homeFragment, fragment)
        // 置き換えのトランザクションをバックスタックに保存する
        transaction.addToBackStack(null)
        // フラグメントトランザクションをコミット
        transaction.commit()
    }


    private fun updateData() {

        var mDatabaseReference = FirebaseFirestore.getInstance()
        mDatabaseReference.collection(travelPlanId).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val date = data!!["date"] as String
                        val body = data!!["body"] as String
                        val groupId = data!!["groupId"] as String
                        val timeTable = TimeTable(date, body, groupId)
                        mTimeTableArrayList.add(timeTable)
                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
            handler.post {
                updateRecyclerView(mTimeTableArrayList)
            }
        }
    }

    private fun updateRecyclerView(list: List<TimeTable>) {
        homeTableAdapter.refresh(list)
    }

    companion object {
        private const val COUNT = 20 // 1回のAPIで取得する件数
    }


}