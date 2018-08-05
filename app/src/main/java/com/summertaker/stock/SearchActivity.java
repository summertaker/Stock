package com.summertaker.stock;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.Util;

public class SearchActivity extends BaseActivity {

    private AutoCompleteTextView mTvAucoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        mContext = SearchActivity.this;

        initBaseActivity(mContext);
        initGesture();

        mTvAucoComplete = findViewById(R.id.tvAutoComplete);

        // 검색어 입력 폼 지우기
        ImageView ivClear = findViewById(R.id.ivClear);
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvAucoComplete.setText("");

                // 키보드 보이기
                //Util.showKeyboard(mContext, mTvAucoComplete);

                mTvAucoComplete.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }
            }
        });

        if (BaseApplication.getInstance().getBaseItems().size() == 0) {
            showBaseProgress(2);
            setBaseProgressBar(1);
            mDataManager.setOnBaseItemLoaded(new DataManager.BaseItemCallback() {
                @Override
                public void onParse(int count) {
                    setBaseProgressBar(count + 1);
                }

                @Override
                public void onLoad() {
                    hideBaseProgress();

                    mTvAucoComplete.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    }
                    init();
                }
            });
            mDataManager.loadBaseItem();
        } else {
            showBaseProgress(0);
            hideBaseProgress();

            // 키보드 보이기
            Util.showKeyboard(mContext, mTvAucoComplete);
            init();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_finish) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        String[] searchData = new String[BaseApplication.getInstance().getBaseItems().size()];
        for (int i = 0; i < BaseApplication.getInstance().getBaseItems().size(); i++) {
            searchData[i] = BaseApplication.getInstance().getBaseItems().get(i).getName();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.autocomplete, searchData);

        mTvAucoComplete.setThreshold(1); //will start working from first character
        mTvAucoComplete.setAdapter(adapter);
        mTvAucoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                String code = "";

                for (Item item : BaseApplication.getInstance().getBaseItems()) {
                    if (name.equals(item.getName())) {
                        code = item.getCode();
                        break;
                    }
                }

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("code", code);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }
}
