package com.github.video

import android.view.View

fun View.getFirstY(): Int {
    val location = IntArray(2)
    this.getLocationInWindow(location)
    return location[1]
}