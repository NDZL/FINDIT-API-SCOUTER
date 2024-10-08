package com.ndzl.finditservice

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.util.Log
import java.util.Random

class FindItService : Service() {


    // Random number generator.
    private val mGenerator = Random()

    /** Method for clients.  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    val MSG_SAY_HELLO = 111
    val MSG_GET_RND = 222

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

    //convert string into parcelable object


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