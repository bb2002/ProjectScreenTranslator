package kr.saintdev.pst.models.libs.async;

/**
 * Created by yuuki on 18. 4. 21.
 */

public interface OnBackgroundWorkListener {
    void onSuccess(int requestCode, BackgroundWork worker);
    void onFailed(int requestCode, Exception ex);
}
