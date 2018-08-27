package com.summertaker.stock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.Config;

public class WordCategoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_category_activity);

        mContext = WordCategoryActivity.this;

        initBaseActivity(mContext);
        initGesture();

        // 속보 강조 글자
        TextView tvBreakingInclude = findViewById(R.id.tvBreakingInclude);
        tvBreakingInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_BREAKING_INCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_alert_include));
                startActivity(intent);
            }
        });

        // 속보 제외 글자
        TextView tvBreakingExclude = findViewById(R.id.tvBreakingExclude);
        tvBreakingExclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_BREAKING_EXCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_alert_exclude));
                startActivity(intent);
            }
        });

        // 네이버 강조 글자
        TextView tvNaverInclude = findViewById(R.id.tvNaverInclude);
        tvNaverInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_NAVER_INCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_naver_include));
                startActivity(intent);
            }
        });

        // 네이버 제외 글자
        TextView tvNaverExclude = findViewById(R.id.tvNaverExclude);
        tvNaverExclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_naver_exclude));
                startActivity(intent);
            }
        });

        // 네이버 광고 글자
        TextView tvNaverAd = findViewById(R.id.tvNaverAd);
        tvNaverAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_NAVER_AD);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_naver_exclude));
                startActivity(intent);
            }
        });

        // 다음 포함 글자
        TextView tvDaumInclude = findViewById(R.id.tvDaumInclude);
        tvDaumInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_DAUM_INCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_daum_include));
                startActivity(intent);
            }
        });

        // 다음 제외 글자
        TextView tvDaumExclude = findViewById(R.id.tvDaumExclude);
        tvDaumExclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_DAUM_EXCLUDE);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_daum_exclude));
                startActivity(intent);
            }
        });

        // 다음 광고 글자
        TextView tvDaumAd = findViewById(R.id.tvDaumAd);
        tvDaumAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordListActivity.class);
                intent.putExtra(Config.KEY_WORD_CATEGORY, Config.KEY_WORD_CATEGORY_DAUM_AD);
                intent.putExtra(Config.KEY_ACTIVITY_TITLE, getString(R.string.word_category_daum_ad));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent search = new Intent(mContext, SearchActivity.class);
                startActivity(search);
                return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }
}
