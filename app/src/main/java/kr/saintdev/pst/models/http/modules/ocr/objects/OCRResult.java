package kr.saintdev.pst.models.http.modules.ocr.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-19
 */

public class OCRResult {
    private boolean isErrorOccurred = false;    // 오류 발생 여부
    private String errorMessage = null;         // 오류 이유

    private String language = null;                 // 탐색된 언어
    ArrayList<OCRLine> lines = new ArrayList<>();   // 이미지 내 라인
    private String sentence = null;

    public void setSuccess(JSONObject response) throws JSONException {
        this.isErrorOccurred = false;
        this.language = response.getString("language");

        StringBuilder sentenceBuilder = new StringBuilder();
        JSONArray regions = response.getJSONArray("regions");

        for(int i = 0; i < regions.length(); i ++) {
            // 단락 하나를 가져옵니다.
            JSONObject region = regions.getJSONObject(i);

            // 문장 하나를 가져옵니다.
            JSONArray regionInLines = region.getJSONArray("lines");
            for(int j = 0; j < regionInLines.length() ; j ++) {
                JSONObject line = regionInLines.getJSONObject(j);

                OCRLine ocrLine = new OCRLine(line, isUseSpacingWord());
                this.lines.add(ocrLine);

                sentenceBuilder.append(ocrLine.getSentence());
                sentenceBuilder.append('\n');
            }
        }

        this.sentence = sentenceBuilder.toString();
    }

    public void setFailed(String errorMessage) {
        this.isErrorOccurred = true;
        this.errorMessage = errorMessage;
    }

    public boolean isErrorOccurred() {
        return isErrorOccurred;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLanguage() {
        return language;
    }

    public ArrayList<OCRLine> getLines() {
        return lines;
    }

    public String getSentence() {
        return this.sentence;
    }

    private boolean isUseSpacingWord() {
        String[] notSupportedSpacingWordLanguages = {
                "ja",
                "zh-Hans",
                "zh-Hant"
        };

        for(int i = 0; i < notSupportedSpacingWordLanguages.length; i ++) {
            if(notSupportedSpacingWordLanguages[i].equals(this.language)) {
                return false;
            }
        }

        return true;
    }
}
