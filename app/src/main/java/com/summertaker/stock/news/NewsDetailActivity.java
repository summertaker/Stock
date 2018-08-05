package com.summertaker.stock.news;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.WebActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.News;

public class NewsDetailActivity extends BaseActivity {

    private News mNews;

    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail_activity);

        mContext = NewsDetailActivity.this;

        initBaseActivity(mContext);
        initGesture();

        Intent intent = getIntent();

        mNews = new News();
        mNews.setTitle(intent.getStringExtra("title"));
        mNews.setElapsed(intent.getIntExtra("elapsed", 0));
        mNews.setPublishedText(intent.getStringExtra("publishedText"));
        mNews.setUrl(intent.getStringExtra("url"));

        // 툴바 제목
        //setActionBarTitle(mNews.getTitle());
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToolbarClick();
            }
        });

        mScrollView = findViewById(R.id.scrollView);

        /*
        TextView tvContent = findViewById(R.id.tvContent);
        tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mNews.getUrl();
                url = url.replace("https://finance.naver.com", "https://m.stock.naver.com");

                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("title", mNews.getTitle());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
        */

        showBaseProgress(0);

        mDataManager.setOnNewsDetailLoaded(new DataManager.NewsDetailCallback() {
            @Override
            public void onLoad(News news) {
                mNews.setContent(news.getContent());
                renderData();
            }
        });
        mDataManager.loadNewsDetail(mNews.getUrl());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent search = new Intent(this, SearchActivity.class);
                startActivity(search);
                return true;
            case R.id.action_open_in_new:
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNews.getUrl()));
                Intent intent = new Intent(mContext, WebActivity.class);

                String url = mNews.getUrl();
                if (!url.contains("market_special.nhn")) { // 특징주는 모바일 링크가  없다.
                    url = url.replace("finance.naver.com", "m.stock.naver.com");
                    url = url.replace("news_read.nhn", "read.nhn");
                    url = url.replace("article_id", "articleId");
                    url = url.replace("office_id", "officeId");

                    if (url.contains("&mode=mainnews")) { // 주요 뉴스
                        // https://finance.naver.com/news/news_read.nhn?article_id=0004077754&office_id=008&mode=mainnews
                        // https://m.stock.naver.com/news/read.nhn?category=mainnews&officeId=008&articleId=0004077754
                        url = url.replace("&mode=mainnews", "&category=mainnews");
                    } else if (url.contains("&mode=RANK")) { // 많이 본 뉴스
                        // https://finance.naver.com/news/news_read.nhn?article_id=0003978587&office_id=015&mode=RANK&typ=0
                        // https://m.stock.naver.com/news/read.nhn?category=ranknews&officeId=015&articleId=0003978587
                        url = url.replace("&mode=RANK", "&category=ranknews");
                    } else if (url.contains("&mode=LSS2D")) { // 속보
                        // https://finance.naver.com/news/news_read.nhn?article_id=0003978605&office_id=015&mode=LSS2D&type=0&section_id=101&section_id2=258&section_id3=&date=20180713&page=1
                        // https://m.stock.naver.com/news/read.nhn?category=flashnews&officeId=015&articleId=0003978605
                        url = url.replace("&mode=LSS2D", "&category=flashnews");
                    }
                }

                //Log.e(TAG, url);

                intent.putExtra("url", url);
                startActivity(intent);
                return true;
            //case R.id.action_settings:
            //    Intent wordCategory = new Intent(this, WordCategoryActivity.class);
            //    startActivity(wordCategory);
            //    return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderData() {
        hideBaseProgress();

        // 뉴스 제목
        TextView tvTitle = findViewById(R.id.tvTitle);
        String title = mNews.getTitle();
        tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);

        // 경과일, 발행일 설정
        String elapsed;
        String publishedText = mNews.getPublishedText(); // 0000-00-00 00:00:00
        if (publishedText == null || publishedText.isEmpty()) {
            elapsed = "";
            publishedText = "";
        } else {
            publishedText = publishedText.substring(5, 16); // 년도, 초 잘라내기
            publishedText = publishedText.replace("-", ".");

            // 경과일
            if (mNews.getElapsed() == 0) {
                elapsed = mResources.getString(R.string.today);
                publishedText = publishedText.substring(6, 11); // 시간만 표시
            } else if (mNews.getElapsed() == 1) {
                elapsed = mResources.getString(R.string.yesterday);
            } else {
                elapsed = String.format(mResources.getString(R.string.s_days_ago), String.valueOf(mNews.getElapsed()));
            }
        }

        // 경과일
        TextView tvElapsed = findViewById(R.id.tvElapsed);
        tvElapsed.setText(elapsed);

        // 날짜
        TextView tvPublished = findViewById(R.id.tvPublished);
        tvPublished.setText(publishedText);

        // 내용
        TextView tvContent = findViewById(R.id.tvContent);
        String content = mNews.getContent();
        setTextViewHTML(tvContent, content);
        //tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        tvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                finish();
                return false;
            }
        });
    }

    protected void onToolbarClick() {
        mScrollView.scrollTo(0, 0);
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }
}
