package com.summertaker.stock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Word;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class WordListActivity extends BaseActivity {

    private View mMenuItemRefreshView;

    private String mCategory;

    private ArrayList<Word> mWords = new ArrayList<>();
    private WordListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list_activity);

        mContext = WordListActivity.this;

        Intent intent = getIntent();
        mCategory = intent.getStringExtra(Config.KEY_WORD_CATEGORY);
        mActivityTitle = intent.getStringExtra(Config.KEY_ACTIVITY_TITLE);

        initBaseActivity(mContext);
        initGesture();

        setActionBarTitle(mActivityTitle);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarClick();
            }
        });

        mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMenuItemRefreshView = mToolbar.findViewById(R.id.action_refresh);
                if (mMenuItemRefreshView != null) {
                    mMenuItemRefreshView.setVisibility(View.GONE);
                }
            }
        });

        mAdapter = new WordListAdapter(mContext, mWords);
        mAdapter.setHasStableIds(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Word data = mWords.get(position);
                //Toast.makeText(mContext, tag.getId() + "", Toast.LENGTH_SHORT).show();

                Intent edit = new Intent(mContext, WordEditActivity.class);
                edit.putExtra(Config.KEY_ACTIVITY_TITLE, mActivityTitle);
                edit.putExtra("id", data.getId());
                edit.putExtra("category", data.getCategory());
                edit.putExtra("value", data.getValue());
                startActivityForResult(edit, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Tag tag = mWords.get(position);
            }
        }));

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                Intent edit = new Intent(this, WordEditActivity.class);
                edit.putExtra(Config.KEY_ACTIVITY_TITLE, mActivityTitle);
                edit.putExtra("category", mCategory);
                startActivityForResult(edit, Config.ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        mWords.clear();

        int no = 1;
        for (Word be : BaseApplication.getInstance().getWords()) {
            if (!be.getCategory().equals(mCategory)) {
                continue;
            }
            Word word = new Word();
            word.setId(be.getId());
            word.setNo(no);
            word.setCategory(be.getCategory());
            word.setValue(be.getValue());

            mWords.add(word);
            no++;
        }

        renderData();
    }

    private void renderData() {
        mAdapter.notifyDataSetChanged();
        //setActionBarTitleCount(mWords.size());
    }

    private void refresh() {
        //mWords = BaseApplication.getInstance().getWords();
        //Log.e(TAG, "size: " + mWords.size());
        loadData();
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        manager.scrollToPositionWithOffset(0, 0);
    }

    public void onToolbarClick() {
        goToTheTop();
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            refresh();
        }
    }
}
