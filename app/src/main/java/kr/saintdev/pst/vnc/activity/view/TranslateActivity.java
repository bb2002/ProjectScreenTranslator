package kr.saintdev.pst.vnc.activity.view;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import kr.saintdev.pst.R;
import kr.saintdev.pst.vnc.activity.control.TranslateActivityControl;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-05-23
 */

public class TranslateActivity extends AppCompatActivity {
    File imageFile = null;      // OCR 처리할 이미지
    TranslateActivityControl control = null;    // 이 엑티비티에 대한 Control

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        getSupportActionBar().hide();

        // OCR 처리할 이미지를 불러온다.
        String path = getIntent().getStringExtra("image");

        if(path == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_msg_cache_image_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            this.imageFile = new File(path);
            if(!imageFile.exists()) {
                // 존재하지 않는 이미지.
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_cache_image_error), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        this.control = new TranslateActivityControl(this);
        this.control.onCreate(null);
        control.executeOCR(imageFile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.control.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        control.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TranslateActivityControl.REQUEST_STT && resultCode == RESULT_OK) {
            // STT 음성인식에 대한 결과
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(result.size() == 0) {
                control.showDialog(R.string.error_msg_title_warning, R.string.translate_none_text_error);
            } else {
                String text = result.get(0);
                control.addNewTextInEditor(text);
            }
        }
    }
}
