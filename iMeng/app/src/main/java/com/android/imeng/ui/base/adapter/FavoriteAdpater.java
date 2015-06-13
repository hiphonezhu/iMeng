package com.android.imeng.ui.base.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;

import com.android.imeng.R;
import com.android.imeng.framework.ui.BasicAdapter;
import com.android.imeng.ui.base.OptListener;
import com.android.imeng.ui.gallery.GalleryListener;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.List;

/**
 * 收藏配器
 * @author hiphonezhu@gmail.com
 * @version [iMeng, 2015-06-07 21:20]
 */
public class FavoriteAdpater extends BasicAdapter<String> {
    private int width; // 宽
    private int height; // 高
    private Mode mode = Mode.NORMAL;
    OptListener optListener;
    public FavoriteAdpater(Context context, List<String> data, int resourceId, OptListener optListener) {
        super(context, data, resourceId);
        this.optListener = optListener;
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
        notifyDataSetChanged();
    }

    @Override
    protected void getView(final int position, View convertView) {
        // 调整宽高
        if (convertView.getTag() == null) // convertView刚创建
        {
            convertView.setLayoutParams(new AbsListView.LayoutParams(width, height));
        }
        SimpleDraweeView coverView = findViewById(convertView, R.id.cover_view);
        final Button deleteBtn = findViewById(convertView, R.id.delete_btn);

        final String localPath = getItem(position);
        coverView.setImageURI(Uri.fromFile(new File(localPath)));
        if (mode == Mode.NORMAL)
        {
            deleteBtn.setVisibility(View.GONE);
        }
        else if (mode == Mode.DELETE)
        {
            deleteBtn.setVisibility(View.VISIBLE);
        }
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optListener.onOpt(deleteBtn, localPath);
            }
        });
    }

    public enum Mode
    {
        NORMAL, // 正常模式
        DELETE, // 删除模式
    }
}
