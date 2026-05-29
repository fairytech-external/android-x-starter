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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * 앱 시작시 구동되는 첫 Fragment.
 * 기능:
 *  - 사용자 ID 설정 (setUserId)
 *  - 인식 서비스 start / stop
 *  - UI 실행 (launchUI)
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
        }

    // 앱 사용기록 접근 권한 허용 후 콜백
    private val appUsagePermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (MomentSDK.isAppUsagePermissionGranted(requireContext().applicationContext)) {
                handleStart()
            } else {
                updateServiceButtons()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // launchUI 등 다른 화면을 다녀온 뒤 실제 실행 상태로 Start / Stop 버튼을 갱신
        if (_binding != null) {
            updateServiceButtons()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext().applicationContext
        // NOTE: setUserId는 반드시 init/start 호출 전에 호출해야 합니다.
        moment.setUserId("test-user-id", object : MomentSDK.ResultCallback {
            override fun onSuccess() {
                // setUserId 성공 후 init 호출
                moment.init(getConfig(context), object :
                    MomentSDK.RestartResultCallback {
                    override fun onSuccess(resultCode: MomentSDK.RestartResultCode) {
                        if (resultCode == MomentSDK.RestartResultCode.SERVICE_RESTARTED) {
                            Toast.makeText(
                                context,
                                "restart에 성공했습니다: $resultCode",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        updateServiceButtons()
                    }

                    override fun onFailure(exception: MomentException) {
                        handleMomentFailure("init", exception, "init에 실패했습니다.")
                        updateServiceButtons()
                    }
                })
            }

            override fun onFailure(exception: MomentException) {
                handleMomentFailure("setUserId", exception, "userId 설정에 실패했습니다.")
            }
        })
        /** 권한 관련 **/
        // 알림 권한 없을시 받음
        if (!MomentSDK.isNotificationPermissionGranted(context) && canAskRuntimeNotiPermission()) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1. 사용자 ID 설정
        binding.btnSetUserId.setOnClickListener { handleSetUserId() }

        // 2. 인식 서비스 start / stop
        binding.btnStart.setOnClickListener { handleStartClicked() }
        binding.btnStop.setOnClickListener { handleStop() }
        updateServiceButtons()

        // 3. UI 실행 (launchUI)
        binding.btnLaunchUi.setOnClickListener { handleLaunchUI() }
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

    // 1. 사용자 ID 설정
    private fun handleSetUserId() {
        val context = requireContext().applicationContext
        val userId = binding.etUserId.text.toString().trim()
        if (userId.isEmpty()) {
            Toast.makeText(context, "userId를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        moment.setUserId(userId, object : MomentSDK.ResultCallback {
            override fun onSuccess() {
                Toast.makeText(context, "userId 설정에 성공했습니다: $userId", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(exception: MomentException) {
                handleMomentFailure("setUserId", exception, "userId 설정에 실패했습니다.")
            }
        })
    }

    // 2. Start 버튼: 권한 확인 후 서비스 시작
    private fun handleStartClicked() {
        val context = requireContext().applicationContext
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
    }

    // 서비스 시작
    private fun handleStart() {
        try {
            val context = requireContext().applicationContext
            moment.start(getConfig(context), object : MomentSDK.ResultCallback {
                override fun onSuccess() {
                    Toast.makeText(context, "start에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    // start 성공 = 실행 중. isRunning()은 콜백 시점에 아직 갱신 전일 수 있으므로 명시적으로 지정.
                    updateServiceButtons(running = true)
                }

                override fun onFailure(exception: MomentException) {
                    handleMomentFailure("start", exception, "start에 실패했습니다.")
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                    updateServiceButtons()
                }
            })
        } catch (e: MomentException) {
            updateServiceButtons()
        }
    }

    // 서비스 Stop
    private fun handleStop() {
        try {
            moment.stop(object : MomentSDK.ResultCallback {
                override fun onSuccess() {
                    Toast.makeText(context, "stop에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    // stop 성공 = 정지. isRunning()은 콜백 시점에 아직 갱신 전일 수 있으므로 명시적으로 지정.
                    updateServiceButtons(running = false)
                }

                override fun onFailure(exception: MomentException) {
                    handleMomentFailure("stop", exception, "stop에 실패했습니다.")
                    updateServiceButtons()
                }
            })
        } catch (e: MomentException) {
            updateServiceButtons()
        }
    }

    // 3. UI 실행 (launchUI)
    private fun handleLaunchUI() {
        val context = requireContext().applicationContext
        val redirectTo = binding.etRedirectTo.text.toString().trim()
        val callback = object : MomentSDK.ResultCallback {
            override fun onSuccess() {
                // pass
            }

            override fun onFailure(exception: MomentException) {
                handleMomentFailure("launchUI", exception, "launchUI에 실패했습니다.")
            }
        }
        val config = getConfig(context)
        if (redirectTo.isEmpty()) {
            moment.launchUI(config, callback)
        } else {
            moment.launchUI(config, redirectTo, callback)
        }
    }

    // MomentSDK 호출 실패시 토스트 + 로그 공통 처리
    private fun handleMomentFailure(method: String, exception: MomentException, toastMessage: String) {
        Toast.makeText(requireContext().applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
        Log.e("MomentSDK", "$method onFailure(${exception.errorCode.name}): ${exception.message}")
    }

    // 서비스 실행 상태에 따라 Start / Stop 버튼 활성화 상태 갱신
    private fun updateServiceButtons(running: Boolean = moment.isRunning()) {
        binding.btnStart.isEnabled = !running
        binding.btnStop.isEnabled = running
    }

    private fun canAskRuntimeNotiPermission(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU;
    }
}
