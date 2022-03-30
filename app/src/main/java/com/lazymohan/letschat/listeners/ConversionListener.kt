package com.lazymohan.letschat.listeners

import com.lazymohan.letschat.models.User

interface ConversionListener {
  fun onConversionClicked(user: User)
}