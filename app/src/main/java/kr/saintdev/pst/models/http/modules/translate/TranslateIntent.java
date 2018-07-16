package kr.saintdev.pst.models.http.modules.translate;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * 번역요청 인텐트
 * @Date 2018-06-19
 */

public class TranslateIntent {
    /**
     * 번역요청을 의미를 담고 있는 ArrayList
     */
    private ArrayList<TranslateIntentObject> translateObjects = null;
    private String to = null;       // 도달 언어

    /**
     * 번역 요청에 대한 설정값을 담고 있는 HashMap
     */
    private HashMap<String, String> translateParameter = null;

    public TranslateIntent(String to) {
        this.translateObjects = new ArrayList<>();
        this.translateParameter = new HashMap<>();
        this.to = to;
    }

    /**
     * 새로운 번역 요청 의도를 만듭니다.
     * @param from      요청 언어
     * @param text      문장
     */
    public void addRequestTranslate(@Nullable String from, String text) {
        TranslateIntentObject intentObject = new TranslateIntentObject(from, text);
        this.translateObjects.add(intentObject);
    }

    /**
     * 파라미터를 정의합니다.
     * @param key       키
     * @param action    액션
     */
    public void setParameter(String key, String action) {
        this.translateParameter.put(key, action);
    }

    /**
     *
     * @return  번역 의도 배열
     */
    public ArrayList<TranslateIntentObject> getTranslateObjects() {
        return translateObjects;
    }

    /**
     *
     * @return  정의된 파라미터 들
     */
    public HashMap<String, String> getTranslateParameter() {
        return translateParameter;
    }

    /**
     * 도달 언어를 가져옵니다.
     * @return  언어 코드
     */
    public String getToLanguage() {
        return this.to;
    }

    class TranslateIntentObject {
        private String from = null;     // 시작 언어
        private String text = null;     // 텍스트

        public TranslateIntentObject(@Nullable String from, String text) {
            this.from = from;
            this.text = text;
        }

        @Nullable
        public String getFrom() {
            return from;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * 욕설에 대한 처리
     * @date 06.19 2018
     */
    public static final String INTENT_BAD_LANGUAGE = "profanityAction";
    public static final String NO_ACTION = "NoAction";
    public static final String MARKED = "Marked";

    /**
     * 문장 구분을 사용할것인지 정의
     * @date 06.19 2018
     */
    public static final String INTENT_SENTENCE_LENGTH = "includeSentenceLength";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    /**
     * 문장에 형식
     */
    public static final String INTENT_TEXT_TYPE = "textType";
    public static final String PLAIN = "plain";
    public static final String HTML = "html";
}
