package com.example.wafflestudio_toyproject.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.wafflestudio_toyproject.CommentRequest

import com.example.wafflestudio_toyproject.ParticipationRequest

import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.VoteDetailResponse
import com.example.wafflestudio_toyproject.VoteDetailViewModel
import com.example.wafflestudio_toyproject.adapter.CommentItemAdapter
import com.example.wafflestudio_toyproject.adapter.ImageSliderAdapter
import com.example.wafflestudio_toyproject.databinding.FragmentVoteDetailBinding
import com.google.android.material.tabs.TabLayoutMediator
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

    private val viewModel: VoteDetailViewModel by viewModels()

    private var voteId: Int = -1
    private lateinit var commentAdapter: CommentItemAdapter
    private val comments = mutableListOf<VoteDetailResponse.Comment>()

    private var editingCommentId: Int? = null // 현재 수정 중인 댓글 ID 저장
    private var originalCommentContent: String? = null // 원본 댓글 내용 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteDetailBinding.inflate(inflater, container, false)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            val origin = arguments?.getString("origin")
            Log.d("VoteDetailFragment", "From $origin")
            //무한루프 방지
            while (navController.previousBackStackEntry?.destination?.id == R.id.voteParticipantsDetailFragment) {
                navController.popBackStack(R.id.voteParticipantsDetailFragment, true)
            }

            when (origin) {
                "hotVote" -> navController.popBackStack(R.id.hotVoteFragment, true) // 핫 투표에서 온 경우
                "ongoingVote" -> navController.popBackStack(R.id.ongoingVoteFragment, true) // 진행 중인 투표에서 온 경우
                else -> navController.navigateUp()
            }
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

        viewModel.voteDetails.observe(viewLifecycleOwner) { result ->
            Log.d("VoteDetailFragment", "Received vote details: $result")
            result.onSuccess { voteDetail ->
                displayVoteDetails(voteDetail)
                loadComments(voteDetail.comments)
                Log.d("VoteDetailFragment", "Response body: $voteDetail")
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.voteResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { updatedVoteDetail ->
                val hasParticipated = updatedVoteDetail.choices.any { it.participated }

                if (hasParticipated) {
                    Toast.makeText(requireContext(), "투표에 성공적으로 참여했습니다!", Toast.LENGTH_SHORT).show()
                    binding.voteButton.text = "다시 투표하기"
                } else {
                    Toast.makeText(requireContext(), "투표가 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    binding.voteButton.text = "투표하기"
                }
                Toast.makeText(requireContext(), "투표에 성공적으로 참여했습니다!", Toast.LENGTH_SHORT).show()

                binding.errorTextView.visibility = View.GONE
                updateColorBar(updatedVoteDetail) // 선택지 배경 색칠
                fetchVoteDetails(voteId) // UI 업데이트
                binding.voteButton.text = "다시 투표하기"
            }.onFailure { error ->
                if (error.message == "Forbidden") {
                    binding.errorTextView.text = "참여 코드가 틀렸습니다."
                    binding.errorTextView.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "투표 참여 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.selectedChoices.observe(viewLifecycleOwner) { selectedChoices ->
            updateChoiceUI(selectedChoices)
        }

        viewModel.commentResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { updatedVoteDetail ->
                Toast.makeText(requireContext(), "댓글이 성공적으로 반영되었습니다.", Toast.LENGTH_SHORT).show()
                fetchVoteDetails(voteId) // UI 갱신
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val origin = arguments?.getString("origin")
                Log.d("VoteDetailFragment", "From $origin")

                while (navController.previousBackStackEntry?.destination?.id == R.id.voteParticipantsDetailFragment) {
                    navController.popBackStack(R.id.voteParticipantsDetailFragment, true)
                }

                when (origin) {
                    "hotVote" -> navController.popBackStack(R.id.hotVoteFragment, true) // 핫 투표에서 온 경우
                    "ongoingVote" -> navController.popBackStack(R.id.ongoingVoteFragment, true) // 진행 중인 투표에서 온 경우
                    else -> navController.navigateUp()
                }
            }
        })

        setupCommentRecyclerView()
        setupCommentEditTextListener()

        // 화면 새로고침
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchVoteDetails(voteId) // 데이터 새로고침
            binding.swipeRefreshLayout.isRefreshing = false // 새로고침 완료 후 로딩 종료
        }
    }

    private fun setupImageSlider(imageUrls: List<String>) {
        Log.d("VoteDetailFragment", "Setting up Image Slider with URLs: $imageUrls")
        binding.postImage.apply {
            adapter = ImageSliderAdapter(imageUrls) // 어댑터 연결
            orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 스와이프 설정
        }
        binding.indicator.setViewPager(binding.postImage)
        Log.d("CircleIndicator3", "Indicator child count: ${binding.indicator.childCount}")
    }

    private fun fetchVoteDetails(voteId: Int) {
        val accessToken = userRepository.getAccessToken()
        viewModel.fetchVoteDetails(voteId, accessToken!!)
    }

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentItemAdapter(
            comments = comments,
            onEditComment = { comment -> onEditCommentClicked(comment) }, // 수정 이벤트 처리
            onDeleteComment = { comment -> onDeleteCommentClicked(comment) } // 삭제 이벤트 처리
        )
        binding.commentRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
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

        if (voteDetail.images.isNotEmpty()) {
            setupImageSlider(voteDetail.images)
        } else {
            binding.postImage.visibility = View.GONE
        }

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

        binding.participantCount.text = "${voteDetail.participant_count}명 참여"
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

        viewModel.setInitialChoices(voteDetail.choices)
        // 기존 선택지 제거 (중복 방지)
        binding.choicesContainer.removeAllViews()

        if (voteDetail.realtime_result) {
            Log.d("updateColorBar", "Updating color bar in displayVoteDetails")
            updateColorBar(voteDetail)
        }

        // 투표 선택지 표시하기
        voteDetail.choices.forEach { choice ->
            val choiceLayout = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_choice_button, binding.choicesContainer, false)

            val choiceText = choiceLayout.findViewById<TextView>(R.id.choiceText)
            val checkCircle = choiceLayout.findViewById<FrameLayout>(R.id.checkCircle)
            val checkIcon = choiceLayout.findViewById<ImageView>(R.id.checkIcon)

            choiceLayout.tag = choice.choice_id // 선택지 ID 저장
            choiceText.text = choice.choice_content

            // 선택 상태 UI 반영
            viewModel.selectedChoices.observe(viewLifecycleOwner) { selectedChoices ->
                val isSelected = selectedChoices.contains(choice.choice_id)
                checkCircle.isSelected = isSelected
                checkIcon.setColorFilter(
                    if (isSelected) resources.getColor(R.color.selected_icon_color, null)
                    else resources.getColor(R.color.unselected_icon_color, null)
                )
            }

            // 클릭 이벤트 → ViewModel에 선택 상태 변경 요청
            checkCircle.setOnClickListener {
                viewModel.toggleChoice(choice.choice_id, voteDetail.multiple_choice)
            }

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

        // voteButton 클릭 이벤트 추가
        viewModel.selectedChoices.observe(viewLifecycleOwner) { selectedChoices ->
            val hasParticipated = viewModel.hasParticipated.value ?: false
            binding.voteButton.setOnClickListener {
                val accessToken = userRepository.getAccessToken() ?: return@setOnClickListener

                if (selectedChoices.isEmpty()) {
                    if (hasParticipated) {
                        AlertDialog.Builder(requireContext())
                            .setMessage("투표 참여를 취소하시겠습니까?")
                            .setPositiveButton("예") { _, _ ->
                                if (voteDetail.participation_code_required) {
                                    showParticipationCodeDialog { enteredCode ->
                                        viewModel.performVote(voteId, accessToken, emptyList(), enteredCode)
                                    }
                                } else {
                                    viewModel.performVote(voteId, accessToken, emptyList(), null)
                                }
                            }
                            .setNegativeButton("아니요", null)
                            .create()
                            .show()
                    } else {
                        binding.errorTextView.text = "투표 항목을 선택해주세요."
                        binding.errorTextView.visibility = View.VISIBLE
                    }
                } else {
                    binding.errorTextView.visibility = View.GONE
                    binding.voteButton.setOnClickListener {
                        if (selectedChoices.isEmpty()) {
                            binding.errorTextView.text = "투표 항목을 선택해주세요."
                            binding.errorTextView.visibility = View.VISIBLE
                        } else {
                            binding.errorTextView.visibility = View.GONE
                            val accessToken = userRepository.getAccessToken() ?: return@setOnClickListener
                            if (voteDetail.participation_code_required) {
                                showParticipationCodeDialog { enteredCode ->
                                    viewModel.performVote(voteId, accessToken, selectedChoices.toList(), enteredCode)
                                }
                            } else {
                                viewModel.performVote(voteId, accessToken, selectedChoices.toList(), null)
                            }
                        }
                    }
                }
            }
        }

        binding.postCommentButton.setOnClickListener {
            val content = binding.commentEditText.text.toString()
            val token = userRepository.getAccessToken()

            // 수정 버튼 비활성화
            if (!binding.postCommentButton.isEnabled) {
                return@setOnClickListener
            }

            // 공백 댓글 처리
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

    private fun updateChoiceUI(selectedChoices: Set<Int>) {
        binding.choicesContainer.forEach { child ->
            if (child is ConstraintLayout) {
                val checkCircle = child.findViewById<FrameLayout>(R.id.checkCircle)
                val checkIcon = child.findViewById<ImageView>(R.id.checkIcon)
                val choiceId = child.tag as? Int ?: return@forEach

                val isSelected = selectedChoices.contains(choiceId)
                checkCircle.isSelected = isSelected
                checkIcon.setColorFilter(
                    if (isSelected) resources.getColor(R.color.selected_icon_color, null)
                    else resources.getColor(R.color.unselected_icon_color, null)
                )
            }
        }
    }

    // 댓글 게시
    private fun postComment(voteId: Int, content: String, token: String) {
        val content = binding.commentEditText.text.toString()
        val token = userRepository.getAccessToken()

        if (content.isBlank()) {
            Toast.makeText(context, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingCommentId != null) {
            val commentId = editingCommentId!!
            editingCommentId = null // 상태 초기화
            viewModel.editComment(voteId, commentId, content, token!!)
            binding.postCommentButton.text = "게시"
        } else {
            viewModel.postComment(voteId, content, token!!)
        }

        binding.commentEditText.text.clear()
    }

    // 댓글 수정
    private fun editComment(voteId: Int, commentId: Int, updatedContent: String, token: String) {
        val content = binding.commentEditText.text.toString()
        val token = userRepository.getAccessToken()

        if (content.isBlank()) {
            Toast.makeText(context, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.editComment(voteId, commentId, content, token!!)
        binding.commentEditText.text.clear()
        binding.postCommentButton.text = "게시"
    }

    fun onEditCommentClicked(comment: VoteDetailResponse.Comment) {
        editingCommentId = comment.comment_id
        originalCommentContent = comment.comment_content // 원본 댓글 저장

        binding.commentEditText.setText(comment.comment_content) // 기존 댓글 복사
        binding.postCommentButton.text = "수정" // 버튼 텍스트 변경

        // 버튼 초기 상태 설정
        updatePostCommentButtonState()
    }

    // 텍스트 변경 리스너 추가
    private fun setupCommentEditTextListener() {
        binding.commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePostCommentButtonState() // 텍스트 변경 시 버튼 상태 업데이트
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 버튼 활성화 상태 업데이트
    private fun updatePostCommentButtonState() {
        val currentText = binding.commentEditText.text.toString()
        val isModified = currentText != originalCommentContent

        binding.postCommentButton.isEnabled = isModified
        binding.postCommentButton.setBackgroundColor(
            if (isModified) resources.getColor(R.color.primaryColor, null)
            else resources.getColor(R.color.unselected_color, null) // 회색으로 표시
        )
    }

    fun onDeleteCommentClicked(comment: VoteDetailResponse.Comment) {
        val commentId = comment.comment_id
        val token = userRepository.getAccessToken()

        // 댓글 삭제 창
        AlertDialog.Builder(requireContext())
            .setMessage("댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteComment(voteId, commentId, token!!)
            }
            .setNegativeButton("취소", null)
            .create()
            .show()
    }

    // 투표 선택지 색칠
    private fun updateColorBar(voteDetail: VoteDetailResponse) {
        // 뷰의 레이아웃이 완료된 후에 실행
        binding.choicesContainer.post {
            val totalParticipants = voteDetail.participant_count
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