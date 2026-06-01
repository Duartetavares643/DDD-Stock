package com.example.ddd_stock
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ddd_stock.auth.AuthViewModel
import com.example.ddd_stock.databinding.ActivityMainBinding
import com.example.ddd_stock.service.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var vm: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(AuthViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        val nav = findNavController(R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(nav, AppBarConfiguration(setOf(R.id.nav_home), binding.drawerLayout))
        binding.navView.setupWithNavController(nav)

        nav.addOnDestinationChangedListener { _, d, _ ->
            val auth = d.id == R.id.loginFragment || d.id == R.id.registerFragment
            if (auth) { supportActionBar?.hide(); binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED) }
            else { supportActionBar?.show(); binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
        }

        if (SessionManager(this).isLoggedIn()) nav.navigate(R.id.nav_home)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment_content_main).navigateUp(AppBarConfiguration(setOf(R.id.nav_home), binding.drawerLayout)) || super.onSupportNavigateUp()
}
