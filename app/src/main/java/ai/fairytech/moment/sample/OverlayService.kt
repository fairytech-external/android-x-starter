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

package ai.fairytech.moment.sample

import ai.fairytech.moment.constants.ActionNameConstants
import ai.fairytech.moment.sample.R
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.webkit.WebView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.localbroadcastmanager.content.LocalBroadcastManager


/**
 * 해당 서비스는 "캐시백 사이트 진입 알림" 애니메이션을 위해 이용됨.
 */
class OverlayService : Service() {
    private val binder: IBinder = LocalBinder()

    private var mWindowManager: WindowManager? = null
    private var mFloatingWidgetView: View? = null
    private var containerView: View? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // View 셋업 하기
        mFloatingWidgetView = inflater.inflate(R.layout.overlay_widget_layout, null)
        val params: WindowManager.LayoutParams =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 0
        mWindowManager!!.addView(mFloatingWidgetView, params)
        containerView = mFloatingWidgetView!!.findViewById<WebView>(R.id.container)

        // 처음엔 뷰가 보이지 않음
        mFloatingWidgetView!!.visibility = View.GONE
    }

    fun showView() {
        mFloatingWidgetView!!.post {
            // 뷰를 왼쪽에서 오른쪽, 다시 들어가는 애니메이션으로 보여줌
            mFloatingWidgetView!!.visibility = View.VISIBLE
            val startX = -1000.0f
            containerView!!.translationX = startX
            val set = AnimatorSet()
            val inAnim = ObjectAnimator.ofFloat(containerView!!, "translationX", startX, 0f)
            inAnim.duration = 500
            inAnim.startDelay = 500
            inAnim.interpolator = AccelerateInterpolator()
            val outAnim = ObjectAnimator.ofFloat(containerView!!, "translationX", 0f, startX)
            outAnim.duration = 500
            outAnim.startDelay = 3000
            outAnim.interpolator = AccelerateInterpolator()
            set.playTogether(inAnim, outAnim);
            set.start();
            outAnim.doOnEnd {
                mFloatingWidgetView!!.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingWidgetView != null) mWindowManager!!.removeView(
            mFloatingWidgetView
        )
    }

    inner class LocalBinder : Binder() {
        val service: OverlayService
            get() = // Return this instance of LocalService so clients can call public methods.
                this@OverlayService
    }
}
