package ai.fairytech.moment.sample.ui.main

import ai.fairytech.moment.sample.R
import ai.fairytech.moment.sample.firebase.FirebaseFunctionsManager
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainFragment : BaseMainFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseFunctionsManager.getInstance()?.initialize(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup fcm
        // 사용자가 구매하여 실적 생성되거나 확정됬을때 FCM 알림으로 받음
        CoroutineScope(Dispatchers.IO).launch {
            val token = FirebaseMessaging.getInstance().token.await()
            if (!token.isNullOrEmpty()) {
                FirebaseFunctionsManager.getInstance()?.updateToken(token)?.await()
            }
            moment?.setUserId(FirebaseFunctionsManager.getInstance()?.getUserId() ?: "test-user-id")
        }
    }
}