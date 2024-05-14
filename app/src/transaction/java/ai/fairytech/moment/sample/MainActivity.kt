package ai.fairytech.moment.sample

import ai.fairytech.moment.sample.firebase.FirebaseFunctionsManager
import ai.fairytech.moment.sample.ui.cashback.CashbackFragment
import ai.fairytech.moment.sample.ui.main.MainFragment
import ai.fairytech.moment.sample.ui.transaction.TransactionFragment
import android.os.Bundle
import com.google.android.material.navigation.NavigationBarView

class MainActivity : BaseMainActivity() {
    private val mainFragment = MainFragment()
    private val cashbackFragment = CashbackFragment()
    private val transactionFragment = TransactionFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseFunctionsManager.getInstance()?.initialize(applicationContext);

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

                R.id.menu_transaction -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, transactionFragment)
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }
}