package com.enescerrahoglu.paylash.view.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.enescerrahoglu.paylash.R
import com.enescerrahoglu.paylash.databinding.ActivityNavigationBinding


class NavigationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadFragment(HomeFragment())
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    if (binding.bottomNavigation.selectedItemId == R.id.home) {
                        findViewById<RecyclerView>(R.id.recyclerView).layoutManager!!.smoothScrollToPosition(findViewById<RecyclerView>(R.id.recyclerView), null, 0);
                        false
                    } else {
                        loadFragment(HomeFragment())
                        true
                    }
                }
                R.id.new_post -> {
                    if (binding.bottomNavigation.selectedItemId == R.id.new_post) {
                        false
                    } else {
                        loadFragment(NewPostFragment())
                        true
                    }
                }
                R.id.profile -> {
                    if (binding.bottomNavigation.selectedItemId == R.id.profile) {
                        false
                    } else {
                        loadFragment(ProfileFragment())
                        true
                    }
                }
                else -> {
                    loadFragment(HomeFragment())
                    true
                }
            }
        }
    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout,fragment)
        transaction.commit()
    }
}