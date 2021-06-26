package com.yaksonn.shoppingapp.utils

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.interfaces.AddMediaClickListener

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun Context.showAlertDialog(cancelable: Boolean = false, cancelableTouchOutside: Boolean = false, builderFunction: AlertDialog.Builder.() -> Any) {
    val builder = AlertDialog.Builder(this)
    builder.builderFunction()
    val dialog = builder.create()
    dialog.setCancelable(cancelable)
    dialog.setCanceledOnTouchOutside(cancelableTouchOutside)
    dialog.show()
}

fun AlertDialog.Builder.positiveButton(text: String = "OK", handleClick: (i: Int) -> Unit = {}) {
    this.setPositiveButton(text) { _, i -> handleClick(i) }
}

fun AlertDialog.Builder.negativeButton(text: String = "CANCEL", handleClick: (i: Int) -> Unit = {}) {
    this.setNegativeButton(text) { _, i -> handleClick(i) }
}


fun alertImage(context: Context, addMediaClickListener: AddMediaClickListener) {
    val items = context.resources.getStringArray(R.array.takephoto)

    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setTitle(context.getString(R.string.photoChoose))
    builder.setItems(items, object : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, item: Int) {
            if (item == 0) {

                addMediaClickListener.clickPhoto(0)

            } else if (item == 1) {

                addMediaClickListener.clickPhoto(1)


            } else {
                dialog.dismiss()
            }
        }
    })
    builder.show()
}

fun getPathLocalUri(uri: Uri, context: Context): String {
    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    val cursor: Cursor = context.contentResolver.query(uri, projection, null, null, null)!!
    val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    cursor.moveToFirst()
    return cursor.getString(columnIndex)
}

fun rotate(bitmap: Bitmap, degree: Int): Bitmap? {
    val w = bitmap.width
    val h = bitmap.height
    val mtx = Matrix()
    mtx.postRotate(degree.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
}