package com.ndzl.finditservice

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parceler
import java.util.Random
import kotlin.collections.forEach
import kotlin.collections.remove
import kotlinx.parcelize.Parcelize
import kotlin.concurrent.thread

data class RFIDEvent(val rfidEPC: String)
/*
//got crazy with parcelable - use simple Strings instead
@Parcelize
class RFIDResults(val rfidEPC: String): Parcelable
*/

interface RFIDEventListener {
    fun eventReadNotify(event: RFIDEvent, msg: Message)
}

class RFIDEventGenerator {
    private val listeners = mutableListOf<RFIDEventListener>()

    fun addListener(listener: RFIDEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: RFIDEventListener) {
        listeners.remove(listener)
    }

    fun triggerEvent(msg: Message) {

        val randomNumber = (100..999).random()
        val event = RFIDEvent(""+randomNumber)
        listeners.forEach { it.eventReadNotify(event, msg) }
    }
}


var mustLoop = true



class FindItService : Service() {


    // Random number generator.
    private val mGenerator = Random()

    /** Method for clients.  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    val MSG_SAY_HELLO = 111
    val MSG_GET_RND = 222
    val MSG_RFID_INIT = 10111
    val MSG_RFID_TRIGGER_START = 10222
    val MSG_RFID_TRIGGER_STOP = 10333
    val MSG_RFID_RESULT = 10999


    private val messenger = Messenger(IncomingHandler())


    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            print("IncomingHandler/handleMessage")

            when (msg.what) {
                MSG_SAY_HELLO -> {
                    // Invoke service method
                    msg.replyTo.send(Message.obtain(null,123 ))
                    sayHello()
                    //msg.replyTo.send(Message.obtain(null, 0, "Greetings were sent!".toByteArray()))

                }
                MSG_GET_RND -> {
                    // Invoke service method
                    val rnd = randomNumber
                    msg.replyTo.send(Message.obtain(null, 0+rnd as Int))

                }
                MSG_RFID_INIT -> {
                    eventGenerator = RFIDEventGenerator()
                    eventListener = RFIDEventListenerImpl()
                    eventGenerator.addListener(eventListener)
                }
                MSG_RFID_TRIGGER_START -> {
                    mustLoop=true
                    for (i in 1..100) {
                        eventGenerator.triggerEvent(msg)
                        Thread.sleep(300)
                    }
                }
                MSG_RFID_TRIGGER_STOP -> {
                    mustLoop = false
                }
                else -> super.handleMessage(msg)
                // Handle other messages
            }
            println("end of when block")
        }
    }
    /*

        inner class LocalBinder  : Binder() {
            // Return this instance of LocalService so clients can call public methods.
            fun getService(): FindItService = this@FindItService
        }
    */

    private fun startRepeatingTask(timeInterval: Long) {
        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                if(mustLoop) {
                    Log.d("OCR", "startRepeatingTask ")

                }
                handler.postDelayed(this, timeInterval)

            }
        }
        handler.postDelayed(runnable, timeInterval)
    }

    lateinit var eventGenerator: RFIDEventGenerator
    lateinit var eventListener: RFIDEventListenerImpl
    inner class RFIDEventListenerImpl : RFIDEventListener {
        override fun eventReadNotify(event: RFIDEvent, msg: Message) {

            println("Event received EPC=${event.rfidEPC}")

            val bundleResult: Bundle = Bundle()

            bundleResult.putString("rfidEPC", event.rfidEPC )
            if(msg != null)
                msg.replyTo.send(Message.obtain(null, MSG_RFID_RESULT, bundleResult))

        }
    }



    public fun sayHello() {
        Log.d("FINDIT SERVICE", "Hello from service!")

        //start activity ServiceActivity
        val intent = Intent(this, ServiceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onBind(intent: Intent): IBinder {
        return messenger.binder
        //return LocalBinder()
    }
}