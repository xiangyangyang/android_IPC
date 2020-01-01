package com.cxy.aidldemo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
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
    private val mListenerList = CopyOnWriteArrayList<IOnNewBookArrivedListener>()
    private var mIsServiceDestoryed = AtomicBoolean(false)

    private val mBinder = object : IBookManager.Stub() {
        override fun registerListener(listener: IOnNewBookArrivedListener?) {
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener)
            } else {
                Log.d(TAG, "already exists.")
            }
        }

        override fun unregisterListener(listener: IOnNewBookArrivedListener?) {
            if (mListenerList.contains(listener)) {
                mListenerList.remove(listener)
                Log.d(TAG, "unregister listener succeed.")

            } else {
                Log.d(TAG, "not found, can not unregister.")
            }

            Log.d(TAG, "unregisterListener, current size: ${mListenerList.size}")
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
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder;
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
        Log.d(TAG, "onNewBookArrived, notify listeners:${mListenerList.size}")
        for (i in 0..mListenerList.size) {
            Log.d(TAG, "onNewBookArrived, notify listener: ${mListenerList[i]}")
            mListenerList[i].onNewBookArrived(book)
        }
    }

}