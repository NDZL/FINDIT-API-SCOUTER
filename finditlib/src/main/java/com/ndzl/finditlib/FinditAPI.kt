package com.ndzl.finditlib

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children


class FinditAPI {

    fun dynamicallyDisplayInternalView(consumerContext: Context, parentLayout: ViewGroup) {
        val newTextView = TextView(consumerContext)
        newTextView.text = "Dynamically added TextView from inside the library"

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        newTextView.layoutParams = params
        parentLayout.addView(newTextView,  parentLayout.childCount)

    }


    fun dynamicallyDisplayParametricView(consumerContext: Context, parentLayout: ViewGroup, viewToBeDisplayed: View) {

        parentLayout.addView(viewToBeDisplayed,  parentLayout.childCount)

    }



    companion object
    {
        val MSG_GET_RND = 222

        fun whoAreYou() {
            println("This is  the FinditAPI class.")
        }
    }
}