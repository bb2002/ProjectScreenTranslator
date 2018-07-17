package kr.saintdev.pst.models.http.modules.downloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.saintdev.pst.models.libs.async.BackgroundWork;
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener;


/**
 * Created by yuuki on 18. 4. 22.
 */

public class ImageDownloader extends BackgroundWork<Bitmap> {
    String res;

    public ImageDownloader(String res, int requestCode, OnBackgroundWorkListener listener) {
        super(requestCode, listener);
        this.res = res;
    }

    @Override
    protected Bitmap script() throws Exception {
        URL url = new URL(res);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);  // 서버 응답 수신
        conn.connect();

        InputStream is = conn.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(is);

        return bitmap;
    }
}
