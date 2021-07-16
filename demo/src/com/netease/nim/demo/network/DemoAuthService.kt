/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.nim.demo.network

import android.content.pm.PackageManager
import android.util.Log
import com.netease.nim.demo.BuildConfig
import com.netease.nim.demo.DemoCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DemoAuthService {
    private val baseUrl = if (BuildConfig.DEBUG) "https://yiyong-qa.netease.im" else "https://yiyong.netease.im"
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val appKey: String by lazy {
        DemoCache.getContext()?.run {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.netease.nim.appKey")
        } ?: throw IllegalStateException("DemoCache don't have context.")
    }

    private val retrofit: Retrofit by lazy {
        initForRetrofit()
    }

    private fun initForRetrofit(baseUrl: String = this.baseUrl): Retrofit {
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor { Log.d("DemoAuthService", "====> $it") }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }.build()
        return Retrofit.Builder().apply {
            baseUrl(baseUrl)
            client(okHttpClient)
            addConverterFactory(GsonConverterFactory.create())
        }.build()
    }

    @JvmStatic
    fun fetchSmsCode(mobile: String, callback: ResponseCallback<Void>) {
        executeNetWork(callback) {
            val body = mapOf("mobile" to mobile)
            it.fetchLoginSmsCode(appKey, body = body)
        }
    }

    @JvmStatic
    fun loginBySmsCode(mobile: String, smsCode: String, callback: ResponseCallback<UserData>) {
        executeNetWork(callback) {
            val body = mutableMapOf("mobile" to mobile, "smsCode" to smsCode)
            it.loginBySmsCode(appKey, body = body)
        }
    }

    @JvmStatic
    fun registerBySmsCode(mobile: String, smsCode: String, nickname: String, callback: ResponseCallback<UserData>) {
        executeNetWork(callback) {
            val body = mutableMapOf("mobile" to mobile, "smsCode" to smsCode, "nickname" to nickname)
            it.registerBySmsCode(appKey, body = body)
        }
    }

    private fun <T> executeNetWork(callback: ResponseCallback<T>, action: (DemoAuthApi) -> Call<NetResponse<T>>) {
        retrofit.create(DemoAuthApi::class.java).let {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    val temp = action(it)
                    temp.execute().body()!!
                }
                callback.onResponse(result)
            }
        }
    }
}