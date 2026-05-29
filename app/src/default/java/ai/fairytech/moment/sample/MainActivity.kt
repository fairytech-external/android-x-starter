package ai.fairytech.moment.sample

import ai.fairytech.moment.sample.ui.main.MainFragment
import android.os.Bundle

class MainActivity: BaseMainActivity() {
    private val mainFragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, mainFragment)
            .commit()

    }
}