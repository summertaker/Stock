package com.summertaker.stock.setting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.data.Word;

import java.util.ArrayList;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ItemViewHolder> {

    //private Context mContext;
    private ArrayList<Word> mDataList;

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNo;
        public TextView tvWord;

        public ItemViewHolder(View view) {
            super(view);

            tvNo = view.findViewById(R.id.tvNo);
            tvWord = view.findViewById(R.id.tvWord);
        }
    }

    public WordListAdapter(Context context, ArrayList<Word> dataList) {
        //this.mContext = context;
        this.mDataList = dataList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_list_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Word data = mDataList.get(position);

        String no = String.valueOf(data.getNo()) + ".";
        holder.tvNo.setText(no);

        String word = data.getValue();
        holder.tvWord.setText(word);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getId();
    }
}
