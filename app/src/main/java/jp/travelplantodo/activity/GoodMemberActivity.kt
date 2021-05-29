package jp.travelplantodo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import jp.travelplantodo.adapter.GoodMemberAdapter
import jp.travelplantodo.model.GoodFrendMember
import kotlinx.android.synthetic.main.activity_good_member.*
import kotlinx.android.synthetic.main.activity_member_adding.*

class GoodMemberActivity : AppCompatActivity() {

    private lateinit var mGoodMemberArrayList: ArrayList<GoodFrendMember>
    private val goodMemberAdapter by lazy { GoodMemberAdapter(this) }
    var travelPlanId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_good_member)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerViewGoodMember.apply {
            adapter = goodMemberAdapter
            layoutManager = LinearLayoutManager(applicationContext) // 一列ずつ表示
        }

        mGoodMemberArrayList = ArrayList<GoodFrendMember>()
        updateData()

        travelPlanId = intent.getStringExtra(EXTRA_TRAVEL_PLAN_ID_GoodMember).toString()

        travelPlanIdSend(travelPlanId)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun updateData() {

        var mDatabaseReference = FirebaseFirestore.getInstance()
        val User = FirebaseAuth.getInstance().currentUser
        if(User != null) {
            mDatabaseReference.collection(GoodMemberPATH).document(User.uid).collection(
                GoodMemberInformationPATH
            ).addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val data = dc.document.data as Map<*, *>?
                            val sendUserName = data!!["sendUserName"].toString()
                            val sendUserUid = data!!["sendUserUid"].toString()
                            val userName = data!!["userName"].toString()
                            val userUid = data!!["userUid"].toString()
                            val goodFrendMember =
                                GoodFrendMember(userName, userUid, sendUserName, sendUserUid)
                            mGoodMemberArrayList.add(goodFrendMember)

                        }
                        DocumentChange.Type.MODIFIED -> {

                            var  mGoodMemberArrayList2 = ArrayList<GoodFrendMember>()
                            val data = dc.document.data as Map<*, *>?
                            mGoodMemberArrayList.forEach{

//                                if(it.TravelPlanId != data!!["travelPlanId"]) {
//                                    val AddingMember =
//                                        AddingMember(it.userName, it.userUid, it.sendUserName, it.TravelPlanId)
//                                    mAddingMemberArrayList2.add(AddingMember)
//                                }
                            }
                            mGoodMemberArrayList = mGoodMemberArrayList2
                        }

                        DocumentChange.Type.REMOVED -> {

                            var  mGoodMemberArrayList2 = ArrayList<GoodFrendMember>()
                            val data = dc.document.data as Map<*, *>?
                            mGoodMemberArrayList.forEach{

//                                if(it.TravelPlanId != data!!["travelPlanId"]) {
////                                    val AddingMember =
////                                        AddingMember(it.userName, it.userUid, it.sendUserName, it.TravelPlanId)
////                                    mAddingMemberArrayList2.add(AddingMember)
////                                }
                            }

                            mGoodMemberArrayList = mGoodMemberArrayList2
                        }

                    }
                }
                updateRecyclerView(mGoodMemberArrayList)
            }
        }
    }

    private fun travelPlanIdSend(travelPlanId:String) {
        goodMemberAdapter.travelPlanId = travelPlanId
    }

    private fun updateRecyclerView(list: List<GoodFrendMember>) {
        Log.d("確認メンバー中身","${list.size}")
        goodMemberAdapter.refresh(list)
    }
}