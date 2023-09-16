package dev.queiroz.applemusic

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.databinding.ActivityMainBinding
import dev.queiroz.applemusic.ui.home.HomeFragment
import dev.queiroz.applemusic.ui.search.SearchFragment
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val appleMusicViewModel: AppleMusicViewModel by viewModels()

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuListenNow -> setCurrentFragment(fragment = homeFragment)
                R.id.menuSearch -> setCurrentFragment(fragment = searchFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            commit()
        }

}