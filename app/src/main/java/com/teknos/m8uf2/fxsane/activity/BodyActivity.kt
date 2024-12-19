package com.teknos.m8uf2.fxsane.activity

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.ActivityBodyBinding
import com.teknos.m8uf2.fxsane.fragment.AboutUsFragment
import com.teknos.m8uf2.fxsane.fragment.HomeFragment
import com.teknos.m8uf2.fxsane.fragment.RealEstateFragment

class BodyActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityBodyBinding
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBodyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_container, HomeFragment())
                .commit()
        }
        setSupportActionBar(binding.toolBar)
        drawerLayout = binding.main
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = binding.navigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navigateToFragment(HomeFragment())
                    true
                }
                R.id.aboutUs -> {
                    navigateToFragment(AboutUsFragment())
                    true
                }
                R.id.inmobiliaria -> {
                    navigateToFragment(RealEstateFragment())
                    true
                }
                else -> false
            }
        }
    }
    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_container, fragment)
            .commit()
        drawerLayout.closeDrawer(GravityCompat.START)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.configMenu -> {
                onClickSettingsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun onClickSettingsMenu() {
        val popupMenu = PopupMenu(this, findViewById(R.id.configMenu))
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.show()
    }
}