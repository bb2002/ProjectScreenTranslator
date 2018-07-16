package kr.saintdev.pst.vnc.activity.control;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.http.modules.ocr.OCRExecuter;
import kr.saintdev.pst.models.http.modules.ocr.objects.OCRResult;
import kr.saintdev.pst.models.http.modules.translate.TranslateExecuter;
import kr.saintdev.pst.models.http.modules.translate.TranslateIntent;
import kr.saintdev.pst.models.http.modules.translate.TranslateResult;
import kr.saintdev.pst.models.http.modules.tts.TTS;
import kr.saintdev.pst.models.http.modules.tts.TTSObject;
import kr.saintdev.pst.models.libs.async.BackgroundWork;
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener;
import kr.saintdev.pst.models.libs.manager.RepositoryKey;
import kr.saintdev.pst.models.libs.manager.RepositoryManager;
import kr.saintdev.pst.vnc.activity.view.TranslateActivity;
import kr.saintdev.pst.vnc.dialog.message.LanguageSelectDialog;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * TranslateActivity 에 대한 컨트롤러
 * @Date 2018-06-07
 */

public class TranslateActivityControl extends SuperControl {
    private TranslateActivity view = null;

    // Views
    private EditText beforeTextEditor   = null;     // 번역 전 문장 에디터
    private TextView afterTextView = null;          // 번역 후 텍스트 뷰
    private Button inputSelectButton = null;        // 입력 언어 선택
    private Button outputSelectButton = null;       // 출력 언어 선택
    private ImageButton[] beforeTextOptions = null;       // 번역 전 텍스트에 대한 옵션
    private ImageButton[] afterTextOptions = null;        // 번역 후 텍스트에 대한 옵션
    // Views end

    private String[] supportLanguageCodes = null;       // 지원하는 언어의 코드 값
    private String[] supportLanguageNames = null;       // 지원하는 언어의 이름
    private RepositoryManager repositoryManager = null; // 앱 내 Key Value 저장 공간

    private OnButtonClickEvent eventHandler = null;
    private OnBackgroundHandler backgroundHandler = null;

    private LanguageSelectDialog languageSelectDialog = null;
    private int inputLanguagePosition = 0;      // 입력 언어 인덱스
    private int outputLanguagePosition = 1;     // 츨력 언어 인덱스
    private boolean isLastTranslateSessionSuccess = false;  // 마지막 번역 세션이 성공 했는가?

    private static final int REQUEST_OCR = 0x0;         // OCR 처리
    private static final int REQUEST_TRANSLATE = 0x1;   // 번역기
    private static final int REQUEST_TTS = 0x2;         // TTS 처리
    public static final int REQUEST_STT = 0x3;         // 음성인식

    public TranslateActivityControl(TranslateActivity view) {
        super(view);
        this.view = view;

        this.eventHandler = new OnButtonClickEvent();
        this.backgroundHandler = new OnBackgroundHandler();

        // 에셋을 불러옵니다.
        this.repositoryManager = RepositoryManager.Companion.getInstance(view);
        this.supportLanguageCodes = view.getResources().getStringArray(R.array.supported_languages_v2_codes);
        this.supportLanguageNames = view.getResources().getStringArray(R.array.supported_languages_v2_names);

        this.languageSelectDialog = new LanguageSelectDialog(view);
    }

    @Override
    public void onCreate(@Nullable ViewGroup container) {
        // View 내 객체를 찾습니다.
        this.beforeTextEditor = view.findViewById(R.id.tran_original_view);
        this.afterTextView = view.findViewById(R.id.tran_converted_view);
        this.inputSelectButton = view.findViewById(R.id.tran_input_language);
        this.outputSelectButton = view.findViewById(R.id.tran_output_language);
        this.beforeTextOptions = new ImageButton[] {
                view.findViewById(R.id.tran_original_nav_speech),
                view.findViewById(R.id.tran_original_nav_mic)
        };
        this.afterTextOptions = new ImageButton[] {
                view.findViewById(R.id.tran_converted_nav_speech),
                view.findViewById(R.id.tran_converted_nav_copy)
        };

        // 이벤트를 등록합니다.
        setOnClickHandler(new View[]{
                this.inputSelectButton,
                this.outputSelectButton,
                this.beforeTextOptions[0],
                this.beforeTextOptions[1],
                this.afterTextOptions[0],
                this.afterTextOptions[1]
        });
        this.beforeTextEditor.addTextChangedListener(new OnTextChangeHandler());
    }

    @Override
    public void onResume() {
        // 기본값을 설정합니다.
        this.inputLanguagePosition =
                Integer.parseInt(repositoryManager.getHashValue(RepositoryKey.INPUT_LANGUAGE));
        this.outputLanguagePosition =
                Integer.parseInt(repositoryManager.getHashValue(RepositoryKey.OUTPUT_LANGUAGE));
        updateButtonText();
    }

    @Override
    public void onStop() {

    }

    /**
     * 클릭 이벤트 핸들러
     */
    class OnButtonClickEvent implements View.OnClickListener, DialogInterface.OnDismissListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.tran_input_language:      // 입력 언어 변경
                case R.id.tran_output_language:     // 출력 언어 변경
                    openLanguageChangeDialog(v);
                    break;
                case R.id.tran_original_nav_speech:     // 번역 전 문장을 읽습니다.
                case R.id.tran_converted_nav_speech:    // 번역된 문장을 읽습니다.
                    readTextForTTS(v);
                    break;
                case R.id.tran_original_nav_mic:        // 음성인식을 통해 새 문장을 입력합니다.
                    writeTextForSTT();
                    break;
                case R.id.tran_converted_nav_copy:      // 번역된 문장을 복사합니다.
                    copyTextInAfterView();
                    break;
            }
        }

        /**
         * 언어 선택이 완료되었다면, 다시 해당 문장을 번역합니다.
         */
        @Override
        public void onDismiss(DialogInterface dialog) {
            int id = (int) languageSelectDialog.getTag();
            int idx = languageSelectDialog.getLanguageIdx();

            if(idx != -1) {     // 번역 대상 언어가 선택된 상태
                if (id == R.id.tran_input_language) inputLanguagePosition = idx; // 입력 언어 변경
                else    outputLanguagePosition = idx; // 출력 언어 변경

                updateButtonText();     // 버튼 내 값을 업데이트 한다.
                executeTranslate();     // 번역기를 재구동 한다.
            }
        }


        /**
         * 언어 선택 대화 창
         * @param v View 객체
         */
        private void openLanguageChangeDialog(View v) {

            languageSelectDialog.setTag(v.getId());
            languageSelectDialog.setOnDismissListener(this);
            languageSelectDialog.show();

            if(v.getId() == R.id.tran_output_language) {
                languageSelectDialog.setBlockAutoDetect(true);
            }
        }

        /**
         * TTS 번역 처리를 제공하는 함수
         * @param v View 객체
         */
        private void readTextForTTS(View v) {
            int idx = (v.getId() == R.id.tran_original_nav_speech ?
                    inputLanguagePosition : outputLanguagePosition);
            String sentence = (v.getId() == R.id.tran_original_nav_speech ?
                    beforeTextEditor.getText().toString() : afterTextView.getText().toString());

            if(v.getId() == R.id.tran_converted_nav_speech) {
                if(isLastTranslateSessionSuccess) {
                    if (sentence.length() != 0) executeTTS(sentence, idx);
                }
            } else {
                if (sentence.length() != 0) executeTTS(sentence, idx);
            }
        }

        /**
         * 음성인식을 제공하는 함수
         */
        private void writeTextForSTT() {
            Intent stt = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            stt.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            stt.putExtra(RecognizerIntent.EXTRA_PROMPT, view.getString(R.string.translate_stt_prompt));
            stt.putExtra(RecognizerIntent.EXTRA_LANGUAGE, supportLanguageCodes[inputLanguagePosition]);
            view.startActivityForResult(stt, REQUEST_STT);
        }

        /**
         * 현재 결과 창에 저장된 텍스트를 복사하는 함수
         */
        private void copyTextInAfterView() {
            if(isLastTranslateSessionSuccess) {
                ClipboardManager clipboard = (ClipboardManager) view.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Project ScreenTranslate", afterTextView.getText());
                clipboard.setPrimaryClip(data);

                Toast.makeText(view, view.getString(R.string.translate_copied), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view, view.getString(R.string.translate_last_session_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 백그라운드 처리 핸들러
     */
    class OnBackgroundHandler implements OnBackgroundWorkListener {
        @Override
        public void onSuccess(int requestCode, BackgroundWork worker) {
            switch (requestCode) {
                case REQUEST_OCR:               // OCR 요청 처리가 완료 됨.
                    closeProgressWindow();

                    OCRResult ocrResult = (OCRResult) worker.getResult();
                    if(ocrResult.isErrorOccurred()) {
                        // OCR 처리에 실패헀을 경우
                        String title = view.getString(R.string.error_msg_title_fatal);
                        String msg = ocrResult.getErrorMessage();
                        showDialog(title, msg);
                    } else {
                        // OCR 작업 완료.
                        String sentence = ocrResult.getSentence();

                        if(sentence == null || sentence.length() == 0) {
                            showDialog(R.string.error_msg_title_warning, R.string.translate_none_text_error);
                        } else {
                            beforeTextEditor.setText(sentence);
                        }
                    }
                    break;

                case REQUEST_TRANSLATE:
                    // 번역을 실행했습니다.
                    TranslateResult result = (TranslateResult) worker.getResult();
                    isLastTranslateSessionSuccess = false;

                    if(result.isSuccess()) {
                        isLastTranslateSessionSuccess = true;

                        TranslateResult.TranslateResultObject translatedObject = result.getResultArray()[0];
                        showMessageInResultWindow(translatedObject.getTranslatedArray()[0], false);
                    } else {
                        isLastTranslateSessionSuccess = false;

                        showMessageInResultWindow(result.getErrorMessage(), true);
                    }
                    break;
                case REQUEST_TTS:                   // TTS 를 통해 텍스트를 읽습니다.
                    closeProgressWindow();

                    TTSObject ttsObj = (TTSObject) worker.getResult();
                    if(ttsObj.isSuccess()) {
                        // 이 MediaFile 을 재생합니다.
                        try {
                            playMP3Media(ttsObj.getTtsFilePath());
                        } catch(IOException ex) {
                            showDialog(view.getString(R.string.error_msg_title_fatal), ex.getLocalizedMessage());
                        }
                    } else {
                        showDialog(view.getString(R.string.error_msg_title_fatal), ttsObj.getErrorDetail());
                    }
                    break;
            }
        }

        @Override
        public void onFailed(int requestCode, Exception ex) {
            // HTTP 요청 중 오류가 발생했을 경우
            closeProgressWindow();
            showDialog(
                    "Error! " + requestCode,
                    view.getString(R.string.error_msg_content_common) + "\n" + ex.getLocalizedMessage()
            );
        }
    }

    /**
     * TextWatcher 을 설정한다.
     */
    class OnTextChangeHandler implements TextWatcher {
        Timer timer = null;
        RefreshHandler handler = null;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(handler == null) {
                this.handler = new RefreshHandler();
            }

            if(timer == null || timer.getState() == Thread.State.TERMINATED) {
                timer = new Timer(this.handler);
                timer.start();
            } else {
                timer.reset();
            }

            showMessageInResultWindow(view.getString(R.string.entity_dotdotdot), false);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        /**
         * 텍스트 변경이 가해진 후, 2초간 수정이 없었을 경우
         */
        class RefreshHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                executeTranslate(); // 번역기 를 재구성 합니다.
            }
        }

        /**
         * 변경이 가해진 후 2초를 계산하는 타이머
         */
        class Timer extends Thread {
            int time = 2;
            Handler handler = null;

            public Timer(Handler handler) {
                this.handler = handler;
            }

            public void reset() {
                time = 2;
            }

            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                        time --;
                    } catch (Exception ex) {
                        return;
                    }

                    if (time == 0) {
                        handler.sendEmptyMessage(0);
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 오디오 재생이 끝날 경우 핸들러
     */
    class AudioPlayCompletelHandler implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            beforeTextOptions[0].setVisibility(View.VISIBLE);
            afterTextOptions[0].setVisibility(View.VISIBLE);
        }
    }

    /**
     * 대화창을 엽니다.
     * @param title     윈도우 제목
     * @param message   윈도우 내용
     */
    public void showDialog(String title, String message) {
        showDialogWindow(title, message, true, false, 1,1);
    }

    public void showDialog(int title, int message) {
        showDialog(view.getString(title), view.getString(message));
    }

    /**
     * File 을 재생합니다.
     */
    private MediaPlayer player = null;
    public void playMP3Media(File file) throws IOException {
        if(player == null) {
            this.player = new MediaPlayer();
            this.player.setOnCompletionListener(new AudioPlayCompletelHandler());
        } else {
            this.player.reset();
        }

        this.player.setDataSource(file.getAbsolutePath());
        this.player.prepare();
        this.player.start();

        // tts 파일 재생이 시작되었다면, 더이상 누를수 없도록 막는다.
        afterTextOptions[0].setVisibility(View.INVISIBLE);
        beforeTextOptions[0].setVisibility(View.INVISIBLE);
    }

    /**
     * OCR 분석을 실행합니다.
     * @param image 이 이미지를 ocr 을 통해 광학 이미지 분석을 실행합니다.
     */
    public void executeOCR(File image) {
        showProgressWindow();

        OCRExecuter ocr = new OCRExecuter(image, REQUEST_OCR, this.backgroundHandler, view);
        ocr.execute();
    }

    /**
     * 현재 Editor 에 있는 Text 를 inputLanguageIndex 와 outputLanguageIndex 값을 참고하여
     * 번역을 실행합니다.
     */
    public void executeTranslate() {
        String source = this.supportLanguageCodes[inputLanguagePosition];
        String target = this.supportLanguageCodes[outputLanguagePosition];
        String text = beforeTextEditor.getText().toString();

        if(text.length() != 0) {
            showMessageInResultWindow(view.getString(R.string.entity_progress_translate), false);

            // 번역 의도를 만듭니다.
            TranslateIntent translateIntent = new TranslateIntent(target);
            translateIntent.addRequestTranslate(source, text);

            TranslateExecuter executer = new TranslateExecuter(translateIntent, view, REQUEST_TRANSLATE, backgroundHandler);
            executer.execute();
        } else {
            // 문장이 전혀 없을 경우
            afterTextView.setText("");
        }
    }

    /**
     *
     * @param sentence              읽을 문장
     * @param targetLanguageIdx     sentence 의 언어
     */
    private void executeTTS(String sentence, int targetLanguageIdx) {
        showProgressWindow();

        TTS tts = new TTS(sentence, targetLanguageIdx, REQUEST_TTS, backgroundHandler, view);
        tts.execute();
    }

    /**
     * 번역기 결과창에 메세지를 띄웁니다.
     */
    public void showMessageInResultWindow(String msg, boolean isError) {
        if(isError) {
            this.afterTextView.setTextColor(view.getResources().getColor(R.color.colorRed));
        } else {
            this.afterTextView.setTextColor(view.getResources().getColor(R.color.colorPrimaryDark));
        }

        this.afterTextView.setText(msg);
    }

    /**
     * 현재 입/출력 인덱스를 기반하여 언어 변경 버튼 내 텍스트를 바꾼다.
     */
    private void updateButtonText() {
        this.inputSelectButton.setText(supportLanguageNames[inputLanguagePosition]);
        this.outputSelectButton.setText(supportLanguageNames[outputLanguagePosition]);
    }

    /**
     *
     * @param views OnClick 이벤트를 처리할 View
     */
    public void setOnClickHandler(View[] views) {
        for(View v : views) {
            v.setOnClickListener(this.eventHandler);
        }
    }

    /**
     * 번역 전 문장 입력기에 새로운 문장을 생성합니다.
     */
    public void addNewTextInEditor(String text) {
        CharSequence now = beforeTextEditor.getText();
        String newText = now.toString() + text + "\n";
        this.beforeTextEditor.setText(newText);
    }

    /**
     * 모든 자원을 회수합니다.
     */
    public void release() {
        if(player != null) {
            player.release();
        }
    }
}
