/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.nim.demo.network

class NetResponse<T>(val code: Int, val data: T? = null, val msg: String? = null) {

    fun isSuccessful() = code == 200
}