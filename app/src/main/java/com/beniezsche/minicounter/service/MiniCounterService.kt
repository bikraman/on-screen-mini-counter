package com.beniezsche.minicounter.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.beniezsche.minicounter.R
import kotlin.math.abs


class MiniCounterService : Service(), View.OnTouchListener, View.OnClickListener {

    private var topLeftView: View? = null

    private var counterView: View? = null
    private var offsetX = 0f
    private var offsetY = 0f
    private var originalXPos = 0
    private var originalYPos = 0
    private var moving = false
    private var wm: WindowManager? = null

    private var count = 0

    private lateinit var tvCounter: TextView

    companion object {
        var isServiceRunning: Boolean = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("InflateParams")
    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()

        isServiceRunning = true

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        counterView = LayoutInflater.from(this).inflate(R.layout.layout_counter, null)
        counterView!!.setOnTouchListener(this)
        counterView!!.alpha = 1.0f
        counterView!!.setOnClickListener(this)

        val windowManagerLayoutParamOverlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, windowManagerLayoutParamOverlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.START or Gravity.TOP
        params.x = 0
        params.y = 0
        wm!!.addView(counterView, params)
        topLeftView = View(this)
        val topLeftParams = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,windowManagerLayoutParamOverlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT)
        topLeftParams.gravity = Gravity.START or Gravity.TOP
        topLeftParams.x = 0
        topLeftParams.y = 0
        topLeftParams.width = 0
        topLeftParams.height = 0

        val buttonMinusOne = counterView!!.findViewById<Button>(R.id.buttonMinusOne)
        val buttonPlusOne = counterView!!.findViewById<Button>(R.id.buttonPlusOne)

        tvCounter = counterView!!.findViewById(R.id.tvCounter)

        buttonMinusOne.setOnClickListener {
            if(count == 0)
                count = 0
            else
                count--
            setCounterText(count)
        }

        buttonMinusOne.setOnLongClickListener {
            count = 0
            setCounterText(count)
            false
        }

        buttonPlusOne.setOnClickListener {
            count++
            setCounterText(count)
        }

        wm!!.addView(topLeftView, topLeftParams)
    }

    private fun setCounterText(count: Int) {
        tvCounter.text = count.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        if (counterView != null) {
            wm!!.removeView(counterView)
            wm!!.removeView(topLeftView)
            counterView = null
            topLeftView = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.rawX
            val y = event.rawY
            moving = false
            val location = IntArray(2)
            counterView!!.getLocationOnScreen(location)
            originalXPos = location[0]
            originalYPos = location[1]
            offsetX = originalXPos - x
            offsetY = originalYPos - y
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val topLeftLocationOnScreen = IntArray(2)
            topLeftView!!.getLocationOnScreen(topLeftLocationOnScreen)
            println("topLeftY=" + topLeftLocationOnScreen[1])
            println("originalY=$originalYPos")
            val x = event.rawX
            val y = event.rawY
            val params: WindowManager.LayoutParams = counterView!!.layoutParams as WindowManager.LayoutParams
            val newX = (offsetX + x).toInt()
            val newY = (offsetY + y).toInt()
            if (abs(newX - originalXPos) < 1 && abs(newY - originalYPos) < 1 && !moving) {
                return false
            }
            params.x = newX - topLeftLocationOnScreen[0]
            params.y = newY - topLeftLocationOnScreen[1]
            wm!!.updateViewLayout(counterView, params)
            moving = true
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (moving) {
                return true
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show()
    }
}