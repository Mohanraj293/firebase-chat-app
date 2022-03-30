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
  private var conversionId: String? = null

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
    receivedUser.id?.let { message.put(Constants.KEY_RECEIVER_ID, it) }
    message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
    message[Constants.KEY_TIMESTAMP] = Date()
    database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
    if (conversionId != null) {
      updateConversion(binding.inputMessage.text.toString())
    } else {
      val conversion = mutableMapOf<String, Any>()
      preferenceManager.getString(Constants.KEY_USER_ID)
          ?.let { conversion.put(Constants.KEY_SENDER_ID, it) }
      preferenceManager.getString(Constants.KEY_NAME)
          ?.let { conversion.put(Constants.KEY_SENDER_NAME, it) }
      preferenceManager.getString(Constants.KEY_IMAGE)
          ?.let { conversion.put(Constants.KEY_SENDER_IMAGE, it) }
      receivedUser.id?.let {
        conversion.put(Constants.KEY_RECEIVER_ID, it)
      }
      receivedUser.name?.let {
        conversion.put(Constants.KEY_RECEIVER_NAME, it)
      }
      receivedUser.image?.let {
        conversion.put(Constants.KEY_RECEIVER_IMAGE, it)
      }
      binding.inputMessage.text.toString().let {
        conversion.put(Constants.KEY_LAST_MESSAGE, it)
      }
      Date().let {
        conversion.put(Constants.KEY_TIMESTAMP, it)
      }
      addConversion(conversion as HashMap<String, Any>)
    }
    binding.inputMessage.text = null
  }

  private fun listenMessage() {
    database.collection(Constants.KEY_COLLECTION_CHAT)
        .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
        .addSnapshotListener { value, error ->
          if (error != null) return@addSnapshotListener
          if (value != null) {
            val count = chatMessages.size
            for (dc in value.documentChanges) {
              if (dc.type == DocumentChange.Type.ADDED) {
                val chatMessage = ChatMessage()
                chatMessage.senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                chatMessage.receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)
                chatMessage.message = dc.document.getString(Constants.KEY_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                chatMessage.dateTime = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    ?.let { getReadableDateTime(it) }
                chatMessages.add(chatMessage)
              }
            }
            chatMessages.sortWith { a, b -> a.dateObject!!.compareTo(b.dateObject) }
            if (count == 0) chatAdapter.notifyDataSetChanged()
            else {
              chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
          }
          binding.progressBar.visibility = View.GONE
          if (conversionId == null) {
            checkForConversion()
          }
        }
    database.collection(Constants.KEY_COLLECTION_CHAT)
        .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
        .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        .addSnapshotListener { value, error ->
          if (error != null) return@addSnapshotListener
          if (value != null) {
            val count = chatMessages.size
            for (dc in value.documentChanges) {
              if (dc.type == DocumentChange.Type.ADDED) {
                val chatMessage = ChatMessage()
                chatMessage.senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                chatMessage.receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)
                chatMessage.message = dc.document.getString(Constants.KEY_MESSAGE)
                chatMessage.dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)
                chatMessage.dateTime = dc.document.getDate(Constants.KEY_TIMESTAMP)
                    ?.let { getReadableDateTime(it) }
                chatMessages.add(chatMessage)
              }
            }
            chatMessages.sortWith { a, b -> a.dateObject!!.compareTo(b.dateObject) }
            if (count == 0) chatAdapter.notifyDataSetChanged()
            else {
              chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
          }
          binding.progressBar.visibility = View.GONE
          if (conversionId == null) {
            checkForConversion()
          }
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

  private fun addConversion(conversion: HashMap<String, Any>) {
    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        .add(conversion)
        .addOnSuccessListener {
          conversionId = it.id
        }
  }

  private fun updateConversion(message: String) {
    val documentReference = conversionId?.let {
      database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
          .document(it)
    }
    documentReference?.update(
        Constants.KEY_LAST_MESSAGE, message,
        Constants.KEY_TIMESTAMP, Date()
    )
  }

  private fun checkConversationRemotely(
    senderId: String,
    receiverId: String
  ) {
    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
        .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
        .get()
        .addOnCompleteListener {
          if (it.isSuccessful && it.result != null && it.result.documents.size > 0) {
            val documentSnapshot = it.result.documents[0]
            conversionId = documentSnapshot.id
          }
        }
  }

  private fun checkForConversion() {
    if (chatMessages.size != 0) {
      receivedUser.id?.let {
        preferenceManager.getString(Constants.KEY_USER_ID)
            ?.let { it1 -> checkConversationRemotely(it1, it) }
      }
      receivedUser.id?.let {
        preferenceManager.getString(Constants.KEY_USER_ID)
            ?.let { it1 -> checkConversationRemotely(it, it1) }
      }
    }
  }
}