package com.overplay.videoplayer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.overplay.videoplayer.R

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragment,
                PlayerFragment()
            ).commit()
    }
}