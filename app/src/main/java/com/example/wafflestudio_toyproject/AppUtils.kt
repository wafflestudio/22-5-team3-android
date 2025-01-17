package com.example.wafflestudio_toyproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.wafflestudio_toyproject.LoginActivity

object AppUtils {
    fun navigateToLoginScreen(context: Context) {
        val currentActivity = context as? AppCompatActivity
        if (currentActivity != null && currentActivity !is LoginActivity) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
}
