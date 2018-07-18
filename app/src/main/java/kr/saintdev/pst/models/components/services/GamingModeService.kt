package kr.saintdev.pst.models.components.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.widget.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.http.modules.ocr.OCRExecuter
import kr.saintdev.pst.models.http.modules.ocr.objects.OCRLine
import kr.saintdev.pst.models.http.modules.ocr.objects.OCRResult
import kr.saintdev.pst.models.http.modules.translate.TranslateExecuter
import kr.saintdev.pst.models.http.modules.translate.TranslateIntent
import kr.saintdev.pst.models.http.modules.translate.TranslateResult
import kr.saintdev.pst.models.libs.DeviceControl
import kr.saintdev.pst.models.libs.SystemOverlay
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.EnvSettingKeys
import kr.saintdev.pst.models.libs.manager.EnvSettingManager
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import java.io.File
import java.lang.Exception

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class GamingModeService : Service() {
    private var view: View? = null                                      // View
    private var overlayView: OverlayView? = null                        // Overlay view
    private var controllerView: ControllerView? = null                  // Controller view
    private var wm: WindowManager? = null                               // WindowManager

    // Control 에 대한 View
    private var backgroundImage: ImageView? = null                      // 캡쳐된 이미지를 띄우는 뷰
    private var inputSpinner: Spinner? = null                           // 입력 언어 선택
    private var outputSpinner: Spinner? = null                          // 출력 언어 선택
    private var logcatView: TextView? = null                            // 화면 중간에 로그 출력
    private var overlayContainer: FrameLayout? = null                   // 번역 결과를 보여줄 오버레이 컨테이너

    // data
    private val REQUEST_OCR = 0x0                                       // OCR 요청
    private val REQUEST_TRANSLATE = 0x1                                 // 번역 요청
    private val repoManager = RepositoryManager.getInstance(this)
    private var supportLanguageCodes: Array<String>? = null             // 지원하는 언어 코드
    private val params = WindowManager.LayoutParams(                    // WindowParams
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if(DeviceControl.checkAPILevel(Build.VERSION_CODES.O)) TYPE_APPLICATION_OVERLAY else TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT)
    private var cacheImage: String? = null                              // 캐시 이미지
    private val ocrResultData: ArrayList<OCRLine> = arrayListOf()       // OCR 결과 데이터
    private val translateResultData: ArrayList<OCRLine> = arrayListOf() // Translate 결과 데이터 (기본값은 OCR 과 같다)

    // setting
    private var inputLanguageIndex = 0      // Unk 에서
    private var outputLanguageIndex = 1     // kor 으로
    private var isOverlayAvailable = true  // Overlay 작동 여부
    private var overlayBackgroundColor = Color.BLACK    // 오버레이 뷰 텍스트 배경 색
    private var overlayTextColor = Color.WHITE          // 오버레이 뷰 텍스트 색

    override fun onCreate() {
        super.onCreate()

        this.wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.overlay_gaming_view, null, false) // 메인 뷰를 가져온다.
        this.controllerView = ControllerView(this)          // 컨트롤러 뷰를 불러온다.
        this.supportLanguageCodes = resources.getStringArray(R.array.supported_languages_v2_codes)

        if (v != null) {
            this.backgroundImage = v.findViewById(R.id.gaming_background_image)
            this.logcatView = v.findViewById(R.id.gaming_logcat)
            this.inputSpinner = controllerView?.findViewById(R.id.gaming_input_spinner)
            this.outputSpinner = controllerView?.findViewById(R.id.gaming_output_spinner)
            this.overlayContainer = v.findViewById(R.id.gaming_overlay_container)

            this.view = v

            // WindowManager 에 추가합니다.
            this.wm?.addView(this.view, params)
            this.wm?.addView(this.controllerView, params)
        }

        this.inputLanguageIndex = repoManager.getHashValue(RepositoryKey.INPUT_LANGUAGE)!!.toInt()
        this.outputLanguageIndex = repoManager.getHashValue(RepositoryKey.OUTPUT_LANGUAGE)!!.toInt()

        // Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.supported_languages_v2_names, android.R.layout.simple_spinner_dropdown_item)
        if (inputSpinner != null && outputSpinner != null) {
            inputSpinner!!.adapter = adapter
            outputSpinner!!.adapter = adapter
            inputSpinner!!.setSelection(inputLanguageIndex, false)
            outputSpinner!!.setSelection(outputLanguageIndex, false)
            inputSpinner!!.onItemSelectedListener = onInputLangaugeSpinnerChangeListener
            outputSpinner!!.onItemSelectedListener = onOutputLangaugeSpinnerChangeListener
        }

        // Env setting 을 가져온다.
        try {
            val env = EnvSettingManager.getInstance(this)

            this.overlayBackgroundColor = Color.parseColor(env.read(EnvSettingKeys.GAMING_BG_COLOR))
            this.overlayTextColor = Color.parseColor(env.read(EnvSettingKeys.GAMING_TEXT_COLOR))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(SystemOverlay.isGrantedSystemOverlay(this)) {
            if (intent != null) {
                this.cacheImage = intent.getStringExtra("image")
                val bitmap = BitmapFactory.decodeFile(cacheImage)
                this.backgroundImage?.setImageBitmap(bitmap)

                // 결과 뷰를 띄웁니다.
                this.overlayView = OverlayView(this)
                this.overlayContainer?.addView(this.overlayView)
                requestOCR()
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

        if(this.view != null) wm?.removeView(this.view)
        if(this.controllerView != null) wm?.removeView(this.controllerView)

        this.isOverlayAvailable = false
    }

    override fun onBind(intent: Intent?) = null

    /**
     * 번역 결과를 보여줄 OverlayView
     */
    inner class OverlayView(context: Context) : View(context) {
        private val textPaint = Paint()
        private val bgPaint = Paint()

        init {
            this.textPaint.isAntiAlias = true
            this.textPaint.color = overlayTextColor


            this.bgPaint.color = overlayBackgroundColor
            this.bgPaint.style = Paint.Style.FILL
            this.bgPaint.alpha = 160
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            if(canvas != null) {
                for (line in translateResultData) {
                    // OCR 문장을 가져온다.
                    val lt = line.leftTop
                    val size = line.size

                    // 폰트 크기를 적용
                    this.textPaint.textSize = size.y.toFloat()

                    // 배경색 그리기
                    val rect = Rect(lt.x, lt.y - size.y - 4, lt.x + size.x + 4, lt.y + 4)
                    canvas.drawRect(rect, this.bgPaint)
                    canvas.drawText(line.sentence, lt.x.toFloat(), lt.y.toFloat(), this.textPaint)
                }
            }
        }
    }

    /**
     * KeyEvent 를 잡을 뷰
     */
    inner class ControllerView(context: Context) : RelativeLayout(context) {
        init {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.overlay_controller_view, null, false)
            addView(view)
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if(event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
                stopSelf()
            }

            return false
        }
    }

    /**
     * Functions
     */

    fun requestOCR() {
        ocrResultData.clear()       // OCR 데이터 초기화
        printLog(R.string.entity_progress_ocr.str())

        val ocrExecute = OCRExecuter(File(this.cacheImage), REQUEST_OCR, onBackgroundTaskCallback, this, inputLanguageIndex)
        ocrExecute.execute()
    }

    // OCR Data 를 기반으로 번역을 실행합니다.
    fun requestTranslate() {
        translateResultData.clear()
        translateResultData.addAll(ocrResultData)
        printLog(R.string.entity_progress_translate.str())

        val translateIntent = TranslateIntent(supportLanguageCodes!![outputLanguageIndex])

        when {
            translateResultData.size > 25 -> printLog(R.string.error_msg_translate_v2_gaming_line_overflow.str())
            translateResultData.size == 0 -> printLog(R.string.translate_last_session_error.str())
            else -> {
                (0 until translateResultData.size)
                        .map { ocrResultData[it] }
                        .forEach { translateIntent.addRequestTranslate(supportLanguageCodes!![outputLanguageIndex], it.sentence) }

                // 번역을 실행한다.
                val translator = TranslateExecuter(translateIntent, this@GamingModeService, REQUEST_TRANSLATE, onBackgroundTaskCallback)
                translator.execute()
            }
        }
    }

    fun printLog(msg: String?) {
        if(msg == null) {
            this.logcatView?.text = ""
            this.logcatView?.visibility = View.INVISIBLE
        } else {
            this.logcatView?.visibility = View.VISIBLE
            this.logcatView?.text = msg
        }
    }

    /**
     * 입력 언어 변경 시 이벤트 리스너
     */
    private val onInputLangaugeSpinnerChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            inputLanguageIndex = position
            requestOCR()
        }
    }

    /**
     * 출력 언어 변경 시 이벤트 리스너
     */
    private val onOutputLangaugeSpinnerChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if(position == 0) {
                // 자동으로는 번역 할 수 없습니다.
                Toast.makeText(this@GamingModeService, R.string.translate_auto_not_supported.str(), Toast.LENGTH_SHORT).show()
                outputSpinner?.setSelection(outputLanguageIndex, false)
            } else {
                outputLanguageIndex = position
                requestTranslate()
            }
        }
    }

    private val onBackgroundTaskCallback = object : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            if(!isOverlayAvailable) return

            val result = worker.result
            when(result) {
                is OCRResult -> ocr(result)
                is TranslateResult -> translate(result)
                else -> printLog("An error occurred.\nCasting failed.")
            }
        }

        override fun onFailed(requestCode: Int, ex: Exception) {
            printLog("${R.string.error_msg_content_common.str()}\n${ex.message}")
        }

        private fun ocr(ocrResult: OCRResult) {
            printLog(null)

            if(!ocrResult.isErrorOccurred) {
                ocrResultData.addAll(ocrResult.lines)           // OCR 결과값을 저장합니다.
                overlayView?.invalidate()

                requestTranslate()              // 번역을 요청합니다.
            } else {
                printLog(ocrResult.errorMessage)
            }
        }

        /**
         * translate result 값을 저장하고, 화면에 그린다.
         */
        private fun translate(translateResult: TranslateResult) {
            printLog(null)

            if(translateResult.isSuccess) {
                val resultArray = translateResult.resultArray

                if(resultArray.size == translateResultData.size) {
                    for(i in 0 until resultArray.size) {
                        val line = translateResultData[i]

                        val result = resultArray[i]
                        line.sentence = result.translatedArray[0]
                    }

                    overlayView?.invalidate()
                } else {
                    // 모순이 발생했습니다.
                    printLog(R.string.error_msg_translate_v2_length_not_match.str())
                }
            } else {
                printLog(translateResult.errorMessage)
            }
        }
    }

    fun Int.str() = getString(this)
    fun Int.color() = resources.getColor(this)
}