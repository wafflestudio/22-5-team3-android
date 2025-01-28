package com.example.wafflestudio_toyproject
import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wafflestudio_toyproject.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //학과 선택 스피너 세팅
        val departments = listOf(
            "인문대학", "사회과학대학", "자연과학대학", "간호대학", "경영대학", "공과대학",
            "농업생명과학대학", "미술대학", "사범대학", "생활과학대학", "수의과대학", "약학대학", "음악대학", "의과대학",
            "자유전공학부", "첨단융합학부"
        )
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, departments)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.departmentSpinner.adapter = adapter

        // signupButton 클릭 리스너
        binding.signupButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val realname = binding.nameEditText.text.toString()
            val department = binding.departmentSpinner.selectedItemPosition + 1

            var errorMessage = when {
                username.isEmpty() -> "UserID is required."
                password.isEmpty() -> "Password is required."
                email.isEmpty() -> "Email is required."
                realname.isEmpty() -> "Name is required."
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches() -> "Invalid email format."

                else -> null
            }

            if (!isPasswordValid(password)) {
                errorMessage = "Password is invalid."
            }

            if (errorMessage != null) {
                printErrorMessage(errorMessage)
            } else {
                binding.errorTextView.visibility = View.GONE
                signup(username, email, password, realname, department)
            }
        }


        // loginButton 클릭 리스너
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signup(
        username: String,
        email: String,
        password: String,
        realname: String,
        college: Int
    ) {
        userRepository.signup(
            username,
            email,
            password,
            realname,
            college,
            onSuccess = { response ->
                Toast.makeText(this, "회원가입 성공: ${response.email}", Toast.LENGTH_SHORT).show()
                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            },
            onError = { errorMessage ->
                val parsedErrorMessage = parseErrorDetail(errorMessage)
                printErrorMessage(parsedErrorMessage)
            }
        )
    }

    private fun printErrorMessage(errorMessage: String) {
        binding.errorTextView.text = errorMessage
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun parseErrorDetail(errorMessage: String?): String {
        return try {
            if (errorMessage.isNullOrEmpty()) {
                "알 수 없는 오류가 발생했습니다." // errorMessage가 null이거나 비어있을 경우
            } else {
                // 에러 메시지에서 상태 코드 추출
                val parts = errorMessage.split(":").map { it.trim() } // ":" 기준으로 나눔
                if (parts.size == 2) {
                    val statusCode = parts[1].toIntOrNull() // 상태 코드를 정수로 변환
                    when (statusCode) {
                        400 -> "Invalid field format" // userid 또는 password 형식 오류
                        409 -> "UserID or Email already exists" // ID 또는 이메일 중복
                        else -> "알 수 없는 오류가 발생했습니다." // 정의되지 않은 상태 코드
                    }
                } else {
                    "알 수 없는 오류가 발생했습니다." // 메시지 형식이 올바르지 않을 경우
                }
            }
        } catch (e: Exception) {
            "서버 오류가 발생했습니다. 다시 시도해주세요."
        }
    }

    // 비밀번호 유효성 검사 함수
    private fun isPasswordValid(password: String): Boolean {
        if (password.length !in 8..20) return false // 길이 검증
        if (password.contains(" ")) return false // 공백 포함 여부 검증

        // 영문, 숫자, 특수문자 패턴
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        // 세 가지 중 두 가지 이상 충족해야 함
        val validCriteriaCount = listOf(hasLetter, hasDigit, hasSpecialChar).count { it }
        return validCriteriaCount >= 2
    }
}
