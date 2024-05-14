package ai.fairytech.moment.sample.receiver

import ai.fairytech.moment.constants.ActionNameConstants
import ai.fairytech.moment.constants.ExtraNameConstants
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class BusinessMatchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ActionNameConstants.TRIGGER_BROADCAST_ACTION_NAME) return

        val businessId = intent.getStringExtra(ExtraNameConstants.BUSINESS_ID_EXTRA_NAME) ?: ""
        val matchType = intent.getSerializableExtra(ExtraNameConstants.ACTIVITY_MATCH_TYPE_EXTRA_NAME)
        val timestamp = intent.getLongExtra(ExtraNameConstants.TIMESTAMP_MILLIS_EXTRA_NAME, 0)
        val customData = intent.getStringExtra(ExtraNameConstants.CUSTOM_DATA_EXTRA_NAME) ?: ""
        Toast.makeText(context, "Business match ($businessId, $matchType, $timestamp, $customData)", Toast.LENGTH_SHORT).show()
        Log.i("BusinessMatchReceiver", "Business match ($businessId, $matchType, $timestamp, $customData)")
    }
}