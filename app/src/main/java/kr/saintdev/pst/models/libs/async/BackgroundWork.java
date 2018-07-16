package kr.saintdev.pst.models.libs.async;

import android.os.AsyncTask;

/**
 * Created by yuuki on 18. 4. 21.
 */

public abstract class BackgroundWork<T> extends AsyncTask<Void, Void, T> {
    private int reqeustCode = 0;
    private OnBackgroundWorkListener listener = null;
    private T resultObj = null;

    private boolean isErrorOccurred = false;
    private Exception errorMessage = null;

    public BackgroundWork(int requestCode, OnBackgroundWorkListener listener) {
        this.reqeustCode = requestCode;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(T t) {
        super.onPostExecute(t);

        setResult(t);
        if(this.listener != null) {
            if (isErrorOccurred) {
                this.listener.onFailed(this.reqeustCode, this.errorMessage);
            } else {
                this.listener.onSuccess(this.reqeustCode, this);
            }
        }
    }

    @Override
    protected T doInBackground(Void... voids) {
        try {
            return script();
        } catch (Exception ex) {
            this.errorMessage = ex;
            this.isErrorOccurred = true;
            return null;
        }
    }

    /*
        이 메서드를 Override 하여 실행할 스크립트를 입력합니다.
     */
    protected abstract T script() throws Exception;

    /*
        이 메서드를 통해 결과값을 가져옵니다.
     */

    /*
        ResultObject 가 Null 이면 true 를 반환합니다.
     */
    public boolean isResultNull() {
        if(resultObj == null) {
            return true;
        } else {
            return false;
        }
    }

    public void setResult(T result) {
        this.resultObj = result;
    }

    public Object getResult() {
        return this.resultObj;
    }
}
