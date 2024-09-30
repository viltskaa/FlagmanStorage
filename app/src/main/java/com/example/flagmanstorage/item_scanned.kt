    package com.example.flagmanstorage

    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import com.example.flagmanstorage.QrScanner.PreferencesHelper
    import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem

    class item_scanned : AppCompatActivity() {

        private lateinit var preferencesHelper: PreferencesHelper
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_item_scanned)

        }
    }