package com.example.flagmanstorage
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityMainBinding

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)
        if (!userPreferences.isLoggedIn()) {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }

        initBinding()
        initViews()


    }

    private fun initViews() {
        binding.btnAddToWarehouse.setOnClickListener {
            val intent = Intent(this, IntroductionProds::class.java)
            startActivity(intent)
        }
        binding.btnAddToShipping.setOnClickListener{
            val intent = Intent(this, ShipmentsProds::class.java)
            startActivity(intent)
        }
    }


    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userName = userPreferences.getUserName()
        if (userName != null) {
            binding.userInfo.text = "Авторизован: $userName"
        }

        // Кнопка выхода
        binding.btnLogout.setOnClickListener {
            userPreferences.logout()
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish() // Закрываем активность
        }
    }
}
