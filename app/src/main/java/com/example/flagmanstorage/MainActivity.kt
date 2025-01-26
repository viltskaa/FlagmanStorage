package com.example.flagmanstorage
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityMainBinding

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
            val intent = Intent(this, ShipmentProds2::class.java)
            startActivity(intent)
        }
        binding.btnRepack.setOnClickListener{
            val intent = Intent(this, WriteOffActivity::class.java)
            startActivity(intent)
        }
        binding.btnSet.setOnClickListener {
            val intent = Intent(this,SettingsActivity::class.java)
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
