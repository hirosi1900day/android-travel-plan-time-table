package jp.travelplantodo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.activity.TimetableSendActivity
import jp.travelplantodo.adapter.HomeTableAdapter
import jp.travelplantodo.model.TimeTable
import kotlinx.android.synthetic.main.fragment_gift.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.list_home.*


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

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, ) {
                viewHolder?.let {
                    updateRecyclerView(mTimeTableArrayList)
                    homeTableAdapter.tableDelete(it.layoutPosition)
                }
            }

            // 1. 行が選択された時に、このコールバックが呼ばれる。ここで行をハイライトする。
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            // 2. 行が選択解除された時 (ドロップされた時) このコールバックが呼ばれる。ハイライトを解除する。
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder?.itemView?.alpha = 1.0f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerViewHome)

        mTimeTableArrayList = ArrayList<TimeTable>()
        updateData()

        fabHome.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == fabHome) {
            val intent = Intent(getActivity(), TimetableSendActivity::class.java)
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
            TimetablePATH).orderBy("timeDateNumber").addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val id = data!!["id"] as String
                        val date = data!!["dateTime"] as String
                        val body = data!!["content"] as String

                        var groupId = ""
                        if (data!!["groupId"] != null) {
                            groupId = data!!["groupId"] as String
                        }
                        val itemDataNumber = data["timeDateNumber"] as String
                        val timeTable = TimeTable(id, date, body, groupId, itemDataNumber)
                        Log.d("オブジェクト確認","${timeTable}")
                        mTimeTableArrayList.add(timeTable)
                        mTimeTableArrayList.sortBy { it.timeDataNumber }
                    }
                    DocumentChange.Type.MODIFIED ->{

                        var  mTimeTableArrayList2 = ArrayList<TimeTable>()
                        val data = dc.document.data as Map<*, *>?
                        mTimeTableArrayList.forEach{

                            if(it.id != data!!["id"]) {
                                val timeTable =
                                    TimeTable(it.id, it.time, it.body, it.groupId, it.timeDataNumber)
                                mTimeTableArrayList2.add(timeTable)
                            }
                        }
                        mTimeTableArrayList = mTimeTableArrayList2

                    }

                    DocumentChange.Type.REMOVED ->{

                        var  mTimeTableArrayList2 = ArrayList<TimeTable>()
                        val data = dc.document.data as Map<*, *>?
                        mTimeTableArrayList.forEach{

                            if(it.id != data!!["id"]) {
                                val timeTable =
                                    TimeTable(it.id, it.time, it.body, it.groupId, it.timeDataNumber)
                                mTimeTableArrayList2.add(timeTable)
                            }
                        }
                        mTimeTableArrayList = mTimeTableArrayList2
                    }
                }
            }
            updateRecyclerView(mTimeTableArrayList)
        }
    }

    private fun updateRecyclerView(list: List<TimeTable>) {
        homeTableAdapter.refresh(list)
    }
}

