package kr.saintdev.pst.models.http.modules.translate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-19
 */

public class TranslateResult {
    private boolean isSuccess = false;
    private String errorMessage = null;
    private TranslateResultObject[] resultArray = null;

    public void setSuccess(JSONArray result) {
        this.isSuccess = true;

        try {
            this.resultArray = new TranslateResultObject[result.length()];

            for (int i = 0; i < result.length(); i++) {
                JSONObject j = result.getJSONObject(i);
                TranslateResultObject translatedObject = new TranslateResultObject();

                if(!j.isNull("detectedLanguage")) {
                    // 언어 감지 모드 사용
                    JSONObject detectObject = j.getJSONObject("detectedLanguage");
                    double score = detectObject.getDouble("score");
                    String detectLanguage = detectObject.getString("language");

                    translatedObject.setUseLanguageAutoDetectMode(detectLanguage, (float) score);
                }

                // 번역된 언어를 저장합니다.
                JSONObject translations = j.getJSONArray("translations").getJSONObject(0);
                translatedObject.setTranslatedContent(translations.getString("text"), translations.getString("to"));

                this.resultArray[i] = translatedObject;
            }
        } catch(JSONException jex) {
            this.isSuccess = false;
            this.errorMessage = jex.getMessage();
        }
    }

    public void setFailed(String errorMessage) {
        this.isSuccess = false;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public TranslateResultObject[] getResultArray() {
        return resultArray;
    }

    public class TranslateResultObject {
        private boolean isUseLanguageAutoDetectMode = false;
        private int score = 0;      // 퍼센트 단위, 최대 100
        private String detectLanguage = null;           // 감지된 언어

        private String translatedText = null;           // 번역된 본문
        private String translatedLanguage = null;       // 번역된 언어

        public void setUseLanguageAutoDetectMode(String detectLanguage, float score) {
            this.isUseLanguageAutoDetectMode = true;
            this.score = (int) score * 100;
            this.detectLanguage = detectLanguage;
        }

        public void setTranslatedContent(String text, String language) {
            this.translatedText = text;
            this.translatedLanguage = language;
        }

        // 언어 감지 모드 사용 여부
        public boolean isUseLanguageAutoDetectMode() {
            return isUseLanguageAutoDetectMode;
        }

        // 사용했다면, 점수
        public int getScore() {
            return score;
        }

        // 감지된 언어
        public String getDetectLanguage() {
            return detectLanguage;
        }

        public String[] getTranslatedArray() {
            return new String[]{ this.translatedText, this.translatedLanguage };
        }
    }
}
