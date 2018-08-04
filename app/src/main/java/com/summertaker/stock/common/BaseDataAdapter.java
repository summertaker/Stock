package com.summertaker.stock.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Tag;

public class BaseDataAdapter extends BaseAdapter {

    protected String TAG;

    public BaseDataAdapter() {
        this.TAG = this.getClass().getSimpleName();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}

