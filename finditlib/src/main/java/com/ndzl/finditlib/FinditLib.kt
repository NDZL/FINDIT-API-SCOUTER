package com.ndzl.finditlib

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class FinditLib {
    
    fun libInit(appctx: Context) {
        val intent = Intent()
        intent.component = ComponentName("com.ndzl.finditservice", "com.ndzl.finditservice.FindItService")

        appctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        println("FinditLib initialized.")
    }

    private var mService: Messenger? = null
    private var mBound: Boolean = false
    val MSG_SAY_HELLO = 111
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            println("FinditLib/onServiceConnected")

            mService = Messenger(service)
            mBound = true

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            println("FinditLib/onServiceDisconnected")
            mService= null
            mBound = false
        }
    }


    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i("FinditLib","IncomingHandler/handleMessage "+msg.what)
        }
    }

    fun libCallServiceSayHello(){
        val msg = android.os.Message.obtain(null, MSG_SAY_HELLO)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun libCallServiceGetRnd(){
        val msg = android.os.Message.obtain(null, MSG_GET_RND)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

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