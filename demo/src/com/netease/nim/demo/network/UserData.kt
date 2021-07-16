/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.nim.demo.network

import com.google.gson.annotations.SerializedName

class UserData(@SerializedName("imAccid") val accId: String,
               @SerializedName("imToken") val token: String)