package com.example.wafflestudio_toyproject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wafflestudio_toyproject.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
/*
        if(isUserLoggedIn()){
            navigateToMainScreen()
        }


 */

    }

    private fun isUserLoggedIn(): Boolean {
        val accessToken = userRepository.getAccessToken()
        Log.d("LoginActivity", "Access Token: $accessToken")
        return !accessToken.isNullOrEmpty() && accessToken != "null"
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun login(username: String, password: String) {
        userRepository.login(username, password,
            onSuccess = { response ->
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                userRepository.saveTokens(response.access_token, response.refresh_token)
                navigateToMainScreen()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "아이디나 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
