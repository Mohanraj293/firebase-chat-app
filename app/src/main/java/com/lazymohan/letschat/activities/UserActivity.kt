package com.lazymohan.letschat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.lazymohan.letschat.adapters.UserAdapter
import com.lazymohan.letschat.databinding.ActivityUserBinding
import com.lazymohan.letschat.listeners.UserListener
import com.lazymohan.letschat.models.User
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager

class UserActivity : AppCompatActivity(),UserListener {
  private lateinit var binding: ActivityUserBinding
  private lateinit var preferenceManager: PreferenceManager
  private lateinit var userAdapter: UserAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityUserBinding.inflate(layoutInflater)
    setContentView(binding.root)
    preferenceManager = PreferenceManager(applicationContext)
    getUsers()
    setListeners()
  }

  private fun setListeners() {
    binding.imageBack.setOnClickListener {
      onBackPressed()
    }
  }

  private fun getUsers() {
    loading(true)
    val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    database.collection(Constants.KEY_COLLECTION_USERS)
        .get()
        .addOnCompleteListener {
          loading(false)
          val currentUserId: String? = preferenceManager.getString(Constants.KEY_USER_ID)
          if (it.isSuccessful && it.result != null) {
            val users = mutableListOf<User>()
            for (document: QueryDocumentSnapshot in it.result) {
              if (currentUserId.equals(document.id)) continue
              val user = User()
              user.name = document.getString(Constants.KEY_NAME)
              user.email = document.getString(Constants.KEY_EMAIL)
              user.image = document.getString(Constants.KEY_IMAGE)
              user.token = document.getString(Constants.KEY_FCM_TOKEN)
              user.id = document.id
              users.add(user)
              Log.d("User", user.name.toString())
            }
            if (users.size > 0) {
              binding.userRV.apply {
                layoutManager = LinearLayoutManager(this@UserActivity)
                userAdapter = UserAdapter(users,this@UserActivity)
                adapter = userAdapter
              }
            } else showErrorMessage()
          } else showErrorMessage()
        }
  }

  private fun showErrorMessage() {
    binding.textError.text = String.format("%s", "No User Available")
    binding.textError.visibility = View.VISIBLE
  }

  private fun loading(isLoading: Boolean) {
    if (isLoading) binding.progressBar.visibility = View.VISIBLE
    else binding.progressBar.visibility = View.INVISIBLE
  }

  override fun onUserClicked(user: User) {
    val intent = Intent(applicationContext,ChatActivity::class.java)
    intent.putExtra(Constants.KEY_USER,user)
    startActivity(intent)
    finish()
  }
}