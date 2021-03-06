package com.example.exampleretrofit.retrofit

import android.util.Log
import com.example.exampleretrofit.model.Photo
import com.example.exampleretrofit.model.User
import com.example.exampleretrofit.utils.API.BASE_URL
import com.example.exampleretrofit.utils.Constants.TAG
import com.example.exampleretrofit.utils.RESPONSE_STATUS
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat

class RetrofitManager {
    companion object {
        val instance = RetrofitManager()
    }

    // 레트로핏 인터페이스 가져오기
    private val iRetrofit: IRetrofit? = RetrofitClient.getClient(BASE_URL)?.create(IRetrofit::class.java)

    // 사진 검색 api 호출
    fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATUS, ArrayList<Photo>?) -> Unit) {

        val term = searchTerm ?: ""

        val call: Call<JsonElement> = iRetrofit?.searchPhotos(searchTerm = term, size = "20") ?: return

//        위와 같은 기능을 하는 코드
//        val term = searchTerm.let {
//            it
//        }?: ""

//        val call: Call<JsonElement> = iRetrofit?.searchPhotos(searchTerm = term).let {
//            it
//        }?: return

        call.enqueue(object: retrofit2.Callback<JsonElement> {

            // 응답에 성공했을 때 호출
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "RetrofitManager - onResponse() called / response : ${response.body()}")

                when(response.code()) {
                    200 -> {

                        response.body()?.let {  // body에 값이 있다면 실행

                            var parsedPhotoDataArray = ArrayList<Photo>()
                            val body = it.asJsonObject
                            val results = body.getAsJsonArray("results")

                            val total = body.get("total").asInt

                            if (total == 0) {
                                completion(RESPONSE_STATUS.NO_CONTENTS, null)
                            } else {
                                results.forEach { resultItem ->
                                    val resultItemObject = resultItem.asJsonObject

                                    val user = resultItemObject.get("user").asJsonObject
                                    val username: String = user.get("username").asString
                                    val likesCount = resultItemObject.get("likes").asInt
                                    val thumbnailLink =
                                        resultItemObject.get("urls").asJsonObject.get("thumb").asString
                                    val createAt = resultItemObject.get("created_at").asString

                                    // 날짜 데이터 수정
                                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                    val formatter = SimpleDateFormat("yyyy년\nMM월 dd일")
                                    val outputDateString = formatter.format(parser.parse(createAt))

//                                Log.d(TAG, "RetrofitManager - outputDateString : $outputDateString")

                                    val photoItem = Photo(
                                        thumbnail = thumbnailLink,
                                        author = username,
                                        createAt = outputDateString,
                                        likesCount = likesCount
                                    )

                                    parsedPhotoDataArray.add(photoItem)
                                }
                                // 값과 이벤트를 같이 넘겨준다.
                                completion(RESPONSE_STATUS.OKAY, parsedPhotoDataArray)
                            }
                        }
                    }
                }

            }

            // 응답에 실패했을 때 호출
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "RetrofitManager - onFailure() called / t: $t")

                completion(RESPONSE_STATUS.FAIL, null)
            }

        })
    }

    // 유저 검색 API 호출
    fun searchUsers (searchTerm: String?, completion: (RESPONSE_STATUS, ArrayList<User>?) -> Unit) {

        val term = searchTerm ?: ""

        val call: Call<JsonElement> = iRetrofit?.searchUsers(searchTerm = term) ?: return

        // 통신 결과에 대한 후 처리를 한다.
        // = 통신 결과에 따라 Callback 메서드를 실행
        call.enqueue(object: retrofit2.Callback<JsonElement> {

            // 통신 성공 시, 호출
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "RetrofitManager - onResponse() called / response : ${response.body()}")

                when(response.code()) {
                    200 -> {
                        response.body()?.let {

                            var parsedUserDataArray = ArrayList<User>()

                            val body = it.asJsonObject
                            val results = body.getAsJsonArray("results")

                            val total = body.get("total").asInt

                            if (total == 0) {
                                completion(RESPONSE_STATUS.NO_CONTENTS, null)
                            } else {
                                results.forEach { resultItem ->
                                    val resultItemObject = resultItem.asJsonObject

                                    val name = resultItemObject.get("name").asString
                                    val nickName = "@" + resultItemObject.get("username").asString
                                    val profile =
                                        resultItemObject.get("profile_image").asJsonObject.get("large").asString

                                    val userItem = User(
                                        name = name,
                                        nickName = nickName,
                                        profile = profile
                                    )

                                    parsedUserDataArray.add(userItem)
                                }

                                completion(RESPONSE_STATUS.OKAY, parsedUserDataArray)
                            }
                        }
                    }
                }
            }

            // 통신 실패 시, 호출출
           override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "RetrofitManager - onFailure() called / t: $t")

                completion(RESPONSE_STATUS.FAIL, null)
            }
        })
    }
}