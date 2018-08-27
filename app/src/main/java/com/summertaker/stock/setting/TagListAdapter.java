package com.summertaker.stock.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.data.Tag;
import com.summertaker.stock.helper.ItemTouchHelperAdapter;
import com.summertaker.stock.helper.ItemTouchHelperViewHolder;
import com.summertaker.stock.helper.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private Context mContext;
    private ArrayList<Tag> mItems;

    private final OnStartDragListener mDragStartListener;

    TagListAdapter(Context context, ArrayList<Tag> tags, OnStartDragListener dragStartListener) {
        mContext = context;
        mItems = tags;
        mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_row, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        Tag tag = mItems.get(position);

        final long id = tag.getId();

        // 이름
        String name = tag.getName();
        holder.tvName.setText(name);

        /*
        // 배경색
        if (tag.getBgc() != null && tag.getBgc().length() == 7) {
            // https://stackoverflow.com/questions/18391830/how-to-programmatically-round-corners-and-set-random-background-colors
            GradientDrawable drawable = (GradientDrawable) holder.tvName.getBackground();
            drawable.setColor(Color.parseColor(tag.getBgc()));
        }

        // 글자색
        if (tag.getFgc() != null && tag.getFgc().length() == 7) {
            holder.tvName.setTextColor(Color.parseColor(tag.getFgc()));
        }
        */

        // Start a drag whenever the handle view it touched
        holder.ivHandle.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder, id);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        mDragStartListener.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView tvName;
        public final ImageView ivHandle;

        ItemViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivHandle = itemView.findViewById(R.id.ivHandle);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.parseColor("#e6e6e6"));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
