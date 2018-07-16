package kr.saintdev.pst.models.http;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-03-29
 */

public class HttpResponseObject {
    private boolean isSuccess = false;      // 실행 성공 여부
    private int errorCode = -1;             // 오류 발생 시 코드값
    private String errorMessage = null;     //               메세지
    private String errorMissingPoint = null;       //        발생 위치
    private JSONObject message = null;      // 오류가 없을 경우 응답 값

    public HttpResponseObject(String json) throws JSONException {
        JSONObject response = new JSONObject(json);

        this.isSuccess = response.getBoolean("success");
        if(this.isSuccess) {
            // 성공했습니다.
            this.message = response.getJSONObject("message");
        } else {
            // 실패 했습니다.
            JSONObject errorObject = response.getJSONObject("errorObject");
            this.errorCode = errorObject.getInt("code");
            this.errorMissingPoint = errorObject.getString("missing");
            this.errorMessage = errorObject.getString("message");
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorMissingPoint() {
        return errorMissingPoint;
    }

    public JSONObject getMessage() {
        return message;
    }
}
