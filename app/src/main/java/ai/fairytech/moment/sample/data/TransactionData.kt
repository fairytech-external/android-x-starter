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

package ai.fairytech.moment.sample.data

/*
 * 캐시백 실적 내역 서버에서 받아오는 정보를 Schema 로 정의
 */
enum class TransactionLogCallbackStatus {
    UNKNOWN_TRANSACTION_LOG_CALLBACK_STATUS,
    NO_ENDPOINT,
    FAIL,
    SUCCESS
}

enum class TransactionStatus {
    UNKNOWN_TRANSACTION_STATUS,
    CREATED,
    CONFIRMED,
    CANCELED,
}

enum class TransactionType {
    UNKNOWN_TRANSACTION_TYPE,
    IN_APP,
    NOTIFICATION,
}

class MonetaryAmount {
    // 통화
    var currency: Currency = Currency.KRW
    // 금액
    var amount: Float = 0.0f
}

enum class Currency {
    UNKNOWN_CUREENCY,
    KRW, // 한국
    USD, // 미국
}

class GetTransactionResponse {
    var userId: String = ""
    // First key is TransactionStatus enum value in string,
    // Second key is Transaction id
    lateinit var allTransactions: HashMap<String, HashMap<String, Transaction>>
}

class Transaction {
    // 항상 unique id값
    var id: String = ""
    var type: TransactionType = TransactionType.UNKNOWN_TRANSACTION_TYPE
    var projectId: String = ""
    var notificationId: String = ""
    var businessName: String = ""
    var businessImageUrl: String = ""
    // Unique 실적에 대한 id값. 같은 transactionId에대하여 다른 type의 transaction이 있을 수 있음.
    var transactionId: String = ""
    // 사용자 아이디
    var userId: String = ""
    // Status of user's transaction.
    lateinit var status: TransactionStatus
    var businessId: String = ""
    // Total transaction amount.
    var sales: MonetaryAmount = MonetaryAmount()
    // Total commission amount in this transaction before fee deducted.
    var totalCommission: MonetaryAmount = MonetaryAmount()
    // Commission amount in this transaction after deduction.
    var commission: MonetaryAmount = MonetaryAmount()
    // Timestamp the transaction has created.
    var timestampMillis: Long = 0L
    // Timestamp the transaction log has created.
    var createdTimestampMillis: Long = 0L
    var callbackStatus: TransactionLogCallbackStatus =
        TransactionLogCallbackStatus.UNKNOWN_TRANSACTION_LOG_CALLBACK_STATUS
}