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
import kotlinx.parcelize.Parceler
import java.util.Random
import kotlin.collections.forEach
import kotlin.collections.remove
import kotlinx.parcelize.Parcelize

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
                    mustLoop = true

                    //repeat triggerEvent every 1 second
                    while(mustLoop){
                        Thread.sleep(
                            (500..900).random().toLong()
                        )
                        eventGenerator.triggerEvent(msg)
                    }

                }
                MSG_RFID_TRIGGER_STOP -> {
                    mustLoop = false
                    eventGenerator.removeListener(eventListener)
                }
                else -> super.handleMessage(msg)
                // Handle other messages
            }
        }
    }
    /*

        inner class LocalBinder  : Binder() {
            // Return this instance of LocalService so clients can call public methods.
            fun getService(): FindItService = this@FindItService
        }
    */

    lateinit var eventGenerator: RFIDEventGenerator
    lateinit var eventListener: RFIDEventListenerImpl
    inner class RFIDEventListenerImpl : RFIDEventListener {
        override fun eventReadNotify(event: RFIDEvent, msg: Message) {

            println("Event received EPC=${event.rfidEPC}")

            val bundleResult: Bundle = Bundle()

            bundleResult.putString("rfidEPC", event.rfidEPC )
            if(msg != null)
                msg.replyTo.send(Message.obtain(null, 10999, bundleResult))

        }
    }

    var mustLoop = true

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