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

import ai.fairytech.moment.sample.data.Transaction
import ai.fairytech.moment.sample.data.TransactionStatus
import ai.fairytech.moment.sample.notification.NotificationController
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CashbackFirebaseMessagingService: FirebaseMessagingService() {
    /**
     * Called if the FCM registration token is updated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.Main).launch {
            FirebaseFunctionsManager.getInstance()?.updateToken(token)?.await()
        }
    }

    companion object {
        const val TYPE_ON_TRANSACTION_EVENT = "transaction"
    }

    // FCM 메시지
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val type = remoteMessage.data.get("type")
        // 실적 생성, 확정시 메시지가 옴
        if (type == TYPE_ON_TRANSACTION_EVENT) {
            var json = remoteMessage.data.get("transaction") ?: ""
            val gson = Gson()
            val transactionLog = gson.fromJson(json, Transaction::class.java)
            val title =  if (transactionLog.status == TransactionStatus.CREATED)
                "" + transactionLog.commission.amount
                    else transactionLog.commission.amount.toString() + "캐시가 확정됬어요!";
            val description =  if(transactionLog.status == TransactionStatus.CREATED)
                transactionLog.businessName + "에서 적립예정 캐시가 생겼어요! "
                    else "새로 추가된 캐시를 확인해보세요";
            NotificationController.getInstance()?.sendNotification(title, description)
        }
    }
}