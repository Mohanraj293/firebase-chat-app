package com.lazymohan.letschat.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.lazymohan.letschat.adapters.ChatAdapter
import com.lazymohan.letschat.databinding.ActivityChatBinding
import com.lazymohan.letschat.models.ChatMessage
import com.lazymohan.letschat.models.User
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
  private lateinit var binding: ActivityChatBinding
  private lateinit var receivedUser: User
  private lateinit var chatMessages: MutableList<ChatMessage>
  private lateinit var chatAdapter: ChatAdapter
  private lateinit var preferenceManager: PreferenceManager
  private lateinit var database: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityChatBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setListeners()
    loadReceivedDetails()
    init()
    listenMessage()
  }

  private fun init() {
    preferenceManager = PreferenceManager(applicationContext)
    chatMessages = mutableListOf()
    chatAdapter =
      preferenceManager.getString(Constants.KEY_USER_ID)?.let {
        receivedUser.image?.let { it1 -> getBitmapFromEncodedString(it1) }?.let { it2 ->
          ChatAdapter(
              chatMessages,
              it2,
              it
          )
        }
      }!!
    binding.chatRecyclerView.adapter = chatAdapter
    database = FirebaseFirestore.getInstance()
  }

  private fun sendMessage() {
    val message = hashMapOf<String, Any>()
    preferenceManager.getString(Constants.KEY_USER_ID)
        ?.let { message.put(Constants.KEY_SENDER_ID, it) }
    receivedUser.id?.let { message.put(Constants.KEY_RECEIVED_ID, it) }
    message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
    message[Constants.KEY_TIMESTAMP] = Date()
    database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
    binding.inputMessage.text = null
  }

  private fun listenMessage() {
    database.collection(Constants.KEY_COLLECTION_CHAT)
        .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        .whereEqualTo(Constants.KEY_RECEIVED_ID, receivedUser.id)
        .addSnapshotListener { value, error ->
          if (error != null) return@addSnapshotListener
          if (value != null) {
            val count = chatMessages.size
            for (dc in value.documentChanges) {
              if (dc.type == DocumentChange.Type.ADDED) {
                val chatMessage = ChatMessage()
                chatMessage.senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                chatMessage.receiverId = dc.document.getString(Constants.KEY_RECEIVED_ID)
                chatMessage.message = dc.document.getString(Constants.KEY_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                chatMessage.dateTime = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    ?.let { getReadableDateTime(it) }
                chatMessages.add(chatMessage)
              }
            }
            if(count==0) chatAdapter.notifyDataSetChanged()
            else {
              chatAdapter.notifyItemRangeInserted(chatMessages.size,chatMessages.size)
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size -1)
            }
          }
          binding.progressBar.visibility = View.GONE
        }
    database.collection(Constants.KEY_COLLECTION_CHAT)
        .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
        .whereEqualTo(Constants.KEY_RECEIVED_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        .addSnapshotListener { value, error ->
          if (error != null) return@addSnapshotListener
          if (value != null) {
            val count = chatMessages.size
            for (dc in value.documentChanges) {
              if (dc.type == DocumentChange.Type.ADDED) {
                val chatMessage = ChatMessage()
                chatMessage.senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                chatMessage.receiverId = dc.document.getString(Constants.KEY_RECEIVED_ID)
                chatMessage.message = dc.document.getString(Constants.KEY_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                chatMessage.dateTime = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    ?.let { getReadableDateTime(it) }
                chatMessages.add(chatMessage)
              }
            }
            if(count==0) chatAdapter.notifyDataSetChanged()
            else {
              chatAdapter.notifyItemRangeInserted(chatMessages.size,chatMessages.size)
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size -1)
            }
          }
          binding.progressBar.visibility = View.GONE
        }
  }

  private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
    val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }

  private fun loadReceivedDetails() {
    receivedUser = intent.getSerializableExtra(Constants.KEY_USER) as User
    binding.textName.text = receivedUser.name
  }

  private fun setListeners() {
    binding.imageBack.setOnClickListener {
      onBackPressed()
    }
    binding.layoutSend.setOnClickListener {
      sendMessage()
    }
  }

  private fun getReadableDateTime(date: Date): String {
    return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
  }
}