package com.example.fyp.adapter

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.fyp.R
import com.example.fyp.activity.AccommodationJobListActivity
import com.example.fyp.activity.AccountActivity
import com.example.fyp.activity.AccountAgentActivity
import com.example.fyp.activity.BookingListAgentActivity
import com.example.fyp.activity.CommissionListActivity
import com.example.fyp.activity.SettingActivity
import com.example.fyp.activity.SettingAgentActivity
import com.example.fyp.activity.SignUpActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationHandlerAgent(private val context: Context) {

    /*  Use in activity code to call the navigation bar buttom


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navigationHandler = BottomNavigationHandler(this)
        navigationHandler.setupBottomNavigation(bottomNavigationView)



    */
    fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_find_job -> {
                    navigateToFindJob()
                    true
                }
                R.id.nav_book -> {
                    navigateToBook()
                    true
                }
                R.id.nav_salary -> {
                    navigateToSalary()
                    true
                }
                R.id.nav_profile -> {
                    navigateToProfile()
                    true
                }
                R.id.nav_settings -> {
                    navigateToSettings()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToFindJob() {
        // Implement navigation logic
        val intent = Intent(context, AccommodationJobListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun navigateToBook() {
        val intent = Intent(context, BookingListAgentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun navigateToSalary() {
        val intent = Intent(context, CommissionListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    private fun navigateToProfile() {
        val intent = Intent(context, AccountAgentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(context, SettingAgentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}