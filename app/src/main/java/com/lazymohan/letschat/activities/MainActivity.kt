package com.lazymohan.letschat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.lazymohan.letschat.adapters.RecentConversationAdapter
import com.lazymohan.letschat.databinding.ActivityMainBinding
import com.lazymohan.letschat.listeners.ConversionListener
import com.lazymohan.letschat.models.ChatMessage
import com.lazymohan.letschat.models.User
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager

class MainActivity : AppCompatActivity(), ConversionListener {

  private lateinit var binding:ActivityMainBinding
  private lateinit var preferenceManager: PreferenceManager
  private lateinit var conversations:MutableList<ChatMessage>
  private lateinit var adapter:RecentConversationAdapter
  private lateinit var database:FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    preferenceManager = PreferenceManager(applicationContext)
    init()
    loadUserDetails()
    getToken()
    setListeners()
    listenConversation()
  }

  private fun init(){
    conversations = mutableListOf()
    adapter = RecentConversationAdapter(conversations,this)
    binding.conversionRV.adapter = adapter
    database = FirebaseFirestore.getInstance()
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

  private fun listenConversation(){
    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        .addSnapshotListener { value, error ->
          if (error !=null) return@addSnapshotListener
          if (value != null){
            for (dc in value.documentChanges){
              if (dc.type == DocumentChange.Type.ADDED){
                val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                val receiverID = dc.document.getString(Constants.KEY_RECEIVER_ID)
                val chatMessage = ChatMessage()
                chatMessage.senderId = senderId
                chatMessage.receiverId = receiverID
                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                  chatMessage.conversionImage = dc.document.getString(Constants.KEY_RECEIVER_IMAGE)
                  chatMessage.conversionName = dc.document.getString(Constants.KEY_RECEIVER_NAME)
                  chatMessage.conversionId = dc.document.getString(Constants.KEY_RECEIVER_ID)
                }else{
                  chatMessage.conversionImage = dc.document.getString(Constants.KEY_SENDER_IMAGE)
                  chatMessage.conversionName = dc.document.getString(Constants.KEY_SENDER_NAME)
                  chatMessage.conversionId = dc.document.getString(Constants.KEY_SENDER_ID)
                }
                chatMessage.message = dc.document.getString(Constants.KEY_LAST_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                conversations.add(chatMessage)
              }else if (dc.type == DocumentChange.Type.MODIFIED){
                for (i in 0..conversations.size){
                  val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                  val receiverID = dc.document.getString(Constants.KEY_RECEIVER_ID)
                  if (conversations[i].senderId.equals(senderId) && conversations[i].receiverId.equals(receiverID)){
                    conversations[i].message = dc.document.getString(Constants.KEY_LAST_MESSAGE)
                    conversations[i].dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    break
                  }
                }
              }
            }
            conversations.sortWith { a, b -> b.dateObject!!.compareTo(a.dateObject) }
            adapter.notifyDataSetChanged()
            binding.conversionRV.smoothScrollToPosition(0)
            binding.conversionRV.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
          }
        }
    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
        .addSnapshotListener { value, error ->
          if (error !=null) return@addSnapshotListener
          if (value != null){
            for (dc in value.documentChanges){
              if (dc.type == DocumentChange.Type.ADDED){
                val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                val receiverID = dc.document.getString(Constants.KEY_RECEIVER_ID)
                val chatMessage = ChatMessage()
                chatMessage.senderId = senderId
                chatMessage.receiverId = receiverID
                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                  chatMessage.conversionImage = dc.document.getString(Constants.KEY_RECEIVER_IMAGE)
                  chatMessage.conversionName = dc.document.getString(Constants.KEY_RECEIVER_NAME)
                  chatMessage.conversionId = dc.document.getString(Constants.KEY_RECEIVER_ID)
                }else{
                  chatMessage.conversionImage = dc.document.getString(Constants.KEY_SENDER_IMAGE)
                  chatMessage.conversionName = dc.document.getString(Constants.KEY_SENDER_NAME)
                  chatMessage.conversionId = dc.document.getString(Constants.KEY_SENDER_ID)
                }
                chatMessage.message = dc.document.getString(Constants.KEY_LAST_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                conversations.add(chatMessage)
              }else if (dc.type == DocumentChange.Type.MODIFIED){
                for (i in 0..conversations.size){
                  val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                  val receiverID = dc.document.getString(Constants.KEY_RECEIVER_ID)
                  if (conversations[i].senderId.equals(senderId) && conversations[i].receiverId.equals(receiverID)){
                    conversations[i].message = dc.document.getString(Constants.KEY_LAST_MESSAGE)
                    conversations[i].dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    break
                  }
                }
              }
            }
            conversations.sortWith { a, b -> b.dateObject!!.compareTo(a.dateObject) }
            adapter.notifyDataSetChanged()
            binding.conversionRV.smoothScrollToPosition(0)
            binding.conversionRV.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
          }
        }
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
    updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
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

  override fun onConversionClicked(user: User) {
    val intent = Intent(applicationContext,ChatActivity::class.java)
    intent.putExtra(Constants.KEY_USER,user)
    startActivity(intent)
  }
}