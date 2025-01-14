package com.example.wafflestudio_toyproject.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wafflestudio_toyproject.CommentRequest

import com.example.wafflestudio_toyproject.ParticipationRequest

import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.VoteDetailResponse
import com.example.wafflestudio_toyproject.adapter.CommentItemAdapter
import com.example.wafflestudio_toyproject.databinding.FragmentVoteDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class VoteDetailFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentVoteDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var voteApi: VoteApi

    @Inject
    lateinit var userRepository: UserRepository

    private var voteId: Int = -1
    private lateinit var commentAdapter: CommentItemAdapter
    private val comments = mutableListOf<VoteDetailResponse.Comment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteDetailBinding.inflate(inflater, container, false)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            navController.navigate(R.id.action_voteDetailFragment_to_ongoingVoteFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        // 전달된 vote_id 가져오기
        arguments?.let {
            voteId = it.getInt("vote_id", -1)
        }

        if (voteId != -1) {
            fetchVoteDetails(voteId) // API 호출
        } else {
            Toast.makeText(requireContext(), "Invalid vote ID", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(R.id.action_voteDetailFragment_to_ongoingVoteFragment)
            }
        })

        setupCommentRecyclerView()
    }

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentItemAdapter(comments) { comment ->
            onEditCommentClicked(comment) // 클릭 이벤트 처리
        }
        binding.commentRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchVoteDetails(voteId: Int) {
        val accessToken = userRepository.getAccessToken()

        voteApi.getVoteDetails(voteId, "Bearer $accessToken")
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(
                    call: Call<VoteDetailResponse>,
                    response: Response<VoteDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { voteDetail ->
                            displayVoteDetails(voteDetail)
                            loadComments(voteDetail.comments)
                            Log.d("VoteDetailFragment", "Response body: ${response.body()}")
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch vote details: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadComments(newComments: List<VoteDetailResponse.Comment>) {
        comments.clear()
        comments.addAll(newComments)
        commentAdapter.notifyDataSetChanged()
    }

    private fun displayVoteDetails(voteDetail: VoteDetailResponse) {
        binding.voteDetailTitle.text = voteDetail.title
        binding.voteDetailDescription.text = voteDetail.content
        binding.userId.text = voteDetail.writer_name

        if (voteDetail.multiple_choice) {
            binding.multipleChoiceMessage.text = " · 중복 선택 가능"
        } else {
            binding.multipleChoiceMessage.text = " · 중복 선택 불가능"
        }

        if (voteDetail.annonymous_choice) {
            binding.anonymousMessage.text = " · 익명 투표"
        } else {
            binding.anonymousMessage.text = " · 기명 투표"
        }

        if (voteDetail.realtime_result) {
            binding.realtimeMessage.text = "실시간 결과 공개"
        } else {
            binding.realtimeMessage.text = "실시간 결과 비공개"
        }

        if (voteDetail.participation_code_required) {
            binding.participationcodeMessage.text = " · 참여코드 필요"
        } else {
            binding.participationcodeMessage.text = " · 참여코드 불필요"
        }

        val hasParticipated = voteDetail.choices.any { it.participated }

        if (hasParticipated) { // 투표 여부를 서버에서 반환하는 경우
            binding.voteButton.text = "다시 투표하기"
        } else {
            binding.voteButton.text = "투표하기"
        }

        val totalParticipants = voteDetail.choices.sumOf { it.choice_num_participants ?: 0 }
        binding.participantCount.text = "${totalParticipants}명 참여"
        if (hasParticipated)
            binding.participantCount.visibility = View.VISIBLE

        // 참여자 목록으로 이동
        binding.participantCount.setOnClickListener {
            val bundle = Bundle().apply {
                val choicesBundle = ArrayList<Bundle>()
                voteDetail.choices.forEach { choice ->
                    val choiceBundle = Bundle().apply {
                        putInt("choice_id", choice.choice_id)
                        putString("choice_content", choice.choice_content)
                        putBoolean("participated", choice.participated)
                        choice.choice_num_participants?.let { putInt("choice_num_participants", it) }
                        putStringArrayList(
                            "choice_participants_name",
                            ArrayList(choice.choice_participants_name ?: emptyList())
                        )
                    }
                    choicesBundle.add(choiceBundle)
                }
                putParcelableArrayList("choices", choicesBundle)

                putInt("vote_id", voteId)
            }

            navController.navigate(
                R.id.action_voteDetailFragment_to_voteParticipantsDetailFragment,
                bundle
            )
        }
        startTrackingTime(voteDetail)

        // 기존 선택지 제거 (중복 방지)
        binding.choicesContainer.removeAllViews()

        // 투표 선택지 표시하기
        val selectedChoices = mutableSetOf<Int>() // 선택된 choice_id 저장

        voteDetail.choices.forEach { choice ->
            // 선택지 UI 생성
            val choiceLayout = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_choice_button, binding.choicesContainer, false)

            val choiceText = choiceLayout.findViewById<TextView>(R.id.choiceText)
            val checkCircle = choiceLayout.findViewById<FrameLayout>(R.id.checkCircle)
            val checkIcon = choiceLayout.findViewById<ImageView>(R.id.checkIcon)

            // 선택지 텍스트 표시
            choiceText.text = choice.choice_content

            checkCircle.isSelected = choice.participated
            checkIcon.setColorFilter(
                if (choice.participated) resources.getColor(R.color.selected_icon_color, null)
                else resources.getColor(R.color.unselected_icon_color, null)
            )
            if (hasParticipated) {
                updateColorBar(voteDetail)
            }

            // 클릭 이벤트 처리
            checkCircle.setOnClickListener {
                if (voteDetail.multiple_choice) {
                    // 다중 선택
                    if (selectedChoices.contains(choice.choice_id)) {
                        selectedChoices.remove(choice.choice_id)
                        checkCircle.isSelected = false
                        checkIcon.setColorFilter(
                            resources.getColor(R.color.unselected_icon_color, null)
                        )
                    } else {
                        selectedChoices.add(choice.choice_id)
                        checkCircle.isSelected = true
                        checkIcon.setColorFilter(
                            resources.getColor(R.color.selected_icon_color, null)
                        )
                    }
                } else {
                    // 단일 선택
                    selectedChoices.clear()
                    selectedChoices.add(choice.choice_id)

                    // 모든 선택지 상태 초기화
                    binding.choicesContainer.forEach { child ->
                        if (child is ConstraintLayout) {
                            val childCheckCircle = child.findViewById<FrameLayout>(R.id.checkCircle)
                            val childCheckIcon = child.findViewById<ImageView>(R.id.checkIcon)
                            childCheckCircle.isSelected = false
                            childCheckIcon.setColorFilter(
                                resources.getColor(R.color.unselected_icon_color, null)
                            )
                        }
                    }

                    // 현재 선택된 항목만 활성화
                    checkCircle.isSelected = true
                    checkIcon.setColorFilter(
                        resources.getColor(R.color.selected_icon_color, null)
                    )
                }
            }

            // 선택지를 컨테이너에 추가
            binding.choicesContainer.addView(choiceLayout)
        }

        fun showParticipationCodeDialog(onCodeEntered: (String) -> Unit) {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_participation_code, null)
            val codeInput = dialogView.findViewById<EditText>(R.id.participationCodeInput)

            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ ->
                    val enteredCode = codeInput.text.toString()
                    if (enteredCode.isNotEmpty()) {
                        onCodeEntered(enteredCode)
                    } else {
                        binding.errorTextView.text = "참여 코드를 입력하세요."
                        binding.errorTextView.visibility = View.VISIBLE
                    }
                }
                .create()
                .show()
        }

        fun performVote(enteredCode: String?) {
            val accessToken = userRepository.getAccessToken()
            val participationRequest = ParticipationRequest(
                participated_choice_ids = selectedChoices.toList(),
                participation_code = enteredCode
            )

            voteApi.participateInVote(voteId, "Bearer $accessToken", participationRequest)
                .enqueue(object : Callback<VoteDetailResponse> {
                    override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "투표에 성공적으로 참여했습니다!", Toast.LENGTH_SHORT).show()
                            binding.errorTextView.visibility = View.GONE
                            response.body()?.let { updatedVoteDetail ->
                                updateColorBar(updatedVoteDetail) // 선택지 배경 색칠
                            }
                            fetchVoteDetails(voteId)
                            binding.voteButton.text = "다시 투표하기"
                        } else {
                            if(response.message() == "Forbidden") {
                                binding.errorTextView.text = "참여 코드가 틀렸습니다."
                                binding.errorTextView.visibility = View.VISIBLE
                            }
                            //Toast.makeText(requireContext(), "투표 참여 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // voteButton 클릭 이벤트 추가
        binding.voteButton.setOnClickListener {
            if (selectedChoices.toList().isEmpty()){
                binding.errorTextView.text = "투표 항목을 선택해주세요."
                binding.errorTextView.visibility = View.VISIBLE
            } else {
                if (voteDetail.participation_code_required) {
                    showParticipationCodeDialog { enteredCode ->
                        performVote(enteredCode)
                    }
                } else {
                    performVote(null) // 참여 코드가 필요 없는 경우 바로 투표 진행
                }
            }
        }

        binding.postCommentButton.setOnClickListener {
            val content = binding.commentEditText.text.toString()
            val token = userRepository.getAccessToken()

            if (content.isBlank()) {
                Toast.makeText(context, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 수정 작업 처리
            if (editingCommentId != null) {
                val commentId = editingCommentId!!
                editingCommentId = null // 상태 초기화
                editComment(voteId, commentId, content, token!!)
                binding.postCommentButton.text = "게시" // 버튼 텍스트 복구
            } else {
                // 새로운 댓글 게시
                postComment(voteId, content, token!!)
            }

            binding.commentEditText.text.clear() // 입력 필드 초기화
        }

    }

    // 댓글 게시
    private fun postComment(voteId: Int, content: String, token: String) {
        val commentRequest = CommentRequest(content)

        voteApi.postComment(voteId, "Bearer $token", commentRequest)
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "댓글이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "댓글 추가에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    Toast.makeText(context, "네트워크 오류로 댓글 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 댓글 수정
    private fun editComment(voteId: Int, commentId: Int, updatedContent: String, token: String) {
        val commentRequest = CommentRequest(updatedContent)

        voteApi.updateComment(voteId, commentId, "Bearer $token", commentRequest)
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "댓글이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        fetchVoteDetails(voteId) // 수정된 댓글 업데이트
                    } else {
                        Toast.makeText(context, "댓글 수정에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    Toast.makeText(context, "네트워크 오류로 댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private var editingCommentId: Int? = null // 현재 수정 중인 댓글 ID 저장

    fun onEditCommentClicked(comment: VoteDetailResponse.Comment) {
        // 댓글 수정 시작
        Log.d("edit", "commendt id: ${comment.comment_id}")
        editingCommentId = comment.comment_id
        binding.commentEditText.setText(comment.comment_content) // 기존 댓글 복사
        binding.postCommentButton.text = "수정" // 버튼 텍스트 변경
    }
    
    // 투표 선택지 색칠
    private fun updateColorBar(voteDetail: VoteDetailResponse) {
        // 뷰의 레이아웃이 완료된 후에 실행
        binding.choicesContainer.post {
            val totalParticipants = voteDetail.choices.sumOf { it.choice_num_participants ?: 0 }
            val parentWidth = binding.choicesContainer.width // 레이아웃 완료 후 너비 가져오기

            binding.choicesContainer.forEachIndexed { index, view ->
                if (view is ConstraintLayout) {
                    val colorBar = view.findViewById<View>(R.id.colorBar)
                    val choice = voteDetail.choices.getOrNull(index)

                    if (choice != null) {
                        val participants = choice.choice_num_participants ?: 0
                        val ratio = if (totalParticipants > 0) participants.toFloat() / totalParticipants else 0f

                        // 비율에 따라 막대 너비 계산
                        val barWidth = (parentWidth * ratio).toInt()
                        Log.d("colorBar", "bar width: ${barWidth}")

                        // 막대 표시 여부와 너비 설정
                        if (participants > 0 && ratio > 0) {
                            val layoutParams = colorBar.layoutParams
                            layoutParams.width = barWidth
                            colorBar.layoutParams = layoutParams

                            colorBar.visibility = View.VISIBLE // 참여자가 있는 경우 표시
                        } else {
                            colorBar.visibility = View.GONE // 참여자가 없는 경우 숨김
                        }
                    }
                }
            }
        }
    }

    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private fun startTrackingTime(voteDetail: VoteDetailResponse) {
        runnable = Runnable {
            val timeRemaining = voteDetail.calculateTimeRemaining()
            binding.voteTimeRemaining.text = timeRemaining

            // 투표 종료 상태 확인
            val isVoteClosed = timeRemaining == "종료됨"
            binding.voteButton.isEnabled = !isVoteClosed

            // 종료 시 오류 메시지
            if (isVoteClosed) {
                binding.errorTextView.text = "투표가 종료되었습니다."
                binding.errorTextView.visibility = View.VISIBLE
            }

            // 1초마다 업데이트
            if (!isVoteClosed) {
                handler.postDelayed(runnable, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun stopTrackingTime() {
        handler.removeCallbacks(runnable)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopTrackingTime()
        _binding = null
    }
}