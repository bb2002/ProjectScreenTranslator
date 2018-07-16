package kr.saintdev.pst.vnc.dialog.message;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import kr.saintdev.pst.R;

public class LanguageSelectDialog extends Dialog {
    ListView view = null;
    int idx = -1;
    Object tag = null;
    private boolean blockAutoDetect = false;

    public LanguageSelectDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_language_select);

        Context c = getContext();
        setTitle(R.string.entity_language_select);

        this.view = findViewById(R.id.listview_language_item);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(c, R.array.supported_languages_v2_names, android.R.layout.simple_list_item_1);
        this.view.setAdapter(adapter);

        OnItemClickHandler handler = new OnItemClickHandler();
        this.view.setOnItemClickListener(handler);
    }

    class OnItemClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(blockAutoDetect && i == 0) {
                Toast.makeText(getContext(), getContext().getString(R.string.error_msg_translate_v2_auto_detect_blocked), Toast.LENGTH_SHORT).show();
                return;
            }

            idx = i;
            dismiss();
        }
    }

    public int getLanguageIdx() {
        return idx;
    }

    public void setLanguageIdx(int idx) {
        this.idx = idx;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return this.tag;
    }

    public void setBlockAutoDetect(boolean b) {
        this.blockAutoDetect = b;
    }
}
