package com.summertaker.stock;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.fluctuation.FluctuationActivity;
import com.summertaker.stock.news.NewsListActivity;
import com.summertaker.stock.portfolio.PortfolioActivity;
import com.summertaker.stock.recommend.RecommendActivity;
import com.summertaker.stock.setting.SettingActivity;
import com.summertaker.stock.setting.TagListActivity;
import com.summertaker.stock.setting.WordCategoryActivity;
import com.summertaker.stock.trade.TradeActivity;
import com.summertaker.stock.trader.TraderActivity;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private int mProgress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = MainActivity.this;
        initBaseActivity(mContext);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showBaseProgress(0);
        loadSetting();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        /*
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadSetting() {
        //mDataManager.readSettings();
        //mDataManager.readTags();
        //mDataManager.readPortfolios();

        mDataManager.setOnSettingLoaded(new DataManager.SettingCallback() {
            @Override
            public void onLoad() {
                loadRecommendTop();
            }
        });
        mDataManager.loadSetting();
    }

    private void loadBaseItems() {
        setBaseProgressBar(mProgress++);
        mDataManager.setOnBaseItemLoaded(new DataManager.BaseItemsCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(mProgress++);
            }

            @Override
            public void onLoad() {
                loadRecommendTop();
            }
        });
        mDataManager.loadBaseItems();
    }

    private void loadRecommendTop() {
        //setBaseProgressBar(mProgress++);
        mDataManager.setOnRecommendTopItemLoaded(new DataManager.RecommendTopItemCallback() {
            @Override
            public void onLoad() {
                init();
            }
        });
        mDataManager.loadRecommendTopItem(this);
    }

    private void init() {
        hideBaseProgress();

        /*
        // 속보
        LinearLayout loBreaking = findViewById(R.id.loBreaking);
        loBreaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, BreakingListActivity.class);
                startActivity(intent);
            }
        });
        */

        /*
        // 뉴스
        LinearLayout loNews = findViewById(R.id.loNews);
        loNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NewsListActivity.class);
                startActivity(intent);
            }
        });
        */

        /*
        // 포트폴리오
        LinearLayout loPortfolio = findViewById(R.id.loPortfolio);
        loPortfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PortfolioActivity.class);
                startActivity(intent);
            }
        });
        */

        /*
        // 상승
        LinearLayout loRise = findViewById(R.id.loRise);
        loRise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RiseActivity.class);
                startActivity(intent);
            }
        });
        */

        // 등락
        LinearLayout loFluctuation = findViewById(R.id.loFluctuation);
        loFluctuation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FluctuationActivity.class);
                startActivity(intent);
            }
        });

        // 매매
        LinearLayout loTrade = findViewById(R.id.loTrade);
        loTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TradeActivity.class);
                startActivity(intent);
            }
        });

        /*
        // 거래원
        LinearLayout loTrader = findViewById(R.id.loTrader);
        loTrader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TraderActivity.class);
                startActivity(intent);
            }
        });
        */

        // 추천
        LinearLayout loRecommend = findViewById(R.id.loReco);
        loRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RecommendActivity.class);
                startActivity(intent);
            }
        });

        /*
        // 태그
        TextView tvTag = findViewById(R.id.tvTag);
        tvTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TagListActivity.class);
                startActivity(intent);
            }
        });

        // 단어
        TextView tvWord = findViewById(R.id.tvWord);
        tvWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WordCategoryActivity.class);
                startActivity(intent);
            }
        });

        // 설정
        TextView tvSetting = findViewById(R.id.tvSetting);
        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SettingActivity.class);
                startActivity(intent);
            }
        });
        */
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }
}
