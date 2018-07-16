package kr.saintdev.pst.vnc.libs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import kr.saintdev.pst.R;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-18
 */

public class TileManager {
    private GridLayout layout = null;
    private View tileView = null;
    private Context context = null;

    public TileManager(Context context, GridLayout layout) {
        this.layout = layout;
        this.context = context;
    }

    public void addTile(Bitmap icon, String text, @Nullable View.OnClickListener listener, int id) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_tile, layout, false);

        ImageView iconView = view.findViewById(R.id.main_menu_tile_icon);
        TextView nameView = view.findViewById(R.id.main_menu_tile_name);

        iconView.setImageBitmap(icon);
        nameView.setText(text);

        if(listener != null) {
            view.setOnClickListener(listener);
            view.setTag(id);
        }

        layout.addView(view);
    }

    public void addTile(int iconRes, int stringRes, @Nullable View.OnClickListener listener, int id) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), iconRes);
        String text = context.getString(stringRes);
        addTile(icon, text, listener, id);
    }
}
