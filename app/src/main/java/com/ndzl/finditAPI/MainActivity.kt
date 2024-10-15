package com.ndzl.finditAPI

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ndzl.finditlib.FinditLib
import com.ndzl.finditlib.RFIDEvent
import com.ndzl.finditlib.RFIDEventListener

class MainActivity : AppCompatActivity() {

    lateinit var finditLIB: FinditLib
    lateinit var finditServiceEventListener: RFIDEventListenerImpl

    var tvOut : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        tvOut = findViewById<TextView>(R.id.tvOut)

        finditLIB = FinditLib()
        finditLIB.libInit( this )
        finditServiceEventListener = RFIDEventListenerImpl()
        FinditLib.rfidEventRelay.addListener(finditServiceEventListener)
    }

    fun onClickbtn_SCAN(v: View?) {
        try {

            //put a View in a Bundle? no!
            val newTextView = TextView(this)
            newTextView.text = "Dynamically added TextView. The consumer app passed this view to the library."
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            newTextView.layoutParams = params

            val bundle = Bundle()
            //   bundle.putParcelable("view", newTextView)  //!! CAN'T BE DONE


            //send the bundle to the finditservice
            val intent = Intent()
            intent.component = ComponentName("com.ndzl.finditservice", "com.ndzl.finditservice.FindItService")
            intent.putExtra("view", bundle)
            startService(intent)


        } catch (e: Exception) {
            Log.e("TAG", "onClickbtn_SCAN " + e.message)
        }
    }



    fun onClickbtn_SWITCH_ONE(v: View?) {
        try {

            FinditLib.whoAreYou()

            //get parent layout
            val parentLayout: ViewGroup = findViewById(R.id.linearLayout1)

            finditLIB.dynamicallyDisplayInternalView(this, parentLayout)


        } catch (e: Exception) {
            Log.e("TAG", "onClickbtn_SWITCH_ONE " + e.message)
        }
    }


    fun onClickbtn_SWITCH_TWO(v: View?) {
        try {

            val newTextView = TextView(this)
            newTextView.text = "Dynamically added TextView. The consumer app passed this view to the library."
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            newTextView.layoutParams = params

            val parentLayout: ViewGroup = findViewById(R.id.linearLayout1)
            finditLIB.dynamicallyDisplayParametricView(this, parentLayout, newTextView)

        } catch (e: Exception) {
            Log.e("TAG", "onClickbtn_SWITCH_TWO " + e.message)
        }
    }



    private var mService: Messenger? = null
    private var mBound: Boolean = false
    val MSG_SAY_HELLO = 111
    val MSG_GET_RND = 222

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            println("finditservice/onServiceConnected")
            /*            val binder = service as FindItService.LocalBinder
                        mService = binder.getService()*/

            mService = Messenger(service)
            mBound = true

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            println("finditservice/onServiceDisconnected")
            mService= null
            mBound = false
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            println("finditservice/onBindingDied")
        }
        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            println("finditservice/onNullBinding")
        }
    }


    private val messenger = Messenger(IncomingHandler())

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i("IncomingHandler","IncomingHandler/handleMessage "+msg.what)
        }
    }

    fun callServiceSayHello(){
        val msg = android.os.Message.obtain(null, MSG_SAY_HELLO)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    fun callServiceGetRnd(){
        val msg = android.os.Message.obtain(null, MSG_GET_RND)
        msg.replyTo = Messenger(IncomingHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    override fun onStart() {
        super.onStart()

        /*//MOVING THE BINDING TO THE FAT LIBRARY
                val intent = Intent()
                intent.component = ComponentName("com.ndzl.finditservice", "com.ndzl.finditservice.FindItService")

                bindService(intent, connection, Context.BIND_AUTO_CREATE)
                */


    }

    fun onClickbtn_SWITCH_THREE(v: View?) {
        try {
            /*

                        //(mService.sayHello()) for local service

                        //calling SayHello via Messenger
                        callServiceSayHello()
                        callServiceGetRnd()

            */
            finditLIB.libCallServiceGetRnd()

        } catch (e: Exception) {
            Log.e("TAG", "onClickbtn_SWITCH_THREE " + e.message)
        }
    }

    var isRunning: Boolean = false
    fun onClickbtn_SWITCH_FOUR(v: View?) {
        try {
            if(!isRunning) {
                isRunning = true
                finditLIB.libCallServiceRFIDInit()
                finditLIB.libCallServiceRFIDStart()
            } else {
                isRunning = false
                finditLIB.libCallServiceRFIDStop()
            }
        } catch (e: Exception) {
            Log.e("TAG", "onClickbtn_SWITCH_FOUR " + e.message)
        }
    }


    inner class RFIDEventListenerImpl : RFIDEventListener {
        override fun eventReadNotify(event: RFIDEvent) {

            println("--- MainActivity --- Event received EPC=${event.rfidEPC}")
            tvOut!!.setText("EPC: ${event.rfidEPC}")

        }
    }
}


/*
* can you marshal a View via Messenger?
*
* No, you cannot directly marshal a View object via Messenger. View objects are not Parcelable or Serializable, and they are tightly coupled to the UI thread and the current activity context. Sending a View across processes would lead to various issues:
UI Thread Confinement: View objects can only be accessed and modified from the UI thread. Sending them to another process (e.g., a service) would violate this principle and could lead to crashes or unexpected behavior.
Context Dependency: View objects rely on the context of the activity they are part of. Sending them to another process would lose this context and make them unusable.
Serialization Complexity: View objects have a complex internal structure and dependencies on other objects, making them difficult to serialize effectively. Alternatives If you need to communicate information related to a View to a service, consider these alternatives:
Extract Data: Instead of sending the entire View, extract the relevant data (e.g., text content, image resource ID, position) and send that data using Parcelable or Bundle with Messenger.
Use IDs: If you need to refer to a specific View in the service, send its ID and let the service handle any necessary UI updates on the main thread using an appropriate mechanism (e.g., by starting an activity or sending a broadcast).
Screenshots: If you need to send a visual representation of the View, capture a screenshot and send the image data.
Custom Data Structures: Define your own data structures that represent the essential information of the View and make them Parcelable. Important Considerations
UI Updates from Services: Remember that services cannot directly update the UI. If the service needs to modify the UI based on the received information, it should communicate with an activity (e.g., using broadcasts or pending intents) to trigger the UI changes on the main thread.
Security and Privacy: Avoid sending sensitive or personal data extracted from View objects to services unless absolutely necessary. By using these alternatives, you can achieve the desired communication while avoiding the problems associated with directly marshalling View objects. If you can provide more details about what you're trying to achieve by sending View data to a service, I can give you more specific guidance on the best approach.
* */