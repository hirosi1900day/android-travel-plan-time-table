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
import jp.travelplantodo.activity.GiftSendActivity
import jp.travelplantodo.adapter.GiftTableAdapter
import jp.travelplantodo.model.GiftTable
import jp.travelplantodo.model.TimeTable
import kotlinx.android.synthetic.main.fragment_gift.*
import kotlinx.android.synthetic.main.fragment_home.*


const val EXTRA_TRAVEL_PLAN_ID_Gift = "jp.travelplantodo.gift"

class GiftFragment: Fragment(), View.OnClickListener {


    private lateinit var mGiftTableArrayList: ArrayList<GiftTable>
    private val giftTableAdapter by lazy { GiftTableAdapter(requireContext()) }

    var travelPlanId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gift, container, false)
        // fragment_api.xmlが反映されたViewを作成して、returnします
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // RecyclerViewの初期化

        recyclerViewGift.apply {
            adapter = giftTableAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
        }
        mGiftTableArrayList = ArrayList<GiftTable>()
        updateData()

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, ) {
                viewHolder?.let {
                    updateRecyclerView(mGiftTableArrayList)
                    giftTableAdapter.tableDelete(it.layoutPosition)
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

        itemTouchHelper.attachToRecyclerView(recyclerViewGift)

        fabGift.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == fabGift) {
            val intent = Intent(getActivity(), GiftSendActivity::class.java)
            Log.d("確認intent Fragment","${travelPlanId}")
            intent.putExtra(EXTRA_TRAVEL_PLAN_ID_Gift, travelPlanId)
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
            GifttablePATH).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val id = data!!["id"] as String
                        val storeName = data!!["storeName"] as String
                        val giftName = data!!["giftName"] as String
                        val userUid = data!!["uid"] as String
                        val userName = data!!["userName"] as String

                        var groupId = ""
                        if (data!!["groupId"] != null) {
                            groupId = data!!["groupId"] as String
                        }
                        val giftTable = GiftTable(id, storeName, giftName, groupId, userUid, userName)
                        Log.d("オブジェクト確認","${giftTable}")
                        mGiftTableArrayList.add(giftTable)
                    }
                    DocumentChange.Type.MODIFIED -> {

                        var  mGiftTableArrayList2 = ArrayList<GiftTable>()
                        val data = dc.document.data as Map<*, *>?
                        mGiftTableArrayList.forEach{

                            if(it.id != data!!["id"]) {
                                val timeTable =
                                    GiftTable(it.id, it.storeName, it.giftName, it.groupId, it.uid, it.userName)
                                mGiftTableArrayList2.add(timeTable)
                            }
                        }
                        mGiftTableArrayList = mGiftTableArrayList2
                    }

                    DocumentChange.Type.REMOVED -> {

                        var  mGiftTableArrayList2 = ArrayList<GiftTable>()
                        val data = dc.document.data as Map<*, *>?
                        mGiftTableArrayList.forEach{

                            if(it.id != data!!["id"]) {
                                val timeTable =
                                    GiftTable(it.id, it.storeName, it.giftName, it.groupId, it.uid, it.userName)
                                mGiftTableArrayList2.add(timeTable)
                            }
                        }
                        mGiftTableArrayList = mGiftTableArrayList2
                    }
                }
            }
            updateRecyclerView(mGiftTableArrayList)
        }
    }

    private fun updateRecyclerView(list: List<GiftTable>) {
        giftTableAdapter.refresh(list)
    }
}
