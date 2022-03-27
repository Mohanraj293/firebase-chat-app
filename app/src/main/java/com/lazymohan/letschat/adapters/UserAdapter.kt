package com.lazymohan.letschat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazymohan.letschat.databinding.ItemContainerUserBinding
import com.lazymohan.letschat.listeners.UserListener
import com.lazymohan.letschat.models.User

class UserAdapter(private val users: List<User>,private val userListener: UserListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

  inner class UserViewHolder(private val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setUserData(user: User) {
      binding.textName.text = user.name
      binding.textEmail.text = user.email
      binding.imageProfile.setImageBitmap(user.image?.let { getUserImage(it) })
      binding.root.setOnClickListener {
        userListener.onUserClicked(user)
      }
    }
  }

  private fun getUserImage(encodedImage: String): Bitmap {
    val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): UserViewHolder {
    return UserViewHolder(
        ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
  }

  override fun onBindViewHolder(
    holder: UserViewHolder,
    position: Int
  ) {
    holder.setUserData(user = users[position])
  }

  override fun getItemCount(): Int = users.size
}