package com.example.maptest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

inline fun <reified A : Activity> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}

inline fun <reified A : Activity> Fragment.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), A::class.java).apply(configIntent))
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Context.toast(@StringRes messageId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, messageId, duration).show()

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(activity, message, duration).show()

fun Fragment.toast(@StringRes messageId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(activity, messageId, duration).show()