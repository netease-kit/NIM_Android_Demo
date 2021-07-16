/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.nim.demo.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DemoAuthApi {
    /**
     * 发送登录验证码
     */

    @POST("/auth/sendLoginSmsCode")
    fun fetchLoginSmsCode(@Header("appKey") appKey: String,
                          @Header("scope") scope: Int = 7,
                          @Body body: Map<String, String>
    ): Call<NetResponse<Void>>

    /**
     * 通过验证码完成登录
     */
    @POST("/auth/loginBySmsCode")
    fun loginBySmsCode(@Header("appKey") appKey: String,
                       @Header("scope") scope: Int = 7,
                       @Body body: Map<String, String>
    ): Call<NetResponse<UserData>>


    /**
     * 通过验证码完成注册
     */
    @POST("/auth/registerBySmsCode")
    fun registerBySmsCode(
            @Header("appKey") appKey: String,
            @Header("scope") scope: Int = 7,
            @Body body: Map<String, String>
    ): Call<NetResponse<UserData>>
}