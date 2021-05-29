package jp.travelplantodo.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.MemeberTravelPlanSelectPATH
import jp.travelplantodo.R
import jp.travelplantodo.TemporaryMemberPATH
import jp.travelplantodo.adapter.AddingMemberAdapter
import jp.travelplantodo.model.AddingMember
import kotlinx.android.synthetic.main.activity_member_adding.*

class MemberAddingActivity : AppCompatActivity() {

    private lateinit var mAddingMemberArrayList: ArrayList<AddingMember>
    private val addingMemberAdapter by lazy { AddingMemberAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_adding)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerViewAddingMember.apply {
            adapter = addingMemberAdapter
            layoutManager = LinearLayoutManager(applicationContext) // 一列ずつ表示
        }

        mAddingMemberArrayList = ArrayList<AddingMember>()
        updateData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun updateData() {

        var mDatabaseReference = FirebaseFirestore.getInstance()
        val User = FirebaseAuth.getInstance().currentUser
        if(User != null) {
            mDatabaseReference.collection(TemporaryMemberPATH).document(User.uid).collection(
                MemeberTravelPlanSelectPATH).addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val data = dc.document.data as Map<*, *>?
                            val sendUserName = data!!["sendUserName"].toString()
                            val userName = data!!["userName"].toString()
                            val userUid = data!!["userUid"].toString()
                            val travelPlanId = data!!["travelPlanId"].toString()
                            val AddingMember =
                                AddingMember(userName, userUid, sendUserName, travelPlanId)
                            mAddingMemberArrayList.add(AddingMember)

                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(
                                "確認",
                                "Modified city: ${dc.document.data}"
                            )

                            var  mAddingMemberArrayList2 = ArrayList<AddingMember>()
                            val data = dc.document.data as Map<*, *>?
                            mAddingMemberArrayList.forEach{

                                if(it.TravelPlanId != data!!["travelPlanId"]) {
                                    val AddingMember =
                                        AddingMember(it.userName, it.userUid, it.sendUserName, it.TravelPlanId)
                                    mAddingMemberArrayList2.add(AddingMember)
                                }
                            }
                            mAddingMemberArrayList = mAddingMemberArrayList2
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d(
                                "確認",
                                "Removed city: ${dc.document.data}"
                            )

                            var  mAddingMemberArrayList2 = ArrayList<AddingMember>()
                            val data = dc.document.data as Map<*, *>?
                            mAddingMemberArrayList.forEach{

                                if(it.TravelPlanId != data!!["travelPlanId"]) {
                                    val AddingMember =
                                        AddingMember(it.userName, it.userUid, it.sendUserName, it.TravelPlanId)
                                    mAddingMemberArrayList2.add(AddingMember)
                                }
                            }
                            mAddingMemberArrayList = mAddingMemberArrayList2
                            Log.d("確認AddingArrayList","${mAddingMemberArrayList}")
                        }

                    }
                }
                updateRecyclerView(mAddingMemberArrayList)
            }
        }
    }


    private fun updateRecyclerView(list: List<AddingMember>) {
        Log.d("確認メンバー中身","${list.size}")
        addingMemberAdapter.refresh(list)
    }

}