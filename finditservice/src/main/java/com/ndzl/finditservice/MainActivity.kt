package com.ndzl.finditservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.os.RemoteException
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //start service FindItService
        startService(Intent(this, FindItService::class.java))
    }

    //private lateinit var mService: FindItService
    private var mService: Messenger? = null
    private var mBound: Boolean = false
    val MSG_SAY_HELLO = 111

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            println("finditservice/onServiceConnected")
/*            val binder = service as FindItService.LocalBinder
            mService = binder.getService()*/

            mService = Messenger(service)
            mBound = true

            //(mService.sayHello()) for local service
            val msg = android.os.Message.obtain(null, MSG_SAY_HELLO)
            try {
                mService?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            println("finditservice/onServiceDisconnected")
            mService= null
            mBound = false
        }
    }



    override fun onStart() {
        super.onStart()
/*
//this local service works fine
        println("finditservice/onStart local service")
        Intent(this, FindItService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
*/


        //non-local service to be bound
        val intent = Intent()
        intent.component = ComponentName("com.ndzl.finditservice", "com.ndzl.finditservice.FindItService")

        //bindService(intent, connection, Context.BIND_AUTO_CREATE)  //no more enabled, was a test
    }

    override fun onStop() {
        super.onStop()
        //unbindService(connection)
        mBound = false
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute).  */
/*    fun onButtonClick(v: View) {
        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call is something that might hang, then put this request
            // in a separate thread to avoid slowing down the activity performance.
            val num: Int = mService.randomNumber
            Toast.makeText(this, "number: $num", Toast.LENGTH_SHORT).show()
        }
    }*/
}
