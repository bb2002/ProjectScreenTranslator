package kr.saintdev.pst.models.http.modules.ocr.objects;

import android.graphics.Point;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 5252b on 2018-04-16.
 */

public class OCRWord {
    private String word = null;     // 단어 데이터
    private Point leftTop = null;     // 위치 값
    private Point size = null;      // 영역 크기

    public OCRWord(JSONObject obj) throws JSONException {
        // 이 단어의 너비를 구합니다.
        String boundingBox = obj.getString("boundingBox");
        String[] boundingPosition = boundingBox.split(",");

        this.leftTop = new Point(Integer.parseInt(boundingPosition[0]), Integer.parseInt(boundingPosition[1]) + Integer.parseInt(boundingPosition[3]));
        this.size = new Point(Integer.parseInt(boundingPosition[2]), Integer.parseInt(boundingPosition[3]));
        this.word = obj.getString("text");
    }

    public String getWord() {
        return word;
    }

    public Point getLeftTop() {
        return leftTop;
    }

    public Point getSize() {
        return size;
    }
}
