package com.beniezsche.minicounter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.beniezsche.minicounter.service.MiniCounterService


class MainActivity : AppCompatActivity() {

    private lateinit var buttonToggleService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 12345)
            }
        }

        buttonToggleService = findViewById(R.id.buttonToggleService)


        if(MiniCounterService.isServiceRunning){
            buttonToggleService.text = "Stop Mini Counter"
            setButtonState()
        }
        else{
            buttonToggleService.text = "Start Mini Counter"
            setButtonState()
        }

    }

    private fun setButtonState() {
        buttonToggleService.setOnClickListener {
            if(MiniCounterService.isServiceRunning){
                toggleService(false)
                buttonToggleService.text = "Start Mini Counter"
            }
            else{
                toggleService(true)
                buttonToggleService.text = "Stop Mini Counter"
            }
        }
    }

    private fun toggleService(startService: Boolean){
        if (startService){
            val mcs = Intent(this, MiniCounterService::class.java)
            startService(mcs)
        }
        else{
            val mcs = Intent(this, MiniCounterService::class.java)
            stopService(mcs)
        }
    }

}