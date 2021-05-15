package jp.travelplantodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodoimport.GiftTableAdapter
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
                        val storeName = data!!["storeName"] as String
                        val giftName = data!!["giftName"] as String
                        val userUid = data!!["uid"] as String
                        val userName = data!!["userName"] as String

                        var groupId = ""
                        if (data!!["groupId"] != null) {
                            groupId = data!!["groupId"] as String
                        }
                        val giftTable = GiftTable(storeName, giftName, userUid, userName, groupId)
                        Log.d("オブジェクト確認","${giftTable}")
                        mGiftTableArrayList.add(giftTable)
                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
                updateRecyclerView(mGiftTableArrayList)
        }
    }

    private fun updateRecyclerView(list: List<GiftTable>) {
        giftTableAdapter.refresh(list)
    }


}
