package kr.saintdev.pst.vnc.activity.control;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kr.saintdev.pst.R;
import kr.saintdev.pst.vnc.dialog.message.DialogManager;
import kr.saintdev.pst.vnc.dialog.message.OnDialogButtonClickListener;
import kr.saintdev.pst.vnc.dialog.progress.ProgressManager;


public abstract class SuperControl implements OnDialogButtonClickListener {
    private Activity control = null;

    // Window 관련
    private DialogManager dm;
    private ProgressManager pm;

    public SuperControl(Activity control) {
        this.control = control;
        this.dm = new DialogManager(control);
        this.pm = new ProgressManager(control);
    }

    /**
     * 새 Dialog 를 띄웁니다.
     * @param titleRes      String 리소스 중, 제목
     * @param contentRes    String 리소스 중, 내용
     * @param showNegative  부정 버튼을 사용할것인가?
     * @param showPositive  긍정 버튼을 사용할것인가?
     * @param requestId     요청 ID
     */
    protected void showDialogWindow(int titleRes, int contentRes, boolean showPositive, boolean showNegative, int requestId) {
        showDialogWindow(titleRes, contentRes, showPositive, showNegative, 1, requestId);
    }

    protected void showDialogWindow(int titleRes, int contentRes, boolean showPositive, boolean showNegative, int type, int requestId) {
        showDialogWindow(control.getString(titleRes), control.getString(contentRes), showPositive, showNegative, type, requestId);
    }

    protected void showDialogWindow(String title, String content, boolean showPositive, boolean showNegative, int type, int requestId) {
        this.dm.setTitle(title);
        this.dm.setDescription(content);
        this.dm.setRequestId(requestId);
        this.dm.setDialogButtonClickListener(this);

        int[][] dialogType = {
                { R.string.common_positive_allow, R.string.common_negative_ignore },
                { R.string.common_positive_ok, R.string.common_negative_no }
        };

        if(showPositive) this.dm.setOnYesButtonClickListener(control.getString(dialogType[type][0]));
        if(showNegative) this.dm.setOnNoButtonClickListener(control.getString(dialogType[type][1]));
        this.dm.show();
    }

    /**
     * 새 Progress 를 띄웁니다.
     */
    protected void showProgressWindow() {
        showProgressWindow(R.string.common_loading);
    }

    protected void showProgressWindow(int titleRes) {
        this.pm.setMessage(control.getString(titleRes));
        this.pm.enable();
    }

    protected void closeProgressWindow() {
        this.pm.disable();
    }

    /**
     * 대화창 내 긍정, 부정 버튼을 클릭 했을 떄 리스너.
     * 기본적으로 Dialog 를 닫습니다.
     * 필요에 따라 상속자는 이를 재정의 해서 사용합니다.
     */
    @Override
    public void onPositiveClick(DialogInterface dialog, int reqId) {
        dialog.dismiss();
    }

    @Override
    public void onNegativeClick(DialogInterface dialog, int reqId) {
        dialog.dismiss();
    }

    /**
     * View 를 가져옵니다
     */
    public View getCorrectView() {
        return null;
    }

    /**
     * LayoutInflater 를 가져옵니다.
     */
    protected LayoutInflater getInflater() {
        LayoutInflater inflater = (LayoutInflater) control.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater;
    }

    public abstract void onCreate(@Nullable ViewGroup container);
    public abstract void onResume();
    public abstract void onStop();
}
