package com.lazymohan.letschat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lazymohan.letschat.databinding.ItemContainerReceivedBinding
import com.lazymohan.letschat.databinding.ItemContainerSentMessageBinding
import com.lazymohan.letschat.models.ChatMessage

class ChatAdapter constructor(private val chatMessage: List<ChatMessage>,private val receiverProfileImage: Bitmap,private val senderId:String)
  :RecyclerView.Adapter<ViewHolder>(){

  private val VIEW_TYPE_SENT = 1
  private val VIEW_TYPE_RECEIVED = 2

  inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding): ViewHolder(binding.root) {
    fun setData(chatMessage: ChatMessage){
      binding.textMessage.text = chatMessage.message
      binding.textDateTime.text = chatMessage.dateTime
    }
  }

  inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedBinding):ViewHolder(binding.root){
    fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap){
      binding.textMessage.text =chatMessage.message
      binding.textDateTime.text= chatMessage.dateTime
      binding.imageProfile.setImageBitmap(receiverProfileImage)
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    if (viewType ==VIEW_TYPE_SENT){
      return SentMessageViewHolder(
          ItemContainerSentMessageBinding.inflate(
              LayoutInflater.from(parent.context),
              parent,
              false
          )
      )
    }else{
      return ReceivedMessageViewHolder(
          ItemContainerReceivedBinding.inflate(
              LayoutInflater.from(parent.context),
              parent,
              false
          )
      )
    }
  }

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    if (getItemViewType(position) == VIEW_TYPE_SENT)
        (holder as SentMessageViewHolder).setData(chatMessage[position])
    else
      (holder as ReceivedMessageViewHolder).setData(chatMessage[position],receiverProfileImage)
  }

  override fun getItemCount(): Int = chatMessage.size

  override fun getItemViewType(position: Int): Int {
    return if(chatMessage[position].senderId.equals(senderId)) VIEW_TYPE_SENT
    else VIEW_TYPE_RECEIVED
  }
}