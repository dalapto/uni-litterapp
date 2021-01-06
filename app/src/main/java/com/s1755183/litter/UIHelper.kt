package com.s1755183.litter

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog

class UIHelper {
    companion object {
        fun displayAlert(context: Context, title: String, message: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("OK"){ _, _ -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
        fun displayProgress(progressbar: ProgressBar) {
            progressbar.visibility = View.VISIBLE
        }
        fun hideProgress(progressbar: ProgressBar) {
            progressbar.visibility = View.INVISIBLE
        }
    }
}