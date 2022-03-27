package com.lazymohan.letschat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.lazymohan.letschat.databinding.ActivitySignInBinding
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager

class SignInActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySignInBinding
  private lateinit var preferenceManager: PreferenceManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignInBinding.inflate(layoutInflater)
    preferenceManager = PreferenceManager(applicationContext)
    if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
      val intent = Intent(applicationContext,MainActivity::class.java)
      startActivity(intent)
      finish()
    }
    setContentView(binding.root)
    setListeners()
  }

  private fun setListeners() {
    binding.createNewAccount.setOnClickListener {
      val intent = Intent(applicationContext, SignUpActivity::class.java)
      startActivity(intent)
    }
    binding.signInBtn.setOnClickListener {
      if(isValidSignUpDetails()){
        signIn()
      }
    }
  }

  private fun signIn(){
    loading(true)
    val database:FirebaseFirestore = FirebaseFirestore.getInstance()
    database.collection(Constants.KEY_COLLECTION_USERS)
        .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.text.toString())
        .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.text.toString())
        .get()
        .addOnCompleteListener {
          if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0){
            val documentSnapshot:DocumentSnapshot = it.result!!.documents[0]
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
            preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.id)
            documentSnapshot.getString(Constants.KEY_NAME)
                ?.let { it1 -> preferenceManager.putString(Constants.KEY_NAME, it1) }
            documentSnapshot.getString(Constants.KEY_IMAGE)
                ?.let { it2 -> preferenceManager.putString(Constants.KEY_IMAGE, it2) }
            val intent:Intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            }else{
              loading(false)
            showToastMessage("Unable to Sign In")
          }
        }
  }

  private fun loading(isLoading:Boolean){
    if(isLoading){
      binding.signInBtn.visibility = View.INVISIBLE
      binding.progressBar.visibility = View.VISIBLE
    }else{
      binding.signInBtn.visibility = View.VISIBLE
      binding.progressBar.visibility = View.INVISIBLE

    }
  }

  private fun showToastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  private fun isValidSignUpDetails(): Boolean {
    return if (binding.inputEmail.text.toString().trim().isEmpty()) {
      showToastMessage("Enter Email")
      false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
      showToastMessage("Enter valid image")
      false
    } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
      showToastMessage("Enter Password")
      false
    } else true
  }
}
