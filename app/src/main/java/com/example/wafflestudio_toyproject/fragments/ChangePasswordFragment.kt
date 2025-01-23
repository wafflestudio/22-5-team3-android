package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.ChangePasswordRequest
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserApi
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.databinding.FragmentChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import javax.inject.Inject
import retrofit2.Response

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        // 비밀번호 변경 버튼 클릭 리스너
        binding.changePasswordButton.setOnClickListener {
            changePassword()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(R.id.action_changePasswordFragment_to_userProfileFragment)
            }
        })
    }
    
    // 비밀번호 변경 api 연결
    private fun changePassword() {
        val currentPassword = binding.currentPassword.text.toString()
        val newPassword = binding.newPassword.text.toString()
        val confirmNewPassword = binding.newPasswordChecking.text.toString()

        // 유효성 검사
        if (validatePasswords(currentPassword, newPassword, confirmNewPassword)) {
            val accessToken = "Bearer ${userRepository.getAccessToken()}"

            val changePasswordRequest = ChangePasswordRequest(
                currentPassword,
                newPassword,
                confirmNewPassword
            )

            userApi.changePassword(accessToken, changePasswordRequest)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        when (response.code()) {
                            200 -> {
                                Toast.makeText(requireContext(), "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                            400 -> {
                                val errorBody = response.errorBody()?.string()
                                val errorMessage = when {
                                    errorBody?.contains("Invalid field format") == true ->
                                        "새 비밀번호 형식이 올바르지 않습니다."
                                    else -> "잘못된 요청입니다."
                                }
                                showPasswordError(errorMessage)
                            }
                            401 -> {
                                val errorBody = response.errorBody()?.string()
                                if (errorBody?.contains("Invalid password") == true) {
                                    showPasswordError("현재 비밀번호가 틀렸습니다.")
                                } else {
                                    showPasswordError("인증에 실패했습니다.")
                                }
                            }
                            else -> {
                                showPasswordError("비밀번호 변경에 실패했습니다.")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        showPasswordError("네트워크 오류: ${t.message}")
                    }
                })
        }
    }
    
    // 비밀번호 empty 검증
    private fun validatePasswords(currentPassword: String, newPassword: String, confirmNewPassword: String): Boolean {
        var isValid = true

        binding.currentPasswordError.visibility = View.GONE
        binding.newPasswordError.visibility = View.GONE
        binding.newPasswordCheckingError.visibility = View.GONE

        // 현재 비밀번호 검증
        if (currentPassword.isEmpty()) {
            binding.currentPasswordError.text = "현재 비밀번호를 입력해주세요."
            binding.currentPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.currentPasswordError.visibility = View.GONE
        }

        // 새 비밀번호 검증
        val passwordPattern = "^(?:(?=.*[A-Za-z])(?=.*\\d)|(?=.*[A-Za-z])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])|(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]))[A-Za-z\\d!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,20}$".toRegex()
        if (newPassword.isEmpty()) {
            binding.newPasswordError.text = "새 비밀번호를 입력해주세요."
            binding.newPasswordError.visibility = View.VISIBLE
            isValid = false
        } else if (!passwordPattern.matches(newPassword)) {
            binding.newPasswordError.text = "영문, 숫자, 특수문자 2종류 이상 조합된 8~20자이어야 합니다."
            binding.newPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.newPasswordError.visibility = View.GONE
        }

        
        // 새 비밀번호 확인 검증
        if (confirmNewPassword.isEmpty()) {
            binding.newPasswordCheckingError.text = "새 비밀번호를 입력해주세요."
            binding.newPasswordCheckingError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.newPasswordCheckingError.visibility = View.GONE
        }

        // 새 비밀번호 != 새 비밀번호 확인
        if (confirmNewPassword != newPassword) {
            binding.newPasswordCheckingError.text = "비밀번호가 일치하지 않습니다."
            binding.newPasswordCheckingError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.newPasswordCheckingError.visibility = View.GONE
        }

        return isValid
    }

    // 오류 메시지 표시
    private fun showPasswordError(message: String) {
        binding.passwordError.text = message
        binding.passwordError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}