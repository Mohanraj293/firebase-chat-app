package com.lazymohan.letschat.listeners

import com.lazymohan.letschat.models.User

interface UserListener {
  fun onUserClicked(user: User)
}