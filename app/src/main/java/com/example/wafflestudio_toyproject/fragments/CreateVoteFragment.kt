package com.example.wafflestudio_toyproject.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.CreateVoteRequest
import com.example.wafflestudio_toyproject.CreateVoteResponse
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.databinding.FragmentCreateVoteBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import android.app.TimePickerDialog
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.NumberPicker
import java.util.Calendar


@AndroidEntryPoint
class CreateVoteFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentCreateVoteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var voteApi: VoteApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVoteBinding.inflate(inflater, container, false)

        // 투표 항목 추가 생성
        binding.buttonAddOption.setOnClickListener {
            addOption()
        }

        // 참여 코드 입력창 활성화
        binding.checkboxCreateParticipationCode.setOnCheckedChangeListener { _, isChecked ->
            binding.participationCodeInput.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // 투표글 생성
        binding.buttonSubmitVote.setOnClickListener {
            createVote()
        }

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            navController.navigate(R.id.action_createVoteFragment_to_ongoingVoteFragment)
        }

        // 날짜 및 시간 선택
        binding.deadlineDate.setOnClickListener {
            showCustomDateTimePicker { selectedDateTime ->
                val (date, time) = selectedDateTime.split(" ")
                binding.deadlineDate.text = date
                binding.deadlineTime.text = time
            }
        }

        binding.deadlineTime.setOnClickListener {
            showCustomDateTimePicker { selectedDateTime ->
                val (date, time) = selectedDateTime.split(" ")
                binding.deadlineDate.text = date
                binding.deadlineTime.text = time
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
    }
    
    // 투표 항목 추가
    private fun addOption() {
        val newOption = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "New Option"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        binding.optionsContainer.addView(newOption)
    }
    
    // 투표글 생성
    private fun createVote() {
        val title = binding.voteTitle.text.toString().trim()
        val content = binding.voteDescription.text.toString().trim()
        val participationCodeRequired = binding.checkboxCreateParticipationCode.isChecked
        val participationCode = if (participationCodeRequired) binding.participationCodeInput.text.toString() else null
        val realtimeResult = binding.checkboxRevealResults.isChecked
        val multipleChoice = binding.checkboxAllowDuplicates.isChecked
        val annonymousChoice = binding.checkboxAnonymous.isChecked
        val endDatetime = "${binding.deadlineDate.text}T${binding.deadlineTime.text}"
        val choices = (0 until binding.optionsContainer.childCount).mapNotNull { index ->
            val view = binding.optionsContainer.getChildAt(index) as? EditText
            view?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }

        // 입력값 검증
        val errorMessage = when {
            title.isEmpty() || title.length > 100 -> "Title must be between 1 and 100 characters."
            content.isEmpty() || content.length > 200 -> "Content must be between 1 and 200 characters."
            participationCodeRequired && (participationCode.isNullOrEmpty() || participationCode.length != 6) -> "Participation code must be 6 characters."
            choices.isEmpty() -> "At least one choice is required."
            binding.deadlineDate.text.isNullOrEmpty() || binding.deadlineTime.text.isNullOrEmpty() -> "Date and Time must not be empty."
            else -> null
        }

        if (errorMessage != null) {
            showError(errorMessage)
            return
        }

        // Prepare API request
        val request = CreateVoteRequest(
            title = title,
            content = content,
            participation_code_required = participationCodeRequired,
            participation_code = participationCode,
            realtime_result = realtimeResult,
            multiple_choice = multipleChoice,
            annonymous_choice = annonymousChoice,
            end_datetime = endDatetime,
            choices = choices
        )

        voteApi.createVote(request).enqueue(object : Callback<CreateVoteResponse> {
            override fun onResponse(
                call: Call<CreateVoteResponse>,
                response: Response<CreateVoteResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Vote created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    showError("Failed to create vote: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CreateVoteResponse>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
        binding.errorTextView.requestLayout()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // 날짜 시간 선택
    private fun showCustomDateTimePicker(onDateTimeSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_date_time_picker, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // NumberPickers
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val dayPicker = dialogView.findViewById<NumberPicker>(R.id.dayPicker)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)

        // 현재 날짜와 14일 후 날짜
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month is 0-indexed
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val maxYear = calendar.get(Calendar.YEAR)
        val maxMonth = calendar.get(Calendar.MONTH) + 1
        val maxDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Configure Year Picker
        yearPicker.minValue = currentYear
        yearPicker.maxValue = maxYear
        yearPicker.value = currentYear

        // Configure Month Picker
        monthPicker.minValue = currentMonth
        monthPicker.maxValue = if (currentYear == maxYear) maxMonth else 12
        monthPicker.value = currentMonth

        // Configure Day Picker
        val updateDayPicker = {
            val selectedYear = yearPicker.value
            val selectedMonth = monthPicker.value
            calendar.set(selectedYear, selectedMonth - 1, 1) // Set to the first day of the selected month
            val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Adjust min and max day range
            dayPicker.minValue = if (selectedYear == currentYear && selectedMonth == currentMonth) currentDay else 1
            dayPicker.maxValue = if (selectedYear == maxYear && selectedMonth == maxMonth) maxDay else maxDaysInMonth
        }

        updateDayPicker()

        // Add listeners to update Day Picker dynamically
        yearPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }

        // Configure Hour Picker
        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        // Configure Minute Picker
        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        // Buttons
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            val selectedDateTime = String.format(
                "%04d-%02d-%02d %02d:%02d",
                yearPicker.value,
                monthPicker.value,
                dayPicker.value,
                hourPicker.value,
                minutePicker.value
            )
            onDateTimeSelected(selectedDateTime)
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}