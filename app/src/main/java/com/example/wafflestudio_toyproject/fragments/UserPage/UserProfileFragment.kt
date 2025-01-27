package com.example.wafflestudio_toyproject.fragments.UserPage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.network.GetMeResponse
import com.example.wafflestudio_toyproject.LoginActivity
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.network.UserApi
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.databinding.FragmentUserProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import androidx.appcompat.app.AlertDialog


@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        // 로그아웃 버튼 클릭 리스너 설정
        binding.logoutButton.setOnClickListener {
            logout()
        }

        // 비밀번호 변경 버튼 클릭 리스너
        binding.passwordChangeButton.setOnClickListener {
            navController.navigate(R.id.action_userProfileFragment_to_changePasswordFragment)
        }

        // 내가 만든 투표 클릭 리스너
        binding.createdVotes.setOnClickListener {
            navController.navigate(R.id.action_userProfileFragment_to_myCreatedVotesFragment)
        }
        
        // 내가 참여한 투표 클릭 리스너
        binding.participatedVotes.setOnClickListener {
            navController.navigate(R.id.action_userProfileFragment_to_myParticipatedVotesFragment)
        }
        
        // 회원 탈퇴 클릭 리스너
        binding.withdrawButton.setOnClickListener {
            showDeleteAccountDialog()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        getUserInformation()
    }

    private fun logout() {
        // SharedPreferences에서 토큰 삭제
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // 로그아웃 성공 메시지 표시
        Toast.makeText(requireContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    
    // 회원 정보 불러오기
    private fun getUserInformation() {
        val colleges = listOf(
            "인문대학", "사회과학대학", "자연과학대학", "간호대학", "경영대학", "공과대학",
            "농업생명과학대학", "미술대학", "사범대학", "생활과학대학", "수의과대학", "약학대학", "음악대학", "의과대학",
            "자유전공학부", "첨단융합학부"
        )

        // getMe API 호출
        userApi.getMe().enqueue(object : Callback<GetMeResponse> {
            override fun onResponse(call: Call<GetMeResponse>, response: Response<GetMeResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { userInfo ->
                        binding.userName.text = userInfo.name
                        binding.userID.text = userInfo.userid
                        binding.userEmail.text = userInfo.email
                        binding.userCollege.text = colleges[if (userInfo.college > 0) userInfo.college - 1 else 0]

                        Log.d("getme", "name: ${userInfo.name}, id: ${userInfo.userid}, email: ${userInfo.email}, college: ${userInfo.college}")
                    }
                } else {
                    Toast.makeText(requireContext(), "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetMeResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 에러: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 회원 탈퇴 다이얼로그
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("회원 탈퇴")
            setMessage("회원 탈퇴하시겠습니까?")
            setPositiveButton("탈퇴") { _, _ ->
                deleteAccount()
            }
            setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }
        
    // 회원 탈퇴
    private fun deleteAccount() {
        userRepository.deleteAccount(
            onSuccess = {
                Toast.makeText(requireContext(), "회원 탈퇴 성공", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            },
            onError = {
                Toast.makeText(requireContext(), "회원 탈퇴 실패: $it", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
