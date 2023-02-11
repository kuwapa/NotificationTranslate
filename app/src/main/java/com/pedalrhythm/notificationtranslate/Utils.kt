package com.pedalrhythm.notificationtranslate

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.text.Spanned
import android.text.SpannedString
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

fun Activity.showMaterialDialog(
    title: String?,
    message: String?,
    positiveBtnClickListener: DialogInterface.OnClickListener?,
    positiveButton: String,
    negativeBtnClickListener: DialogInterface.OnClickListener?,
    negativeButton: String,
    cancelable: Boolean,
    dismissOnButtonClick: Boolean = true,
    backPressIfNonCancellable: Boolean = false,
    onDismissListener: DialogInterface.OnDismissListener? = null,
) : AlertDialog {

    return showMaterialDialogWithSpan(if (title.isNullOrEmpty()) null else SpannedString(title),
        if (message.isNullOrEmpty()) null else SpannedString(message),
        positiveBtnClickListener,
        positiveButton,
        negativeBtnClickListener,
        negativeButton,
        cancelable,
        dismissOnButtonClick,
        backPressIfNonCancellable,
        onDismissListener)
}

fun Activity.showMaterialDialogWithSpan(
    title: Spanned?,
    messageSpanned: Spanned?,
    positiveBtnClickListener: DialogInterface.OnClickListener?,
    positiveButton: String,
    negativeBtnClickListener: DialogInterface.OnClickListener?,
    negativeButton: String,
    cancelable: Boolean,
    dismissOnButtonClick: Boolean = true,
    backPressIfNonCancellable: Boolean = false,
    onDismissListener: DialogInterface.OnDismissListener? = null,
) : AlertDialog {

    val builder = AlertDialog.Builder(this, R.style.FloatingDialogStyle)
    builder.setTitle(null)
    builder.setMessage(null)
    builder.setView(R.layout.dialog_regular_layout)
    builder.setOnDismissListener(onDismissListener)
    builder.setCancelable(cancelable)

    val alertDialog = builder.create()
    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    alertDialog.setOnShowListener {

        val posButton = alertDialog.findViewById<TextView>(R.id.regularDialogPositiveButton)
        val negButton = alertDialog.findViewById<TextView>(R.id.regularDialogNegativeButton)
        val messageTextView = alertDialog.findViewById<TextView>(R.id.regularDialogMessageTextView)
        val titleTextView = alertDialog.findViewById<TextView>(R.id.regularDialogTitleTextView)

        if (title != null) {
            titleTextView?.text = title
            titleTextView?.show()
        }

        if (messageSpanned != null) {
            messageTextView?.text = messageSpanned
            messageTextView?.show()
        }

        if (positiveBtnClickListener != null) {
            posButton?.text = positiveButton
            posButton?.show()
        }

        if (negativeBtnClickListener != null) {
            negButton?.text = negativeButton
            negButton?.show()
        }

        posButton?.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
        negButton?.setTextColor(ContextCompat.getColor(this, R.color.teal_700))

        posButton?.setOnClickListener {
            positiveBtnClickListener?.onClick(alertDialog, 0)
            if (dismissOnButtonClick) {
                alertDialog.dismiss()
            }
        }

        negButton?.setOnClickListener {
            negativeBtnClickListener?.onClick(alertDialog, 0)
            if (dismissOnButtonClick) {
                alertDialog.dismiss()
            }
        }
    }

    /*
    Sometimes like when showing sample route loaded where the dialog is shown with a delay, if the
    user closed the app the app would crash since the dialog didnt have an activity to be visible in.
    So added this check
     */
    if (!isFinishing) {
        alertDialog.show()
    }

    if (!cancelable && backPressIfNonCancellable)
        alertDialog.setOnKeyListener { _, keyCode, _ ->
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                val activity = this@showMaterialDialogWithSpan
                activity.onBackPressed()
                true // pretend we've processed it
            } else
                false // pass on to be processed as normal
        }

    return alertDialog
}

fun View.show() {
    visibility = View.VISIBLE
}

//check notification access setting is enabled or not
fun Context.isNotificationsAccessEnabled(): Boolean {
    try {
        return Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            .contains(packageName)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

val Number.toPx get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics)