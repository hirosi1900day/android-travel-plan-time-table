package jp.travelplantodo.activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.travelplantodo.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference:FirebaseFirestore

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()
        mDataBaseReference = FirebaseFirestore.getInstance()


        // アカウント作成処理のリスナー
        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                // ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {

                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.create_account_failure_message), Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.collection(UsersPATH).document(user!!.uid)

                val myId = randomString(10)

                val myIdRef = mDataBaseReference.collection(IdPATH).document(myId)

                if (mIsCreateAccount) {
                    // アカウント作成の時は表示名をFirebaseに保存する
                    val name = nameText.text.toString()

                    val dataMyId = HashMap<String,String>()
                    dataMyId["userUid"] = user!!.uid
                    dataMyId["userName"] = name

                    val data = HashMap<String, String>()
                    data["name"] = name

                    val userMyId = HashMap<String, String>()
                    userMyId["myId"] = myId

                    myIdRef.set(dataMyId)
                    userRef.set(data)
                    userRef.collection(IdPATH).document(user!!.uid).set(userMyId)

                    // 表示名をPreferenceに保存する
                    saveKeyValue(name, NameKEY)
                    saveKeyValue(myId, MyIdKEY)

                } else {
                    userRef.get().addOnSuccessListener { document ->
                        val data = document.data as Map<*, *>?
                        if (document != null) {
                            saveKeyValue(data!!["name"] as String, NameKEY)
                            Log.d("確認", "DocumentSnaphot data: ${document.data}")
                        } else {
                            Log.d("確認", "No such document")
                        }
                    }

                    userRef.collection(IdPATH).document(user!!.uid).get().addOnSuccessListener { document ->
                        val data = document.data as Map<*, *>?
                        if(document != null) {
                            saveKeyValue(data!!["myId"] as String, MyIdKEY)
                        }else {
                            Log.d("確認", "No such document")
                        }
                    }
                }

                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                // Activityを閉じる
                finish()

            } else {
                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.login_failure_message), Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                // フラグを落としておく
                mIsCreateAccount = false

                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG).show()
            }
        }

        createButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if (email.length != 0 && password.length >= 6 && name.length != 0) {
                // ログイン時に表示名を保存するようにフラグを立てる
                mIsCreateAccount = true

                createAccount(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG).show()
            }

            loginButton.setOnClickListener { v ->
                // キーボードが出てたら閉じる
                val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

                val email = emailText.text.toString()
                val password = passwordText.text.toString()

                if (email.length != 0 && password.length >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false

                    login(email, password)
                } else {
                    // エラーを表示する
                    Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    //アカウント作成
    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    //ログイン
    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveKeyValue(name: String,key: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(key, name)
        editor.commit()
    }


    private fun randomString(StrLength: Int): String {
        val source = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        val len: Int = source.length

        var num = 0 //事前にvar num の定義が残っている場合は、エラーがでるので、var を削除しましょう。

        var randomString = ""
        while (num < StrLength) {
            val rand = (1..len-1).random()
            var nextChar = source.substring(rand-1, rand)
            randomString += nextChar
            num++
        }
        return randomString
    }




}