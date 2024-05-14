/*
 * Fairy Technologies CONFIDENTIAL
 * __________________
 *
 * Copyright (C) Fairy Technologies, Inc - All Rights Reserved
 *
 * NOTICE:  All information contained herein is, and remains the property of Fairy
 * Technologies Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to Fairy Technologies Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information,or reproduction or modification of this material
 * is strictly forbidden unless prior written permission is obtained from Fairy
 * Technologies Incorporated.
 *
 */

package ai.fairytech.moment.sample.firebase

import ai.fairytech.moment.sample.data.GetTransactionResponse
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * 서버와 통신하는 모듈. 데모 서버가 Firebase로 되어있음.
 */
class FirebaseFunctionsManager {
    private val mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-northeast3")

//    private var userId = "temp-user-id";
    companion object {
        var userId = "temp-user-id";
        private var _instance: FirebaseFunctionsManager? = null
        fun getInstance(): FirebaseFunctionsManager? {
            if (_instance == null) {
                synchronized(FirebaseFunctionsManager::class.java) {
                    if (_instance == null) {
                        _instance = FirebaseFunctionsManager()
                    }
                }
            }
            return _instance
        }
    }
    fun initialize(context: Context) {
        userId = uniqueUserId(context);
    }
    fun getUserId() : String {
        return userId;
    }

    fun uniqueUserId(context: Context): String {
        if (Build.VERSION.SDK_INT >= 29) {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }else{
            val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
            return telephonyManager?.deviceId ?: "temp-user-id"
        }
    }

    // 사용자 고유 id애 매핑되어있는 구매내역을 받아옴
    suspend fun getTransactions(): Task<GetTransactionResponse?> {
        val data = hashMapOf(
            "userId" to userId
        )

        return withContext(Dispatchers.IO) {
            mFunctions
                .getHttpsCallable("getTransactions")
                .call(data)
                .continueWith { task ->
                    try {
                        if (!task.isSuccessful) {
                            throw Exception(task.exception)
                        }
                        val jsonData = task.result?.data as HashMap<*, *>
                        val json = Gson().toJson(jsonData)
                        val gson = Gson()
                        gson.fromJson(json, GetTransactionResponse::class.java)
                    } catch (e: Exception) {
                        Log.e("FirebaseFunctionsManager/getTransactions", e.message.toString())
                        null
                    }
                }
        }
    }

    // FCM을 위하여 토큰값 리프레시
    suspend fun updateToken(token: String): Task<String?> {
        val data = hashMapOf(
            "userId" to userId,
            "token" to token
        )

        return withContext(Dispatchers.IO) {
            mFunctions
                .getHttpsCallable("updateToken")
                .call(data)
                .continueWith { task ->
                    try {
                        if (!task.isSuccessful) {
                            throw Exception(task.exception)
                        }
                        val jsonData = task.result?.data as HashMap<*, *>
                        Gson().toJson(jsonData)
                    } catch (e: Exception) {
                        Log.e("FirebaseFunctionsManager/updateToken", e.message.toString())
                        null
                    }
                }
        }
    }

}
