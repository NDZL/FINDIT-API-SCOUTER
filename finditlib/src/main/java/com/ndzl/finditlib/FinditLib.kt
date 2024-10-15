package com.ndzl.finditlib

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize


data class RFIDEvent(val rfidEPC: String)

interface RFIDEventListener {
    fun eventReadNotify(event: RFIDEvent)
}
class RFIDEventRelay {
    private val listeners = mutableListOf<RFIDEventListener>()

    fun addListener(listener: RFIDEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: RFIDEventListener) {
        listeners.remove(listener)
    }

    fun triggerEvent(ev: RFIDEvent) {
        listeners.forEach { it.eventReadNotify(ev) }
    }
}




class FinditLib {

    fun libInit(appctx: Context) {
        val intent = Intent()
        intent.component = ComponentName("com.ndzl.finditservice", "com.ndzl.finditservice.FindItService")

        rfidEventRelay = RFIDEventRelay()

        appctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        println("FinditLib initialized.")
    }

    private var mService: Messenger? = null
    private var mBound: Boolean = false


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
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun handleMessage(msg: Message) {
            Log.i("FinditLib","IncomingHandler/handleMessage "+msg.what/*+" "+msg.obj.toString()*/)
            if(msg.obj != null){
                val bundle = msg.obj as Bundle
                val rfidRes = bundle.getString("rfidEPC")
                println("FinditLib/handleMessage RFID EPC: ${rfidRes.toString()}")
                if(msg.what==MSG_RFID_RESULT){

                    rfidEventRelay.triggerEvent(RFIDEvent(rfidRes.toString()))


                }
            }
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



    fun libCallServiceRFIDInit(){
        val msg = android.os.Message.obtain(null, MSG_RFID_INIT)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun libCallServiceRFIDStart(){
        val msg = android.os.Message.obtain(null, MSG_RFID_TRIGGER_START)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    fun libCallServiceRFIDStop(){
        val msg = android.os.Message.obtain(null, MSG_RFID_TRIGGER_STOP)
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
        val MSG_SAY_HELLO = 111
        val MSG_GET_RND = 222
        val MSG_RFID_INIT = 10111
        val MSG_RFID_TRIGGER_START = 10222
        val MSG_RFID_TRIGGER_STOP = 10333
        val MSG_RFID_RESULT = 10999

        fun whoAreYou() {
            println("This is  the FinditLib class.")
        }

        lateinit var rfidEventRelay: RFIDEventRelay
    }
}