package com.example.wafflestudio_toyproject

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wafflestudio_toyproject.databinding.ActivityLoginBinding
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // 자동 로그인 설정 여부 읽기
        val autoLoginEnabled = sharedPreferences.getBoolean("AUTO_LOGIN_ENABLED", false)
        binding.autoLoginCheckBox.isChecked = autoLoginEnabled

        // 체크박스 상태 저장
        binding.autoLoginCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("AUTO_LOGIN_ENABLED", isChecked).apply()
        }

        // 자동 로그인 여부 확인 및 처리
        if (autoLoginEnabled && isUserLoggedIn()) {
            navigateToMainScreen()
        }

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

        binding.kakaoButton.setOnClickListener {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오 로그인 실패", error)
                    Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                } else if (token != null) {
                    Log.d("KakaoLogin", "카카오 로그인 성공! 액세스 토큰: ${token.accessToken}")

                    userRepository.loginWithKakao(token.accessToken,
                        onSuccess = { response->
                            Toast.makeText(this, "카카오 로그인 성공!", Toast.LENGTH_SHORT).show()
                            userRepository.saveTokens(response.access_token, response.refresh_token)

                            if (binding.autoLoginCheckBox.isChecked) {
                                sharedPreferences.edit().putBoolean("AUTO_LOGIN_ENABLED", true).apply()
                            }

                            navigateToMainScreen()
                        },
                        onError = { message ->
                            Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        binding.googleButton.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
                override fun onSuccess() {
                    val naverToken = NaverIdLoginSDK.getAccessToken()
                    if (naverToken != null) {
                        Log.d("NaverLogin", "네이버 로그인 성공! 액세스 토큰: $naverToken")

                        userRepository.loginWithNaver(naverToken,
                            onSuccess = { response->
                                Toast.makeText(this@LoginActivity, "네이버 로그인 성공!", Toast.LENGTH_SHORT).show()
                                userRepository.saveTokens(response.access_token, response.refresh_token)

                                if (binding.autoLoginCheckBox.isChecked) {
                                    sharedPreferences.edit().putBoolean("AUTO_LOGIN_ENABLED", true).apply()
                                }
                                navigateToMainScreen()
                            },
                            onError = { message ->
                                Toast.makeText(this@LoginActivity, "네이버 로그인 실패: $message", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(this@LoginActivity, "네이버 로그인 실패: 토큰을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()

                    Log.e("NaverLogin", "네이버 로그인 실패: [ErrorCode: $errorCode] $errorDescription")
                    Toast.makeText(this@LoginActivity, "네이버 로그인 실패: $errorDescription", Toast.LENGTH_SHORT).show()
                }

                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }
            })
        }
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

                // 자동 로그인이 활성화된 경우 토큰 저장
                if (binding.autoLoginCheckBox.isChecked) {
                    sharedPreferences.edit().putBoolean("AUTO_LOGIN_ENABLED", true).apply()
                }
                navigateToMainScreen()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "아이디나 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
