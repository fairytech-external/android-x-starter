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

package ai.fairytech.moment.sample.receiver

import ai.fairytech.moment.constants.ActionNameConstants
import ai.fairytech.moment.sample.OverlayService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * 사용자가 캐시백 사이트 접속시, Moment SDK로부터 아래 인텐트를 받음
 * action: ai.fairytech.moment.action.CASHBACK_PAGE_OPENED
 * Extra String Payload
 * - link_url : 캐시백 링크 url
 * - business_id : 캐시백 비지니스 아이디
 */
class CashbackPageOpenReceiver : BroadcastReceiver() {
    // CountDownLatch is used to wait for OverlayService to be connected.
    private lateinit var latch: CountDownLatch

    private var overlayService: OverlayService? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            overlayService = (service as OverlayService.LocalBinder).service
            latch.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            overlayService = null
            latch.countDown()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ActionNameConstants.CASHBACK_PAGE_OPENED_BROADCAST_ACTION_NAME) return
        val businessId = intent.getStringExtra("business_id")

        context.applicationContext.run {
            if (Settings.canDrawOverlays(this)) {
                thread(start = true) {
                    this.startService(Intent(this, OverlayService::class.java))
                    latch = CountDownLatch(1)
                    this.bindService(
                        Intent(this, OverlayService::class.java),
                        connection,
                        Context.BIND_AUTO_CREATE
                    )
                    // Wait for OverlayService to be connected.
                    latch.await(3000, TimeUnit.MILLISECONDS)
                    overlayService?.showView()
                    this.unbindService(connection)
                }

            } else {
                Toast.makeText(
                    this,
                    "Cashback page opened (businessId: $businessId)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}