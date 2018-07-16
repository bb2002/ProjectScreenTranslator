package kr.saintdev.pst.models.http.modules.ocr.objects;

import android.graphics.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 5252b on 2018-04-16.
 */

public class OCRLine {
    private String sentence = null;
    private Point leftTop = null;
    private Point size = null;

    private ArrayList<OCRWord> words = new ArrayList<>();

    public OCRLine(JSONObject lineObj, boolean useSpacingWord) throws JSONException {
        // 이 문장의 너비를 구합니다.
        String boundingBox = lineObj.getString("boundingBox");
        String[] boundingPosition = boundingBox.split(",");

        this.leftTop = new Point(Integer.parseInt(boundingPosition[0]), Integer.parseInt(boundingPosition[1]) + Integer.parseInt(boundingPosition[3]));
        this.size = new Point(Integer.parseInt(boundingPosition[2]), Integer.parseInt(boundingPosition[3]));

        StringBuilder sentenceBuilder = new StringBuilder();
        JSONArray words = lineObj.getJSONArray("words");
        for(int i = 0; i < words.length(); i ++) {
            JSONObject word = words.getJSONObject(i);
            OCRWord ocrWord = new OCRWord(word);

            this.words.add(ocrWord);
            sentenceBuilder.append(ocrWord.getWord());
            if(useSpacingWord && (i+1 != words.length())) {
                sentenceBuilder.append(" ");
            }
        }

        this.sentence = sentenceBuilder.toString();
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public Point getLeftTop() {
        return leftTop;
    }

    public Point getSize() {
        return size;
    }

    public ArrayList<OCRWord> getWords() {
        return words;
    }
}
