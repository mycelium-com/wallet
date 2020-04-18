package com.mycelium.bequant.remote

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mycelium.bequant.BequantPreference
import com.mycelium.bequant.remote.model.Auth
import com.mycelium.bequant.remote.model.AuthResponse
import com.mycelium.bequant.remote.model.Register
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class SignRepository {
    fun authorize(auth: Auth, success: () -> Unit, error: (String) -> Unit) {
        service.authorize(auth).enqueue(object : Callback<AuthResponse> {
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                error.invoke(t.message ?: "")
            }

            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    BequantPreference.setSession(response.body()?.session ?: "")
                    success.invoke()
                } else {
                    error.invoke(response.message())
                }
            }
        })
    }

    fun register(register: Register, success: () -> Unit, error: (String) -> Unit) {
        service.register(register).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                error.invoke(t.message ?: "")
                t.printStackTrace()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    success.invoke()
                } else {
                    error.invoke(response.message())
                }
            }
        })
    }

    companion object {
        val ENDPOINT = "https://reg.bequant.io/"
        val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJteWNlbGl1bSIsImp0aSI6ImJxN2g2M2ZzdmpvdG8xczVvaDEwIiwiaWF0IjoxNTg2NDM0ODI5LCJpc3MiOiJhdXRoLWFwaSIsImJpZCI6M30.0qvEnMxzbWF-P7eOpZDnSXwoOe5vDWluKFOFq5-tPaE"

        private val objectMapper = ObjectMapper()
                .registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        val repository by lazy { SignRepository() }


        val service by lazy {
            Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .client(OkHttpClient.Builder()
                            .addInterceptor {
                                it.proceed(it.request().newBuilder().apply {
                                    header("Content-Type", "application/json")
                                    header("X-API-KEY", API_KEY)
                                }.build())
                            }
                            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build())
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build()
                    .create(BequantService::class.java)
        }
    }
}