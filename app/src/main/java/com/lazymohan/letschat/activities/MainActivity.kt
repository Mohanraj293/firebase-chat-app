package com.lazymohan.letschat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.lazymohan.letschat.databinding.ActivityMainBinding
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

  private lateinit var binding:ActivityMainBinding
  private lateinit var preferenceManager: PreferenceManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    preferenceManager = PreferenceManager(applicationContext)
    loadUserDetails()
    getToken()
    setListeners()
  }

  private fun setListeners(){
    binding.imageSignOut.setOnClickListener {
      signOut()
    }
    binding.newChat.setOnClickListener {
      startActivity(Intent(applicationContext,UserActivity::class.java))
    }
  }

  private fun loadUserDetails(){
    binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
    val byte:ByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT)
    val bitmap:Bitmap = BitmapFactory.decodeByteArray(byte,0,byte.size)
    binding.imageProfile.setImageBitmap(bitmap)
  }

  private fun showToast(message:String){
    Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
  }
  private fun updateToken(token:String){
    val database:FirebaseFirestore = FirebaseFirestore.getInstance()
    val documentReference: DocumentReference? =
      preferenceManager.getString(Constants.KEY_USER_ID)?.let {
        database.collection(Constants.KEY_COLLECTION_USERS).document(it)
      }
    documentReference?.update(Constants.KEY_FCM_TOKEN,token)
        ?.addOnFailureListener {
          showToast("Unable to update token")
        }
  }
  private fun getToken(){
    FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
  }

  private fun signOut(){
    showToast("Signing out...")
    val database:FirebaseFirestore = FirebaseFirestore.getInstance()
    val documentReference:DocumentReference? =
      preferenceManager.getString(Constants.KEY_USER_ID)?.let {
        database.collection(Constants.KEY_COLLECTION_USERS).document(it)
      }
    val updates:HashMap<String,Any> = HashMap()
    updates.put(Constants.KEY_FCM_TOKEN,FieldValue.delete())
    documentReference?.update(updates)
        ?.addOnSuccessListener {
          preferenceManager.clear()
          startActivity(Intent(applicationContext,SignInActivity::class.java))
          finish()
        }
        ?.addOnFailureListener {
          showToast("Unable to Sign out")
        }
  }
}