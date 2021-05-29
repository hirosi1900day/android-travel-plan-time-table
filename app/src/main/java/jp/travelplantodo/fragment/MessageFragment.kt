package jp.travelplantodo.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import jp.travelplantodo.adapter.MessageAdapter
import jp.travelplantodo.model.MessageTable
import kotlinx.android.synthetic.main.fragment_message.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MessageFragment: Fragment(), View.OnClickListener {

    private lateinit var mMessageArrayList: ArrayList<MessageTable>
    private val messageAdapter by lazy { MessageAdapter(requireContext()) }

    var travelPlanId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerViewMessage.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
        }
        mMessageArrayList = ArrayList<MessageTable>()
        messageSendButton.setOnClickListener(this)
        updateData()

    }

    private fun updateData() {

        var mDatabaseReference = FirebaseFirestore.getInstance()

        if(travelPlanId == "") return
        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            MessagePATH
        ).addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = dc.document.data as Map<*, *>?
                        val message = data!!["message"] as String
                        val userName = data!!["userName"] as String
                        val userUid = data!!["userUid"] as String
                        val createdAt = data!!["createdAt"] as String
                        var groupId = ""
                        if (data!!["groupId"] != null) {
                            groupId = data!!["groupId"] as String
                        }
                        val messageTable = MessageTable(message, groupId, userUid, userName, createdAt)
                        Log.d("オブジェクト確認","${messageTable}")
                        mMessageArrayList.add(messageTable)
                        mMessageArrayList.sortBy { it.createdAt }

                    }
                    DocumentChange.Type.MODIFIED -> Log.d("確認", "Modified city: ${dc.document.data}")

                    DocumentChange.Type.REMOVED -> Log.d("確認", "Removed city: ${dc.document.data}")
                }
            }
            updateRecyclerView(mMessageArrayList)
        }
    }

    private fun massageSave(v: View) {

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val userName = sp.getString(NameKEY, "").toString()

        var userUid = FirebaseAuth.getInstance().currentUser!!.uid
        if (userUid == null) {
            userUid = ""
        }

        val message = messageSendText.text.toString()

        if (message.length ==0) {
            return
        }
        messageSendText.text.clear()

        val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())


        val messageData = HashMap<String, String>()
        messageData["message"] = message
        messageData["userUid"] = userUid
        messageData["userName"] = userName
        messageData["createdAt"] = formatted

        if (travelPlanId != "") {
            messageData["groupId"] = travelPlanId
        }else {
            messageData["groupId"] = ""
        }

        val mDatabaseReference = FirebaseFirestore.getInstance()

        mDatabaseReference.collection(TravelPlanIndexPath).document(travelPlanId).collection(
            MessagePATH
        ).document().set(messageData)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                Snackbar.make(v, getString(R.string.message_send_error_message), Snackbar.LENGTH_LONG).show()
            }
    }

    fun getPlanId(travelPlanIdFromFragmentStateAdapter: String) {
        travelPlanId = travelPlanIdFromFragmentStateAdapter
    }

    private fun updateRecyclerView(list: List<MessageTable>) {
        messageAdapter.refresh(list)
    }

    override fun onClick(v: View?) {
        if (v == messageSendButton) {
            v?.let { massageSave(it) }
        }
        // キーボードが出てたら閉じる
        val im = getActivity()?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v!!.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

    }
}