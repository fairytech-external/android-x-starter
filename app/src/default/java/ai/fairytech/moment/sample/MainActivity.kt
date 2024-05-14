package ai.fairytech.moment.sample

import ai.fairytech.moment.sample.ui.cashback.CashbackFragment
import ai.fairytech.moment.sample.ui.main.MainFragment
import android.os.Bundle
import com.google.android.material.navigation.NavigationBarView

class MainActivity: BaseMainActivity() {
    private val mainFragment = MainFragment()
    private val cashbackFragment = CashbackFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, mainFragment)
            .commit()
        val bottomNavView: NavigationBarView = findViewById(R.id.menu_bottom_navigation)
        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, mainFragment)
                        .commit()
                    return@setOnItemSelectedListener true
                }

                R.id.menu_cashback -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, cashbackFragment)
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }
}