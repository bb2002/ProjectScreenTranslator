package kr.saintdev.pst.models.http.modules.tts;

import java.io.File;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-05-15
 */

public class TTSObject {
//    private boolean isAuth = false;             // 인증 결과
//    private int resultCode = -1;                // 요청 코드
//    private boolean isErrorOccurred = false;    // 오류 발생?
//    private String errorMessage = null;         // 오류 메세지
//    private File ttsFilePath = null;          // 처리성공 파일 위치
//
//
//    public void setAuth(boolean auth) {
//        isAuth = auth;
//    }
//
//    public void setResultCode(int resultCode) {
//        this.resultCode = resultCode;
//    }
//
//    public void setErrorOccurred(boolean errorOccurred) {
//        isErrorOccurred = errorOccurred;
//    }
//
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }
//
//    public File getTtsFilePath() {
//        return ttsFilePath;
//    }
//
//    public void setTtsFilePath(File ttsFilePath) {
//        this.ttsFilePath = ttsFilePath;
//    }
//
//    public boolean isAuth() {
//        return isAuth;
//    }
//
//    public int getResultCode() {
//        return resultCode;
//    }
//
//    public boolean isErrorOccurred() {
//        return isErrorOccurred;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }

    private boolean isSuccess = false;
    private String errorMessage = null;
    private String errorDetail = null;
    private File ttsFilePath = null;

    /**
     * TTS 처리에 완벽한 성공을 거두었을 경우
     * @param ttsFilePath
     */
    public void responseSuccess(File ttsFilePath) {
        this.isSuccess = true;
        this.ttsFilePath = ttsFilePath;
    }

    /**
     * TTS 호출 중 오류가 발생헀습니다.
     * @param errorMessage  오류에 대한 간략한 내용
     * @param errorDetail   오류에 대한 자세한 내용
     */
    public void responseFailed(String errorMessage, String errorDetail) {
        this.errorMessage = errorMessage;
        this.errorDetail = errorDetail;
        this.isSuccess = false;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public File getTtsFilePath() {
        return ttsFilePath;
    }
}