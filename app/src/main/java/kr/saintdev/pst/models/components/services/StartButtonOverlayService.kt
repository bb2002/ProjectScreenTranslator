package kr.saintdev.pst.models.components.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import kr.saintdev.pst.R
import kr.saintdev.pst.models.libs.DeviceControl.checkAPILevel
import kr.saintdev.pst.models.libs.ScreenTranslate
import kr.saintdev.pst.models.libs.SystemOverlay
import kr.saintdev.pst.models.libs.manager.EnvSettingKeys
import kr.saintdev.pst.models.libs.manager.EnvSettingManager

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class StartButtonOverlayService : Service() {
    private var view: View? = null
    private var wmManager: WindowManager? = null
    private val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if(checkAPILevel(Build.VERSION_CODES.O)) TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
    )
    private val listener = OnTranslaterStartListener()

    override fun onCreate() {
        super.onCreate()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.view = inflater.inflate(R.layout.overlay_start_button, null)

        this.params.gravity = Gravity.LEFT or Gravity.TOP
        this.params.alpha = 0.85f
        this.wmManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 시작 버튼을 찾습니다.
        val startButton = this.view?.findViewById<ImageButton>(R.id.overlay_start)
        startButton?.setOnTouchListener(listener)

        // 시작 버튼 배경색과 크기를 바꿉니다.
        try {
            val env = EnvSettingManager.getInstance(this)
            val bgDrawable = startButton?.background as GradientDrawable
            bgDrawable.setColor(Color.parseColor(env.read(EnvSettingKeys.OVERLAY_BUTTON_BG_COLOR)))

            val buttonSizeMulti = env.read(EnvSettingKeys.OVERLAY_BUTTON_SIZE).toInt() + 1
            val size = buttonSizeMulti * 30
            val params = startButton.layoutParams
            params.height = size
            params.width = size

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(SystemOverlay.isGrantedSystemOverlay(this)) {
            if (intent != null) {
                visible()
            }
        } else {
            // Overlay 권한이 없다면 중지한다.
            Toast.makeText(this, "Overlay permission is deny.", Toast.LENGTH_SHORT).show()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            this.wmManager?.removeView(this.view)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?) = null

    /**
     * Functions
     */
    private fun visible() {
        try {
            this.wmManager?.addView(this.view, params)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    internal inner class OnTranslaterStartListener : View.OnTouchListener {
        private var touchX: Float = 0.toFloat()
        private var touchY: Float = 0.toFloat()
        private var viewX: Int = 0
        private var viewY: Int = 0

        fun onClick() {
            if (!ScreenTranslate.isStartScreenTranslate(applicationContext)) return

            // 화면 캡쳐 서비스를 호출한다.
            val captureService = Intent(applicationContext, DisplayCaptureService::class.java)
            captureService.putExtra("command", "capture")
            startService(captureService)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val x = (event.rawX - this.touchX).toInt()
            val y = (event.rawY - this.touchY).toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    this.touchX = event.rawX
                    this.touchY = event.rawY
                    this.viewX = params.x
                    this.viewY = params.y
                }

                MotionEvent.ACTION_UP ->
                    // 실제로 버튼을 터치한건지 봅니다.
                    if (Math.abs(x) < 10 || Math.abs(y) < 10) {
                        onClick()
                    }

                MotionEvent.ACTION_MOVE -> {
                    params.x = this.viewX + x
                    params.y = this.viewY + y
                    wmManager?.updateViewLayout(view, params)
                }
            }
            return true
        }
    }
}

fun getStartButtonOverlayIntent(context: Context) = Intent(context, StartButtonOverlayService::class.java)