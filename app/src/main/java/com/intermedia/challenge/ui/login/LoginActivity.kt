package com.intermedia.challenge.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.intermedia.challenge.ui.main.MainScreenActivity
import com.intermedia.challenge.R
import java.lang.Exception


class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    //constants
    private companion object{
        private const val  RC_SIGN_IN=100
        private const val TAG="GOOGLE_SIGN_IN_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    override fun onResume() {
        super.onResume()
        startFirebaseAuth()
    }

    private fun startFirebaseAuth() {

        //CONFIGURAR EL SIGN IN
        val googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions)

        //init firebase auth
        firebaseAuth= FirebaseAuth.getInstance()
        checkUser()

        //google sign in button, click to begin
        val googleButton: Button =findViewById(R.id.googleButton)
        googleButton.setOnClickListener {
            Log.d(TAG, "onCreate: begin Google Sign in")
            val intent=googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
       /* // TODO provisional
        startActivity(Intent(this, MainScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })*/
    }

    private fun checkUser() {
        val firebaseUser=firebaseAuth.currentUser
        if(firebaseUser!=null){
            startActivity(Intent(this@LoginActivity,MainScreenActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask=GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account=accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }catch (e: Exception){
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential= GoogleAuthProvider.getCredential(account!!.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult->
                //login success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")
                //get logged in user
                val firebaseUser= firebaseAuth.currentUser
                //get user info
                val uid=firebaseUser!!.uid
                val email=firebaseUser!!.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email $email")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid $uid")

                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...\n$email")
                    Toast.makeText(this@LoginActivity, "Account created...\n$email", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user")
                    Toast.makeText(this@LoginActivity, "LoggedIn...\n$email", Toast.LENGTH_SHORT).show()
                }

                //start profile activity
                startActivity(Intent(this@LoginActivity,MainScreenActivity::class.java))
                finish()

            }
            .addOnFailureListener{e->
                //login failed
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Loggin failed due to ${e.message}")
                Toast.makeText(this@LoginActivity, "Loggin failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}