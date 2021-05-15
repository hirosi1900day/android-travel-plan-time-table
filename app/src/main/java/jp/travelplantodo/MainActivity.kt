package jp.travelplantodo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

const val EXTRA_TRAVEL_PLAN_ID = "jp.travelplantodo.TravlPlan"

class MainActivity : AppCompatActivity() {


    private lateinit var mDatabaseReference: FirebaseFirestore
    private lateinit var mTravelPlanArrayList: ArrayList<TravelPlan>
    private lateinit var mAdapter: TravelPlanAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))// ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        // ログインしていなければログイン画面に遷移させる
        if (user == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
        //fabをクリックした時の処理
        fab.setOnClickListener { view ->
            Log.d("ログイン確認","${user!!.uid}")
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, TravelPlanSendActivity::class.java)
                startActivity(intent)
            }
        }

        //firebaseから投稿一覧情報を取得する
        mDatabaseReference = FirebaseFirestore.getInstance()
        mAdapter = TravelPlanAdapter(this)
        mTravelPlanArrayList = ArrayList<TravelPlan>()
        listView.adapter = mAdapter
        mAdapter.setTravelPlanArrayList(mTravelPlanArrayList)

        if (user != null) {getTravelPlanForFirebase(user.uid)}

        //listviewをクリックした際の画面遷移
        listView.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val TravelPlan = parent.adapter.getItem(position) as TravelPlan
            val intent = Intent(this, TravelPlanRoomActivity::class.java)
            intent.putExtra(EXTRA_TRAVEL_PLAN_ID, TravelPlan.travelPlanId)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getTravelPlanForFirebase(uid: String) {

        mDatabaseReference = FirebaseFirestore.getInstance()
        mDatabaseReference.collection(UsersPATH).document(uid).collection(TravelPlanMemberPATH).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val travelPlanId = data!!["travelPlanId"] as String
                        //上記の情報を元に一覧を取得する

                        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).get().addOnSuccessListener { document ->
                            if (document != null) {
                                val data = document.data as Map<*, *>?
                                val title = data!!["title"] as String
                                val body = data!!["body"] as String
                                val name = data!!["name"] as String
                                val travelPlanId = data!!["travelPlanId"] as String
                                val travelPlan = TravelPlan(title ,body,name,travelPlanId)
                                mTravelPlanArrayList.add(travelPlan)

                                mAdapter.notifyDataSetChanged()
                            }
                        }
                            .addOnFailureListener { exception ->
                                Log.d("error", "Error getting documents: ", exception)
                            }
                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
        }

    }

}

