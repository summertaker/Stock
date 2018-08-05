package com.summertaker.stock.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.util.Util;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends BaseActivity {

    EditText mEtBuyPricePerItem;
    EditText mEtLowestRof;
    EditText mEtHighestRof;

    private LinearLayout mLoButton;
    private LinearLayout mLoProcessing;
    private ImageView mIvProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        mContext = SettingActivity.this;

        initBaseActivity(mContext);
        initGesture();

        // 종목당 매수 금액
        mEtBuyPricePerItem = findViewById(R.id.etBuyPricePerItem);
        Util.addNumberFormat(mEtBuyPricePerItem);
        mEtBuyPricePerItem.setText(BaseApplication.getInstance().getStringSetting(Config.SETTING_BUY_PRICE_PER_ITEM));

        // 최저등락률
        mEtLowestRof = findViewById(R.id.etLowestRof);
        mEtLowestRof.setText(BaseApplication.getInstance().getStringSetting(Config.SETTING_LOWEST_ROF));

        // 최고등락률
        mEtHighestRof = findViewById(R.id.etHighestRof);
        mEtHighestRof.setText(BaseApplication.getInstance().getStringSetting(Config.SETTING_HIGHEST_ROF));

        /*
        // 아이템 목록에서 아이템을 길게 눌렀을 때
        String longClick = mDataManager.load(Config.SETTING_ON_ITEM_LONG_CLICK);

        // 관심 종목에 추가하기
        mRbAddToFavorites = findViewById(R.id.rbAddToFavorites);
        if (longClick == null || longClick.isEmpty() || longClick.equals(Config.SETTING_ADD_TO_FAVORITES)) {
            mRbAddToFavorites.setChecked(true);
        }

        // 카카오 스탁 실행하기
        mRbStartKakaoStock = findViewById(R.id.rbStartKakaoStock);
        if (longClick != null && longClick.equals(Config.SETTING_START_KAKAO_STOCK)) {
            mRbStartKakaoStock.setChecked(true);
        }
        */

        // 저장하기 버튼
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        mLoButton = findViewById(R.id.loButton);
        mLoProcessing = findViewById(R.id.loProcessing);
        mIvProcessing = findViewById(R.id.ivProcessing);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //case R.id.action_save:
            //    save();
            //    break;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        String buyPricePerItem = mEtBuyPricePerItem.getText().toString(); // 종목당 매수 금액
        buyPricePerItem = buyPricePerItem.replaceAll(",", "");
        BaseApplication.getInstance().setSetting(Config.SETTING_BUY_PRICE_PER_ITEM, buyPricePerItem);

        String lowestRof = mEtLowestRof.getText().toString(); // 최저등락률
        lowestRof = lowestRof.replaceAll(",", "");
        BaseApplication.getInstance().setSetting(Config.SETTING_LOWEST_ROF, lowestRof);

        String highestRof = mEtHighestRof.getText().toString(); // 최고등락률
        highestRof = highestRof.replaceAll(",", "");
        BaseApplication.getInstance().setSetting(Config.SETTING_HIGHEST_ROF, highestRof);

        mDataManager.writeSettings();
        finish();

        /*
        // 종목을 길게 눌렀을 때
        String onItemLongClick = "";
        if (mRbAddToFavorites.isChecked()) {
            onItemLongClick = Config.SETTING_ADD_TO_FAVORITES;
        } else if (mRbStartKakaoStock.isChecked()) {
            onItemLongClick = Config.SETTING_START_KAKAO_STOCK;
        }
        mDataManager.save(Config.SETTING_ON_ITEM_LONG_CLICK, onItemLongClick);
        */

        /*
        // 서버에 저장할 데이터 만들기
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Config.SETTING_BUY_PRICE_PER_ITEM, buyPricePerItem);
            jsonObject.put(Config.SETTING_LOWEST_ROF, lowestRof);
            jsonObject.put(Config.SETTING_HIGHEST_ROF, highestRof);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = jsonObject.toString();
        //Log.e(TAG, "data: " + data);

        doPost(Config.URL_SETTING_SAVE, data);
        */
    }

    private void doPost(String url, final String data) {
        mLoButton.setVisibility(View.GONE);
        mLoProcessing.setVisibility(View.VISIBLE);
        //mIvProcessing.startAnimation(mRotateAnimation);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.isEmpty()) {
                    finish();
                } else {
                    Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "ERROR: " + response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data", data);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }
}
