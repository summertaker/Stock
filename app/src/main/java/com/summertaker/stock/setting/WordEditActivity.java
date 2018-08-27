package com.summertaker.stock.setting;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WordEditActivity extends BaseActivity {

    private View mMenuItemDeleteView;

    private long mId;
    private String mCategory;

    private EditText mEtValue;

    private Button mBtnSave;
    private LinearLayout mLoProcessing;
    private ImageView mIvProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_edit_activity);

        mContext = WordEditActivity.this;

        initBaseActivity(mContext);
        initGesture();

        Intent intent = getIntent();
        mActivityTitle = intent.getStringExtra(Config.KEY_ACTIVITY_TITLE);
        mId = intent.getLongExtra("id", 0);
        mCategory = intent.getStringExtra("category");
        String value = intent.getStringExtra("value");

        setActionBarTitle(mActivityTitle);

        if (mId == 0) {
            mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mMenuItemDeleteView = mToolbar.findViewById(R.id.action_clear);
                    if (mMenuItemDeleteView != null) {
                        mMenuItemDeleteView.setVisibility(View.GONE);
                    }
                }
            });
        }

        // 글자
        mEtValue = findViewById(R.id.etValue);
        mEtValue.setText(value);
        mEtValue.requestFocus();

        // 저장 버튼
        mBtnSave = findViewById(R.id.btnSave);
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // 처리 중...
        mLoProcessing = findViewById(R.id.loProcessing);
        mIvProcessing = findViewById(R.id.ivProcessing);

        // 키보드 보이기
        Util.showKeyboard(mContext, mEtValue);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear:
                deleteData();
                break;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        // 제외 글자
        String value = mEtValue.getText().toString();
        if (value.isEmpty()) {
            Toast.makeText(mContext, "글자를 입력하세요.", Toast.LENGTH_SHORT).show();
            mEtValue.requestFocus();
            return;
        }

        postData(Config.URL_WORD_SAVE, value);
    }

    private void postData(String url, final String value) {
        //Log.e(TAG, "mId: " + mId);
        //Log.e(TAG, "mCategory: " + mCategory);
        //Log.e(TAG, "value: " + value);

        mBtnSave.setVisibility(View.GONE);
        mLoProcessing.setVisibility(View.VISIBLE);
        mIvProcessing.startAnimation(BaseApplication.mRotateAnimation);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //mIvProcessing.clearAnimation();
                BaseApplication.getInstance().getWords().clear();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mDataManager.parseWords(jsonObject);
                    //Log.e(TAG, "size: " + BaseApplication.getInstance().getWords().size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                doFinish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mIvProcessing.clearAnimation();
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", String.valueOf(mId));
                params.put("category", mCategory);
                params.put("value", value);
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

    private void deleteData() {
        postData(Config.URL_WORD_DELETE, "");
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }

    private void doFinish() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        finish();
    }
}
