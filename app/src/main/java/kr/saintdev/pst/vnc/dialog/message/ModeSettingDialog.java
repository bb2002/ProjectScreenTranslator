package kr.saintdev.pst.vnc.dialog.message;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RadioGroup;
import android.widget.Toast;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.components.broadcast.ProcedureEngine;
import kr.saintdev.pst.models.libs.manager.RepositoryKey;
import kr.saintdev.pst.models.libs.manager.RepositoryManager;

import static kr.saintdev.pst.models.libs.LibKt.checkSystemOverlayPermisson;
import static kr.saintdev.pst.vnc.activity.view.SystemOverlayActivityKt.startSystemOverlayGrantActivity;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-07-04
 */

public class ModeSettingDialog extends Dialog {
    private RepositoryManager repoManager = null;
    private RadioGroup selectGroup = null;

    public ModeSettingDialog(@NonNull Context context) {
        super(context);
        this.repoManager = RepositoryManager.Companion.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mode_setting);

        this.selectGroup = this.findViewById(R.id.mode_select_container);
        this.repoManager = RepositoryManager.Companion.getInstance(getContext());

        String nowSetting = repoManager.getHashValue(RepositoryKey.MODE_SETTING);
        if(nowSetting.equals("none") || !nowSetting.equals("gaming")) {
            this.selectGroup.check(R.id.mode_setting_default);
        } else {
            this.selectGroup.check(R.id.mode_setting_gaming);
        }

        this.selectGroup.setOnCheckedChangeListener(new OnRadioChangeListener());
    }

    class OnRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch(checkedId) {
                case R.id.mode_setting_default:
                    repoManager.createHashValue(RepositoryKey.MODE_SETTING, "default");
                    break;
                case R.id.mode_setting_gaming:
                    if(checkSystemOverlayPermisson(getContext())) {
                        repoManager.createHashValue(RepositoryKey.MODE_SETTING, "gaming");
                    } else {
                        // Overlay 가 꺼져있다면 선택 할 수 없습니다.
                        startSystemOverlayGrantActivity(getContext());
                        group.check(R.id.mode_setting_gaming);          // 체크값을 변경한다.
                    }
                    break;
            }
        }
    }
}
