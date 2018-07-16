package kr.saintdev.pst.models.http.modules.tts;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.consts.HostKt;
import kr.saintdev.pst.models.http.HttpRequester;
import kr.saintdev.pst.models.http.HttpResponseObject;
import kr.saintdev.pst.models.libs.async.BackgroundWork;
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener;
import kr.saintdev.pst.models.libs.manager.RepositoryKey;
import kr.saintdev.pst.models.libs.manager.RepositoryManager;


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-05-15
 */

public class TTS extends BackgroundWork<TTSObject> {
    private String sentence;     // 번역 대상의 문자
    private int languageIdx;        // 해당 문자 언어 인덱스
    private Context context;

    public TTS(String sentence, int languageIndex, int requestCode, OnBackgroundWorkListener listener, Context context) {
        super(requestCode, listener);
        this.sentence = sentence;
        this.languageIdx = languageIndex;
        this.context = context;
    }

    @Override
    protected TTSObject script() throws Exception {
        /*
            TTS 호출 사용가용성 을 확인한다.
         */
        HashMap<String, Object> args = new HashMap<>();
        args.put("source", this.sentence);
        args.put("token", RepositoryManager.Companion.quicklyGet(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, context));

        HttpRequester requester =
                new HttpRequester(HostKt.TTS_VIA_SCT, args, 0x0, null);
        HttpResponseObject ttsAvailable = requester.script();    // 번역기 사용 가능 상태를 확인함
        TTSObject ttsObject = new TTSObject();

        if(ttsAvailable.isSuccess()) {
            // API 호출이 가능한 상황
            JSONObject message = ttsAvailable.getMessage();

            String apiID = message.getString("api-id");
            String apiSecret = message.getString("api-secret");

            // 요청 설정
            String vocaloid = selectVocaloid(this.languageIdx);

            if(vocaloid == null) {
                // 지원하지 않는 언어
                ttsObject.responseFailed(context.getString(R.string.error_msg_param_error), context.getString(R.string.error_msg_not_support_language));
            } else {
                // 지원합니다.
                // TTS API 호출을 정의합니다.
                // 변환할 문자열을 인코딩 합니다.
                String text = URLEncoder.encode(this.sentence, "UTF-8");
                URL url = new URL(HostKt.TTS_HOST);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // 해더 설정
                con.setRequestMethod("POST");
                con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", apiID);
                con.setRequestProperty("X-NCP-APIGW-API-KEY", apiSecret);

                // 요청 해더 설정
                String postParams = "speaker=" + vocaloid + "&speed=0&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;

                if (responseCode == 200) {
                    // 랜덤한 이름으로 mp3 파일 생성
                    String tempName = UUID.randomUUID().toString();
                    File f = new File(context.getCacheDir(), tempName);

                    if (f.createNewFile()) {
                        // 파일 생성
                        // API 서버에서 보내는 MP3 파일을 받아옵니다.
                        InputStream is = con.getInputStream();
                        int read = 0;
                        byte[] bytes = new byte[1024];

                        OutputStream outputStream = new FileOutputStream(f);
                        while ((read = is.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        is.close();

                        ttsObject.responseSuccess(f.getAbsoluteFile());
                    } else {
                        // 파일 생성 실패
                        ttsObject.responseFailed(context.getString(R.string.error_msg_content_common), "Can not create file.");
                    }
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();


                    ttsObject.responseFailed("API Server error", response.toString());
                }
            }
        } else {
            // 불가능한 상황
            String errorDetail = null;

            switch(ttsAvailable.getErrorCode()) {
                case 1:     // 계정 오류
                    errorDetail = context.getString(R.string.error_msg_auth_error);
                    break;
                case 2:     // 할당량 초과
                    errorDetail = context.getString(R.string.inode_exceeded);
                    break;
                default:
                    errorDetail = context.getString(R.string.error_msg_content_common);
                    break;
            }

            ttsObject.responseFailed(ttsAvailable.getErrorMessage(), errorDetail);
        }

        return ttsObject;
    }

    private String selectVocaloid(int idx) {
        String[] inputLanguages = context.getResources().getStringArray(R.array.supported_languages_v2_codes);

        switch(inputLanguages[idx]) {
            case "ko":
                return "mijin";
            case "en":
                return "clara";
            case "ja":
                return "yuri";
            case "zh-Hans": case "zh-Hant":
                return "meimei";
            default:
                return null;
        }
    }
}