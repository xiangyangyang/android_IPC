package com.cxy.aidldemo.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cxy.aidldemo.R
import com.cxy.aidldemo.aidl.Book
import com.cxy.aidldemo.aidl.IBookManager
import com.cxy.aidldemo.aidl.IOnNewBookArrivedListener
import com.cxy.aidldemo.service.BookManagerService
import java.lang.ref.WeakReference

class BookManagerActivity : AppCompatActivity() {
    private val TAG = "BookManagerActivity"
    private val MESSAGE_NEW_BOOK_ARRIVED = 1
    private lateinit var mRemoteBookManager: IBookManager
    private lateinit var mHandler: Handler
    private lateinit var mIOnNewBookArrivedListener: IOnNewBookArrivedListener
    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mRemoteBookManager = IBookManager.Stub.asInterface(service)
            try {
                val list = mRemoteBookManager.bookList;
                Log.i(TAG, "query book list, list type: ${list.javaClass.canonicalName}")
                Log.i(TAG, "query book list: ${list.toString()}")
                mRemoteBookManager.addBook(Book(3, "ios"))
                Log.i(TAG, "add book")
                mRemoteBookManager.bookList.also(::println)
                mRemoteBookManager.registerListener(mIOnNewBookArrivedListener)

            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_manager)
        /**
         * If the Handler is using the Looper or MessageQueue of the main thread,
         * you need to fix your Handler declaration, as follows: Declare the Handler as a static class;
         * In the outer class, instantiate a WeakReference to the outer class and pass this object to your
         * Handler when you instantiate the Handler; Make all references to members of the outer class using
         * the WeakReference object.??
         */
        mHandler = MyHandler(WeakReference(this))
        mIOnNewBookArrivedListener = object : IOnNewBookArrivedListener.Stub() {
            override fun onNewBookArrived(newBook: Book?) {
                mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget()
            }
        }

        val intent = Intent(this, BookManagerService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        if (mRemoteBookManager?.asBinder().isBinderAlive) {
            Log.d(TAG, "unregister listener: $mIOnNewBookArrivedListener")
            mRemoteBookManager.unregisterListener(mIOnNewBookArrivedListener)
        }
        unbindService(mConnection)
        super.onDestroy()
    }

    class MyHandler(private val outerClass: WeakReference<BookManagerActivity>) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                outerClass.get()!!.MESSAGE_NEW_BOOK_ARRIVED -> Log.d(
                    outerClass.get()!!.TAG,
                    "receive new book: ${msg.obj}"
                )
                else -> super.handleMessage(msg)
            }
        }
    }
}
