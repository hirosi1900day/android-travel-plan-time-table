package jp.travelplantodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.activity.MemberSendActivity
import jp.travelplantodo.adapter.MemberAdapter
import jp.travelplantodo.model.GiftTable
import jp.travelplantodo.model.Member
import kotlinx.android.synthetic.main.fragment_gift.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_member.*
import kotlinx.android.synthetic.main.list_memeber.*

const val EXTRA_TRAVEL_PLAN_ID_Member = "jp.travelplantodo.model.Member"

class MemberFragment:Fragment(), View.OnClickListener {

    private lateinit var mMemberArrayList: ArrayList<Member>
    private val memberAdapter by lazy { MemberAdapter(requireContext()) }
    lateinit var travelPlanId: String

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

        memberAdapter.travelPlanId = travelPlanId

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
//                    recyclerViewMember.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                   updateRecyclerView(mMemberArrayList)
                    memberAdapter.tableDelete(it.layoutPosition)
                }
            }

            // 1. 行が選択された時に、このコールバックが呼ばれる。ここで行をハイライトする。
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            // 2. 行が選択解除された時 (ドロップされた時) このコールバックが呼ばれる。ハイライトを解除する。
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder?.itemView?.alpha = 1.0f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerViewMember)

        mMemberArrayList = ArrayList<Member>()
        updateData()

        fabMember.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v == fabMember) {
            val intent = Intent(getActivity(), MemberSendActivity::class.java)
            intent.putExtra(EXTRA_TRAVEL_PLAN_ID_Member, travelPlanId)
            startActivity(intent)
        }
    }

    private fun updateData() {
        var mDatabaseReference = FirebaseFirestore.getInstance()

        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            TravelRoomMemberPATH).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED ->
                    {
                        val data = dc.document.data as Map<*, *>?
                        Log.d("確認memberFragment","${data}")
                        val name = data!!["name"] as String
                        val uid = data!!["uid"] as String

                        val TravelRoomMember = Member(name, uid)
                        mMemberArrayList.add(TravelRoomMember)
                        Log.d("確認mMemberArrayList","${mMemberArrayList}")
                    }
                    DocumentChange.Type.MODIFIED ->
                    {
                        var  mMemberArrayList2 = ArrayList<Member>()
                            val data = dc.document.data as Map<*, *>?
                            mMemberArrayList.forEach{

                                if(it.uid != data!!["uid"]) {
                                    val member =
                                        Member(it.name, it.uid)
                                    mMemberArrayList2.add(member)
                                }
                            }
                            mMemberArrayList = mMemberArrayList2
                    }
                    DocumentChange.Type.REMOVED ->
                    {
                        var  mMemberArrayList2 = ArrayList<Member>()
                        val data = dc.document.data as Map<*, *>?
                        mMemberArrayList.forEach{

                            if(it.uid != data!!["uid"]) {
                                val member =
                                    Member(it.name, it.uid)
                                mMemberArrayList2.add(member)
                            }
                        }
                        mMemberArrayList = mMemberArrayList2
                    }
                }
            }
            updateRecyclerView(mMemberArrayList)
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