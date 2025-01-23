package com.example.wafflestudio_toyproject.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wafflestudio_toyproject.CreateVoteRequest
import com.example.wafflestudio_toyproject.CreateVoteResponse
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.adapter.SelectedImagesAdapter
import com.example.wafflestudio_toyproject.databinding.FragmentCreateVoteBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class CreateVoteFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentCreateVoteBinding? = null
    private val binding get() = _binding!!
    private var selectedImages = mutableListOf<Uri>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

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

        // 날짜 선택
        binding.deadlineDate.setOnClickListener {
            showCustomDatePicker { selectedDate ->
                binding.deadlineDate.text = selectedDate
            }
        }

        // 시간 선택
        binding.deadlineTime.setOnClickListener {
            showCustomTimePicker { selectedTime ->
                binding.deadlineTime.text = selectedTime
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(R.id.action_createVoteFragment_to_ongoingVoteFragment)
            }
        })

        selectedImagesAdapter = SelectedImagesAdapter(
            items = selectedImages,
            onRemoveImage = { uri ->
                selectedImages.remove(uri)
                selectedImagesAdapter.notifyDataSetChanged()
            },

            onAddImageClick = {
                openImagePicker() // 이미지 선택
            }
        )

        binding.postImage.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) {
                    val count = data.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        data.clipData?.getItemAt(i)?.uri?.let { uri ->
                            selectedImages.add(uri)
                        }
                    }
                } else if (data?.data != null) {
                    data.data?.let { uri ->
                        selectedImages.add(uri)
                    }
                }

                if (selectedImages.isNotEmpty()) {
                    displaySelectedImages()
                } else {
                    Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        val createVoteJson = CreateVoteRequest(
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

        val jsonRequestBody = Gson().toJson(createVoteJson).toRequestBody("application/json".toMediaType())

        val imageParts = selectedImages.mapNotNull { uri ->
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("images", tempFile.name, requestBody)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        val call = if (imageParts != null) {
            voteApi.createVoteWithImage(jsonRequestBody, imageParts)
        } else {
            voteApi.createVoteWithoutImage(jsonRequestBody)
        }

        call.enqueue(object : Callback<CreateVoteResponse> {
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

    // 날짜 선택 다이얼로그
    private fun showCustomDatePicker(onDateSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_date_picker, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Date")
            .create()

        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val dayPicker = dialogView.findViewById<NumberPicker>(R.id.dayPicker)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.add(Calendar.DAY_OF_YEAR, 30)
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
            calendar.set(selectedYear, selectedMonth - 1, 1)
            val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            dayPicker.minValue = if (selectedYear == currentYear && selectedMonth == currentMonth) currentDay else 1
            dayPicker.maxValue = if (selectedYear == maxYear && selectedMonth == maxMonth) maxDay else maxDaysInMonth
        }
        updateDayPicker()

        yearPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val selectedDate = String.format(
                "%04d-%02d-%02d",
                yearPicker.value,
                monthPicker.value,
                dayPicker.value
            )
            onDateSelected(selectedDate)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 시간 선택 다이얼로그
    private fun showCustomTimePicker(onTimeSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_time_picker, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Time")
            .create()

        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)

        // Configure Hour Picker
        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        // Configure Minute Picker
        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val selectedTime = String.format(
                "%02d:%02d",
                hourPicker.value,
                minutePicker.value
            )
            onTimeSelected(selectedTime)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 이미지 선택
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 다중 선택 허용
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun displaySelectedImages() {
        if (selectedImages.isNotEmpty()) {
            binding.postImage.visibility = View.VISIBLE
            selectedImagesAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}