package com.example.wafflestudio_toyproject
import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wafflestudio_toyproject.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        val departments = listOf("인문대학", "사회과학대학", "자연과학대학", "간호대학", "경영대학", "공과대학",
            "농업생명과학대학", "미술대학", "사범대학", "생활과학대학", "수의과대학", "약학대학", "음악대학", "의과대학",
            "자유전공학부", "첨단융합학부")
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

            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()  && realname.isNotEmpty()) {
                signup(username, email, password, realname, department)
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signup(username: String, email: String, password: String, realname: String, department: Int) {
        userRepository.signup(
            username,
            email,
            password,
            realname,
            department,
            onSuccess = { response ->
                Toast.makeText(this, "회원가입 성공: ${response.email}", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://52.78.27.95/")
            .addConverterFactory(GsonConverterFactory.create()) // JSON 변환
            .client(OkHttpClient.Builder().build()) // 클라이언트 설정
            .build()
    }
}