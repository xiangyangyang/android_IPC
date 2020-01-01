package com.cxy.aidldemo.aidl

import android.os.Parcel
import android.os.Parcelable

class Book(var bookId: Int, var bookName:String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(bookId)
        dest.writeString(bookName)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Book(bookId=$bookId, bookName='$bookName')"
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }


}
