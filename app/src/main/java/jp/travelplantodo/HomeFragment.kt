package jp.travelplantodo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodoimport.HomeTableAdapter
import kotlinx.android.synthetic.main.fragment_home.*


const val EXTRA_TRAVEL_PLAN_ID_Tiem_Table = "jp.travelplantodo.timeTableTravelPlanId"

class HomeFragment: Fragment(), View.OnClickListener{


    private lateinit var mTimeTableArrayList: ArrayList<TimeTable>
    private val homeTableAdapter by lazy { HomeTableAdapter(requireContext()) }

    var travelPlanId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
        // fragment_api.xmlが反映されたViewを作成して、returnします
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
        if (v == fabHome) {
            val intent = Intent(getActivity(), TimetableSendActivity::class.java)
            Log.d("確認intent Fragment","${travelPlanId}")
            intent.putExtra(EXTRA_TRAVEL_PLAN_ID_Tiem_Table, travelPlanId)
            startActivity(intent)
        }
    }


    //fragmentAdapterよりtravelPlanIdを取得する
   fun getPlanId(travelPlanIdFromFragmentStatePagerAdapter: String) {
       travelPlanId = travelPlanIdFromFragmentStatePagerAdapter
   }

    private fun updateData() {

        var mDatabaseReference = FirebaseFirestore.getInstance()

        if(travelPlanId == "") return
        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            TimetablePATH).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val date = data!!["dateTime"] as String
                        val body = data!!["content"] as String

                        var groupId = ""
                        if (data!!["groupId"] != null) {
                            groupId = data!!["groupId"] as String
                        }
                           val timeTable = TimeTable(date, body, groupId)
                           Log.d("オブジェクト確認","${timeTable}")
                           mTimeTableArrayList.add(timeTable)
                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
                updateRecyclerView(mTimeTableArrayList)
        }
    }

    private fun updateRecyclerView(list: List<TimeTable>) {
        homeTableAdapter.refresh(list)
    }


}