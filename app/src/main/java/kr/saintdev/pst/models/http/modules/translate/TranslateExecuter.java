package kr.saintdev.pst.models.http.modules.translate;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.consts.HostKt;
import kr.saintdev.pst.models.http.HttpRequester;
import kr.saintdev.pst.models.http.HttpResponseObject;
import kr.saintdev.pst.models.libs.async.BackgroundWork;
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener;
import kr.saintdev.pst.models.libs.manager.RepositoryKey;
import kr.saintdev.pst.models.libs.manager.RepositoryManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-19
 */

public class TranslateExecuter extends BackgroundWork<TranslateResult> {
    private Context context = null;
    private TranslateIntent intent = null;

    public TranslateExecuter(TranslateIntent intent, Context context, int requestCode, OnBackgroundWorkListener listener) {
        super(requestCode, listener);
        this.context = context;
        this.intent = intent;
    }

    @Override
    protected TranslateResult script() throws Exception {
        HttpResponseObject availableTranslateResponse = checkAvailableTranslate();
        TranslateResult translateResult = new TranslateResult();

        if(availableTranslateResponse.isSuccess()) {
            // API 사용이 가능한 상태
            String apiSecret = availableTranslateResponse.getMessage().getString("api-secret");

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

            // 파라미터를 정의합니다.
            String param = addParams();

            // 번역 데이터를 준비합니다.
            String translateData = createTranslateData();
            RequestBody translateDataBody = RequestBody.create(MediaType.parse("application/json"), translateData);

            // Request Builder 를 만든다.
            Request.Builder reqBuilder = new Request.Builder();
            reqBuilder.addHeader("Ocp-Apim-Subscription-Key", apiSecret);
            reqBuilder.url(HostKt.BING_TRANSLATE_API + param);
            reqBuilder.post(translateDataBody);             // Data 를 보냅니다.

            // Request
            Response response = client.newCall(reqBuilder.build()).execute();
            String responseJSONEncoded = response.body().string();

            // 서버 응답을 확인한다.
            if(response.code() == 200) {
                // API 요청에 성공했습니다.

                JSONArray responseArray = new JSONArray(responseJSONEncoded);
                translateResult.setSuccess(responseArray);
            } else {
                // API 요청에 실패했습니다.

                String errorMessage;
                switch (response.code()) {
                    case 400:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_400);
                        break;
                    case 401:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_401);
                        break;
                    case 403:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_403);
                        break;
                    case 429:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_429);
                        break;
                    case 500:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_500);
                        break;
                    case 503:
                        errorMessage = context.getString(R.string.error_msg_translate_v2_503);
                        break;
                    default:
                        errorMessage = context.getString(R.string.error_msg_content_common);
                }

                translateResult.setFailed(errorMessage);
            }
        } else {
            // API 사용이 불가능한 상태
            String errorMessage;
            switch(availableTranslateResponse.getErrorCode()) {
                case 1:
                    errorMessage = context.getString(R.string.entity_permission_deny);
                    break;
                case 2:
                    errorMessage = context.getString(R.string.inode_exceeded);
                    break;
                default:
                    errorMessage = availableTranslateResponse.getErrorMessage();
            }

            translateResult.setFailed(errorMessage);
        }

        return translateResult;
    }

    private HttpResponseObject checkAvailableTranslate() throws Exception {
        // Sentence 의 길이를 구합니다.
        StringBuilder sentenceBuilder = new StringBuilder();
        ArrayList<TranslateIntent.TranslateIntentObject> objects = this.intent.getTranslateObjects();

        for(TranslateIntent.TranslateIntentObject o : objects) {
            sentenceBuilder.append(o.getText());
        }

        HashMap<String, Object> args = new HashMap<>();
        args.put("source", sentenceBuilder.toString());
        args.put("token", RepositoryManager.Companion.quicklyGet(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, context));

        HttpRequester requester =
                new HttpRequester(HostKt.TRANSLATE_VIA_SCT, args, 0x0, null);
        HttpResponseObject sctResponse = requester.script();    // 번역기 사용 가능 상태를 확인함

        return sctResponse;
    }

    /**
     *
     * @return       파라미터를 정의합니다.
     */
    private String addParams() {
        HashMap<String, String> param = intent.getTranslateParameter();
        param.put("to", this.intent.getToLanguage());

        StringBuilder paramBuilder = new StringBuilder();
        Iterator iterator = param.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            paramBuilder.append("&");
            paramBuilder.append((String) entry.getKey());
            paramBuilder.append("=");
            paramBuilder.append((String) entry.getValue());
        }

        return paramBuilder.toString();
    }

    /**
     *
     * @return      번역문을 JSON 방식으로 정의합니다.
     */
    private String createTranslateData() throws JSONException {
        ArrayList<TranslateIntent.TranslateIntentObject> translateIntentObjects = this.intent.getTranslateObjects();

        JSONArray jsonArray = new JSONArray();
        for(TranslateIntent.TranslateIntentObject object : translateIntentObjects) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Text", object.getText());

            jsonArray.put(jsonObject);
        }

        return jsonArray.toString();
    }
}
