package com.example.firebase

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.firebase.auth.FirebaseAuth
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.GoogleAuthProvider


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //referencia a objeto FirebaseAuth
    private lateinit var buttonRegister: Button
    private lateinit var textViewLogin: TextView
    private lateinit var btnFacebook : com.facebook.login.widget.LoginButton
    private lateinit var btnGoogle : com.google.android.gms.common.SignInButton

    //escuchador de firebaseauth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var googleSighInClient:GoogleSignInClient

    var callbackManager:CallbackManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance() //inicializar
        callbackManager = CallbackManager.Factory.create()

        buttonRegister = findViewById<Button>(R.id.btnRegister)
        buttonRegister.setOnClickListener {
            val email = findViewById<EditText>(R.id.txtEmail).text.toString()
            val password = findViewById<EditText>(R.id.txtPass).text.toString()
            this.register(email, password)
        }
        textViewLogin = findViewById<TextView>(R.id.textViewLogin)
        textViewLogin.setOnClickListener {
            this.goToLogin()
        }
        btnFacebook = findViewById(R.id.login_button)
        btnFacebook.setReadPermissions("email")
        btnFacebook.setOnClickListener{
            signIn()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSighInClient = GoogleSignIn.getClient(this,gso)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnGoogle.setOnClickListener{
            signInGoogle()
        }
         //validamos si existe usuario
        this.checkUser()
    }

    private fun signInGoogle(){
        val  signInIntent = googleSighInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private  val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
                if (result.resultCode == Activity.RESULT_OK){
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResults(task)
                }
    }

    private fun handleResults(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>){
        if(task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if (account!=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                val intent:Intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(){
        btnFacebook.registerCallback(callbackManager,object :FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?){
        val credential:AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
        auth.signInWithCredential(credential).addOnFailureListener{e->
            Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { result ->
            val email:String?= result.user?.email
            Toast.makeText(this,"Te logueaste con el correo:"+email,Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode,resultCode,data)
    }

    override fun onResume() {
        super.onResume()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun register(email:String, password:String){

        auth.createUserWithEmailAndPassword (email, password).addOnCompleteListener { task->
            if (task.isSuccessful) {
                val intent = Intent( this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun checkUser(){
        //verify user
        authStateListener = FirebaseAuth.AuthStateListener { auth->
            if(auth.currentUser != null){
                //cambiando vista
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


}