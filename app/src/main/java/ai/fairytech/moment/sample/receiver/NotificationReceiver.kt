package ai.fairytech.moment.sample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "ai.fairytech.moment.action.NOTIFICATION_SENT") return

        // DEVICE: 로컬 알림
        // REMOTE: FCM 알림
        val notificationType = intent.getStringExtra("notification_type")

        // DEVICE: server-generated proto id.
        // REMOTE: FCM RemoteMessage.messageId, or SDK UUID fallback.
        val notificationId = intent.getStringExtra("notification_id")

        // 알림 제목
        val title = intent.getStringExtra("title")

        // 알림 본문
        val body = intent.getStringExtra("body")

        // 알림 이미지
        val imageUrl = intent.getStringExtra("image_url")

        // 알림 클릭 시 이동할 URL
        val clickUrl = intent.getStringExtra("click_url")

        // 광고 캠페인 ID
        val campaignId = intent.getStringExtra("campaign_id")

        // 캐시백 쇼핑몰 ID
        val businessId = intent.getStringExtra("business_id")

        // ENTER: 앱 진입
        // EXIT: 앱 이탈
        val activityMatchType = intent.getStringExtra("activity_match_type")

        // 쇼핑몰 마다 상이함
        val pageName = intent.getStringExtra("page_name")

        // 알림을 전송한 시각 (밀리초 단위)
        val timestamp = intent.getLongExtra("timestamp_millis", 0)
    }
}