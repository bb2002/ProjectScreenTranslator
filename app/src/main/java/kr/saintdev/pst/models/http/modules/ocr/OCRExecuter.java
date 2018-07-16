package kr.saintdev.pst.models.http.modules.ocr;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.consts.HostKt;
import kr.saintdev.pst.models.http.HttpRequester;
import kr.saintdev.pst.models.http.HttpResponseObject;
import kr.saintdev.pst.models.http.modules.ocr.objects.OCRResult;
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

public class OCRExecuter extends BackgroundWork<OCRResult> {
    private File image = null;
    private Context context = null;
    private String[] supportLanguages = null;
    private RepositoryManager repositoryManager = null;
    private int ocrLanguageIndex = -1;

    public OCRExecuter(File image, int requestCode, OnBackgroundWorkListener listener, Context context) {
        this(image, requestCode, listener, context, -1);
    }

    public OCRExecuter(File image, int requestCode, OnBackgroundWorkListener listener, Context context, int ocrLanguageIndex) {
        super(requestCode, listener);
        this.image = image;
        this.context = context;
        this.supportLanguages = context.getResources().getStringArray(R.array.supported_languages_v2_codes);
        this.repositoryManager = RepositoryManager.Companion.getInstance(context);
        this.ocrLanguageIndex = ocrLanguageIndex;
    }

    @Override
    protected void onPostExecute(OCRResult ocrResult) {
        super.onPostExecute(ocrResult);
    }

    @Override
    protected OCRResult script() throws Exception {
        HttpResponseObject ocrAvailable = checkAvailableOCR();
        OCRResult ocrResult = new OCRResult();

        if(ocrAvailable.isSuccess()) {
            // API 사용이 가능합니다.
            JSONObject message = ocrAvailable.getMessage();
            String apiSecret = message.getString("api-secret");

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            // 파라미터를 정의합니다.
            if(ocrLanguageIndex == -1) {
                this.ocrLanguageIndex = Integer.parseInt(repositoryManager.getHashValue(RepositoryKey.INPUT_LANGUAGE));
            }
            String targetLanguage =
                    this.supportLanguages[ocrLanguageIndex];    // OCR 대상의 언어

            String param = "?language=" + targetLanguage;

            // 이미지를 준비합니다.
            RequestBody imageData = RequestBody.create(MediaType.parse("application/octet-stream"), this.image);

            Request.Builder reqBuilder = new Request.Builder();
            reqBuilder.addHeader("Ocp-Apim-Subscription-Key", apiSecret);
            reqBuilder.url(HostKt.BING_OCR_API + param);
            reqBuilder.post(imageData);

            // Request
            Response response = client.newCall(reqBuilder.build()).execute();
            String responseJSONEncoded = response.body().string();

            if(response.code() == 200) {
                JSONObject responseObject = new JSONObject(responseJSONEncoded);
                ocrResult.setSuccess(responseObject);
            } else {
                JSONObject errorObject = new JSONObject(responseJSONEncoded);
                ocrResult.setFailed(errorObject.getString("message"));
            }
        } else {
            String errorMessage;
            switch(ocrAvailable.getErrorCode()) {
                case 1:     // 쿼터 초과 (사용량 초과)
                    errorMessage = context.getString(R.string.inode_exceeded);
                    break;
                case 2:     // 계정 오류 (인증 오류)
                    errorMessage = context.getString(R.string.entity_permission_deny);
                    break;
                case 4:     // 파라미터 부족 (요청 오류)
                    errorMessage = context.getString(R.string.error_msg_param_error);
                    break;
                default:
                    errorMessage = ocrAvailable.getErrorMessage();
            }

            ocrResult.setFailed(errorMessage);
        }

        return ocrResult;
    }

    private HttpResponseObject checkAvailableOCR() throws Exception {
        HashMap<String, Object> args = new HashMap<>();
        args.put("token", repositoryManager.getHashValue(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN));

        HttpRequester requester = new HttpRequester(HostKt.OCR_VIA_SCT, args, 0, null);
        return requester.script();    // 번역기 사용 가능 상태를 확인함
    }
}
