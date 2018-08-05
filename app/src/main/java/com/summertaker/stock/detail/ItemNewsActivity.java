package com.summertaker.stock.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.News;
import com.summertaker.stock.parser.DaumNewsParser;

public class ItemNewsActivity extends BaseActivity {

    Item mItem;
    News mNews;

    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_news_activity);

        mContext = ItemNewsActivity.this;

        initBaseActivity(mContext);
        initGesture();

        Intent intent = getIntent();

        mItem = new Item();
        //String code = intent.getStringExtra("code");

        mItem = BaseApplication.getInstance().getItem();

        mNews = new News();
        mNews.setTitle(intent.getStringExtra("title"));
        mNews.setElapsed(intent.getIntExtra("elapsed", 0));
        mNews.setPublishedText(intent.getStringExtra("publishedText"));
        mNews.setUrl(intent.getStringExtra("url"));

        // 툴바 제목
        String title = mItem.getName();
        if (mItem.getNor() > 0) {
            title = title + " (" + mItem.getNor() + ")";
        }
        setActionBarTitle(title);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToolbarClick();
            }
        });

        mScrollView = findViewById(R.id.scrollView);

        showBaseProgress(0);

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_news, menu);
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNews.getUrl()));
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

    private void loadData() {
        StringRequest strReq = new StringRequest(Request.Method.GET, mNews.getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseData("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseData(String response) {
        DaumNewsParser daumNewsParser = new DaumNewsParser();
        daumNewsParser.parseDetail(response, mNews);

        renderData();
    }

    private void renderData() {
        hideBaseProgress();

        // 현재가
        TextView tvPrice = findViewById(R.id.tvPrice);
        BaseApplication.getInstance().renderPrice(mItem, tvPrice, null);

        // 등락률
        TextView tvFlucIcon = findViewById(R.id.tvFlucIcon);
        TextView tvRof = findViewById(R.id.tvRof);
        BaseApplication.getInstance().renderRof(mItem, tvFlucIcon, null, tvRof, null);

        // 전일비
        TextView tvPof = findViewById(R.id.tvPof);
        BaseApplication.getInstance().renderPof(mItem, tvPof, "(%s원)", null, "");

        // 뉴스 제목
        TextView tvTitle = findViewById(R.id.tvTitle);
        String title = mNews.getTitle();
        title = title.replace(mItem.getName(), String.format(Config.NEWS_ITEM_NAME_HIGHLIGHT_FORMAT, mItem.getName()));
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

        TextView tvElapsed = findViewById(R.id.tvElapsed);
        tvElapsed.setText(elapsed);

        // 날짜
        TextView tvPublished = findViewById(R.id.tvPublished);
        tvPublished.setText(publishedText);

        // 내용
        TextView tvContent = findViewById(R.id.tvContent);
        String content = mNews.getContent();
        //content = content.replace(mItem.getName(), String.format(Config.NEWS_ITEM_NAME_HYPERLINK_FORMAT, mItem.getCode(), mItem.getName()));
        //tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        setTextViewHTML(tvContent, content);
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
