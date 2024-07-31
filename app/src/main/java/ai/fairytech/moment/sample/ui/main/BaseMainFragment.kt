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

package ai.fairytech.moment.sample.ui.main

import ai.fairytech.moment.MomentSDK
import ai.fairytech.moment.sample.R
import ai.fairytech.moment.exception.MomentException
import ai.fairytech.moment.sample.databinding.FragmentMainBinding
import ai.fairytech.moment.sample.notification.NotificationController
import ai.fairytech.moment.sample.OverlayService
import ai.fairytech.moment.proto.CashbackProgram
import ai.fairytech.moment.sample.ui.cashback.CashbackAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * 앱 시작시 구동되는 첫 Fragment.
 * 기능:
 *  - 권한 허용 및 서비스 시작
 *  - 캐시백 프로그램 리스트 제공
 *  - 마이페이지로 이동.
 */
open class BaseMainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    protected val binding get() = _binding!!

    protected lateinit var moment: MomentSDK

    // 알림 권한 허용
    private val notificationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(context, "알림 권한 허용", Toast.LENGTH_SHORT).show()
            }
            // 다른 앱 위에 그리기 권한 허용 (캐시백 사이트 진입 알림 애니메이션을 위해 이용됨.)
            // Note: 기존에 다른앱위에 그리기 서비스 있을 시, layout만 추가하여 사용해도 됨
            if (!Settings.canDrawOverlays(requireContext())) {
                askDrawOverOtherAppsPermission()
            }
        }

    // 권한 허용
    private val appUsagePermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (MomentSDK.isAppUsagePermissionGranted(requireContext().applicationContext)) {
                handleStart()
            } else {
                binding.startService.isEnabled = true
                binding.startService.isChecked = false
            }
        }

    private val drawOverOtherAppsPermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(requireContext(), OverlayService::class.java)
                requireContext().startService(intent)
                Toast.makeText(context, "다른 앱 위에 그리기 권한 허용", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "다른 앱 위에 그리기 권한이 필요합니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    private val checkedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val context = buttonView.context
            if (isChecked) {
                binding.startService.isEnabled = false
                if (MomentSDK.isAppUsagePermissionGranted(context)) {
                    handleStart()
                } else {
                    if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
                        val intent = Intent()
                        intent.component = ComponentName(
                            "com.android.settings",
                            "com.android.settings.Settings\$UsageAccessSettingsActivity"
                        )
                        startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        appUsagePermissionLauncher.launch(intent)
                    }
                }
            } else {
                handleStop()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moment = MomentSDK.getInstance(requireContext().applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationPermissionLauncher.unregister()
        appUsagePermissionLauncher.unregister()
        drawOverOtherAppsPermissionLauncher.unregister()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext().applicationContext
        // service가 os에 의해 종료됐거나, 사용자가 강제종료했을 수도 있으므로 restart를 시도.
        moment.restartIfNeeded(getConfig(context), object :
            MomentSDK.RestartResultCallback {
            override fun onSuccess(resultCode: MomentSDK.RestartResultCode) {
                if (resultCode == MomentSDK.RestartResultCode.SERVICE_RESTARTED) {
                    Toast.makeText(
                        context,
                        "restart에 성공했습니다: $resultCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (resultCode == MomentSDK.RestartResultCode.SERVICE_RESTARTED
                    || resultCode == MomentSDK.RestartResultCode.SERVICE_ALREADY_RUNNING
                ) {
                    binding.startService.setOnCheckedChangeListener(null)
                    binding.startService.isEnabled = true
                    binding.startService.isChecked = moment.isRunning
                    binding.startService.setOnCheckedChangeListener(checkedChangeListener)
                }
            }

            override fun onFailure(exception: MomentException) {
                Toast.makeText(context, "restart에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e(
                    "MomentSDK",
                    "restartIfNeeded onFailure(${exception.errorCode.name}): ${exception.message}"
                )
                binding.startService.isEnabled = true
                binding.startService.isChecked = moment.isRunning
            }
        })
        moment.setUserId("test-user-id")
        /** 권한 관련 **/
        // 알림 권한 없을시 받음
        if (!MomentSDK.isNotificationPermissionGranted(context) && canAskRuntimeNotiPermission()) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 서비스를 시작하는 스위치
        binding.startService.isChecked = MomentSDK.isAppUsagePermissionGranted(context)
                && moment.isRunning
        binding.startService.setOnCheckedChangeListener(checkedChangeListener)
    }

    private fun getConfig(context: Context): MomentSDK.Config {
        return MomentSDK.Config(context)
            .notificationChannelId(NotificationController.NOTIFICATION_CHANNEL_ID) // 알림 채널 아이디
            .notificationId(NotificationController.NOTIFICATION_ID) // 알림 아이디
            .notificationIconResId(R.drawable.baseline_person_24)
            .serviceNotificationChannelId(NotificationController.SERVICE_NOTIFICATION_CHANNEL_ID) // 서비스를 위해 필요한 채널아이디
            .serviceNotificationId(NotificationController.SERVICE_NOTIFICATION_ID)
            .serviceNotificationIconResId(R.drawable.baseline_person_24)
            .serviceNotificationTitle("인식 서비스")
            .serviceNotificationText("인식 서비스가 동작 중입니다")
    }

    // 서비스 시작
    private fun handleStart() {
        try {
            val context = requireContext().applicationContext
            moment.setMarketingPushEnabled(true)
            moment.setSendBubble(false)
            moment.start(getConfig(context), object : MomentSDK.ResultCallback {
                override fun onSuccess() {
                    Toast.makeText(context, "start에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    binding.startService.isEnabled = true
                    binding.startService.isChecked = moment.isRunning
                }

                override fun onFailure(exception: MomentException) {
                    Toast.makeText(context, "start에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e(
                        "MomentSDK",
                        "start onFailure(${exception.errorCode.name}): ${exception.message}"
                    )
                    binding.startService.isEnabled = true
                    binding.startService.isChecked = moment.isRunning
                }
            })
        } catch (e: MomentException) {
            binding.startService.isEnabled = true
            binding.startService.isChecked = moment.isRunning
        }
    }

    // 서비스 Stop
    private fun handleStop() {
        try {
            moment.stop(object : MomentSDK.ResultCallback {
                override fun onSuccess() {
                    Toast.makeText(context, "stop에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    binding.startService.isEnabled = true
                    binding.startService.isChecked = moment.isRunning
                }

                override fun onFailure(exception: MomentException) {
                    Toast.makeText(context, "stop에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e(
                        "MomentSDK",
                        "start onFailure(${exception.errorCode.name}): ${exception.message}"
                    )
                    binding.startService.isEnabled = true
                    binding.startService.isChecked = moment.isRunning
                }
            })
        } catch (e: MomentException) {
            binding.startService.isEnabled = true
            binding.startService.isChecked = moment.isRunning
        }
    }

    private fun canAskRuntimeNotiPermission(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU;
    }

    private fun askDrawOverOtherAppsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.fromParts("package", requireContext().packageName, null)
        drawOverOtherAppsPermissionLauncher.launch(intent)
    }
}