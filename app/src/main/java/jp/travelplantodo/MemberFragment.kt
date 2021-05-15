package jp.travelplantodo

import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_member.*
import kotlinx.android.synthetic.main.list_memeber.*

class MemberFragment:Fragment() {

    private lateinit var mMemberArrayList: ArrayList<Member>
    private val memberAdapter by lazy { MemberAdapter(requireContext()) }
    lateinit var travelPlanId: String
    private val handler = Handler(Looper.getMainLooper())



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewMember.apply {
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
        }

        mMemberArrayList = ArrayList<Member>()
        updateData()
//        Log.d("確認travelPlanId","${travelPlanId}")
    }

    private fun updateData() {
        var mDatabaseReference = FirebaseFirestore.getInstance()
//        if(travelPlanId == "") {
//            return}

        Log.d("確認","${travelPlanId}")
        Log.d("確認","確認")

        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            TravelRoomMemberPATH).addSnapshotListener { snapshots, e ->
            if (e != null) {

                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        Log.d("確認memberFragment","${data}")
                        val name = data!!["name"] as String
                        val uid = data!!["uid"] as String

                        val TravelRoomMember = Member(name, uid)
                        mMemberArrayList.add(TravelRoomMember)
                        Log.d("確認mMemberArrayList","${mMemberArrayList}")
                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
            handler.post {
                updateRecyclerView(mMemberArrayList)
            }
        }
    }

    private fun updateRecyclerView(list: List<Member>) {
        memberAdapter.refresh(list)
    }

    //fragmentAdapterよりtravelPlanIdを取得する
    fun getPlanId(travelPlanIdFromFragmentStatePagerAdapter: String) {
        travelPlanId = travelPlanIdFromFragmentStatePagerAdapter
    }


}