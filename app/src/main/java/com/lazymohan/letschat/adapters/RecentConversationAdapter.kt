package com.lazymohan.letschat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazymohan.letschat.databinding.ItemContainerRecentConversationBinding
import com.lazymohan.letschat.listeners.ConversionListener
import com.lazymohan.letschat.models.ChatMessage
import com.lazymohan.letschat.models.User

class RecentConversationAdapter(private val chatMessage: List<ChatMessage>,private val conversionListener: ConversionListener): RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>() {

  inner class ConversionViewHolder(private val binding: ItemContainerRecentConversationBinding):RecyclerView.ViewHolder(binding.root){
    fun setData(chatMessage: ChatMessage){
      binding.imageProfile.setImageBitmap(
          chatMessage.conversionImage?.let { getConversionImage(it) })
      binding.textName.text = chatMessage.conversionName
      binding.textRecentMessage.text = chatMessage.message
      binding.root.setOnClickListener {
        val user = User()
        user.id = chatMessage.conversionId
        user.name = chatMessage.conversionName
        user.image = chatMessage.conversionImage
        conversionListener.onConversionClicked(user)
      }
    }
  }


  private fun getConversionImage(encodedImage: String): Bitmap {
    val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ConversionViewHolder {
    return ConversionViewHolder(
        ItemContainerRecentConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
  }

  override fun onBindViewHolder(
    holder: ConversionViewHolder,
    position: Int
  ) {
    holder.setData(chatMessage[position])
  }

  override fun getItemCount(): Int = chatMessage.size
}