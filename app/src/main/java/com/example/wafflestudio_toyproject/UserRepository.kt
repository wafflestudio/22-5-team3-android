package com.example.wafflestudio_toyproject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: UserApi) {
    fun signup(
        username: String,
        email: String,
        password: String,
        onSuccess: (SignupResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = SignupRequest(userid = username, email = email, password = password)

        api.signup(request).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("응답이 비어 있습니다.")
                } else {
                    onError("회원가입 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                onError("네트워크 에러: ${t.message}")
            }
        })
    }
}