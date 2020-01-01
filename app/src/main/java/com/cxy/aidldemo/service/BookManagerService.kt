package com.cxy.aidldemo.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.cxy.aidldemo.aidl.Book
import com.cxy.aidldemo.aidl.IBookManager
import com.cxy.aidldemo.aidl.IOnNewBookArrivedListener
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class BookManagerService : Service() {
    private val TAG: String = "BMS"
    //A thread-safe variant of {@link java.util.ArrayList} in which all mutative operations
    // service is runned in binder pool and has to support multi client requests.
    private val mBookList = CopyOnWriteArrayList<Book>()
    // 对象是不能跨进程的，对角的跨进程传输是通过序列化读写来重写生成在对象，所以是不同的对象。
    // 当通过客户端 unregister service listener时，要通过底层的共通binder对象来实现
    // 具体是通过RemoteCallbackList(内部自动实现了线程同步功能)
    //private val mListenerList = CopyOnWriteArrayList<IOnNewBookArrivedListener>()
    private val mListenerList = RemoteCallbackList<IOnNewBookArrivedListener>()
    private var mIsServiceDestoryed = AtomicBoolean(false)

    private val mBinder = object : IBookManager.Stub() {
        override fun registerListener(listener: IOnNewBookArrivedListener?) {
//            if (!mListenerList.contains(listener)) {
//                mListenerList.add(listener)
//            } else {
//                Log.i(TAG, "already exists.")
//            }
            mListenerList.register(listener)
        }

        override fun unregisterListener(listener: IOnNewBookArrivedListener?) {
//            if (mListenerList.contains(listener)) {
//                mListenerList.remove(listener)
//                Log.i(TAG, "unregister listener succeed.")
//
//            } else {
//                Log.i(TAG, "not found, can not unregister.")
//            }
//            Log.i(TAG, "unregisterListener, current size: ${mListenerList.size}")

            mListenerList.unregister(listener)
            Log.i(TAG, "unregisterListener, current size: ${mListenerList.beginBroadcast()}")
        }

        override fun getBookList(): MutableList<Book> {
            return mBookList
        }

        override fun addBook(book: Book?) {
            mBookList.add(book!!)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mBookList.add(Book(1, "Android 开发艺术探索"))
        mBookList.add(Book(2, "headfirst"))
        Thread(ServiceWorker()).start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        val check =
            checkCallingOrSelfPermission("com.cxy.aidldemo.aidl.permission.ACCESS_BOOK_SERVICE")
        if (check == PackageManager.PERMISSION_DENIED) {
            return null
        }
        Log.i(TAG, "permission is authenticated")
        return mBinder
    }

    override fun onDestroy() {
        mIsServiceDestoryed.set(true)
        super.onDestroy()
    }

    private inner class ServiceWorker : Runnable {
        override fun run() {
            while (!mIsServiceDestoryed.get()) {
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                var bookId = mBookList.size + 1
                val newBook = Book(bookId, "new book#$bookId")
                onNewBookArrived(newBook)
            }
        }

    }

    private fun onNewBookArrived(book: Book) {
        mBookList.add(book)
//        Log.i(TAG, "onNewBookArrived, notify listeners:${mListenerList.size}")
//        for (i in 0 until mListenerList.size) {
//            Log.i(TAG, "onNewBookArrived, notify listener: ${mListenerList[i]}")
//            mListenerList[i].onNewBookArrived(book)
//        }
        for (i in 0 until mListenerList.beginBroadcast()) {
            mListenerList.getBroadcastItem(i).onNewBookArrived(book)
        }

        mListenerList.finishBroadcast()
    }

}