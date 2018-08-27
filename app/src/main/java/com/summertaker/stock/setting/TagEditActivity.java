package com.summertaker.stock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.summertaker.stock.data.Tag;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagEditActivity extends BaseActivity {

    private View mMenuItemDeleteView;

    private long mId;

    private EditText mEtName;
    //private EditText mEtFgc;
    //private EditText mEtBgc;

    private Button mBtnSave;
    private LinearLayout mLoProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_edit_activity);

        mContext = TagEditActivity.this;

        initBaseActivity(mContext);
        initGesture();

        Intent intent = getIntent();
        mId = intent.getLongExtra("id", 0);
        String name = intent.getStringExtra("name");
        //String bgc = intent.getStringExtra("bgc");
        //String fgc = intent.getStringExtra("fgc");

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

        /*
        if (fgc != null) {
            fgc = fgc.replace("#", "");
        }

        if (bgc != null) {
            bgc = bgc.replace("#", "");
        }
        */

        // 이름
        mEtName = findViewById(R.id.etName);
        mEtName.setText(name);

        /*
        // 글자색
        mEtFgc = findViewById(R.id.etFgc);
        mEtFgc.setText(fgc);

        // 배경색
        mEtBgc = findViewById(R.id.etBgc);
        mEtBgc.setText(bgc);
        */

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

        //Util.showKeyboard(mContext, mEtName);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tag_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear:
                removeData();
                break;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        // 이름
        String name = mEtName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(mContext, "이름", Toast.LENGTH_SHORT).show();
            mEtName.requestFocus();
            return;
        }

        /*
        // 배경색
        String bgc = "#" + mEtBgc.getText().toString();
        try {
            // 색상 코드 검증
            int color = Color.parseColor(bgc);
        } catch (IllegalArgumentException iae) {
            Toast.makeText(mContext, "배경색", Toast.LENGTH_LONG).show();
            mEtBgc.requestFocus();
            return;
        }

        // 글자색
        String fgc = "#" + mEtFgc.getText().toString();
        try {
            // 색상 코드 검증
            int color = Color.parseColor(fgc);
        } catch (IllegalArgumentException iae) {
            Toast.makeText(mContext, "글자색", Toast.LENGTH_LONG).show();
            mEtFgc.requestFocus();
            return;
        }
        */

        /*
        if (mId > 0) {
            for (Tag bt : BaseApplication.getInstance().getTags()) {
                if (bt.getId() == mId) {
                    bt.setName(name);
                    //bt.setBgc(bgc);
                    //bt.setFgc(fgc);
                    break;
                }
            }
        } else {
            long id = BaseApplication.getInstance().getTags().size() + 1;
            Tag tag = new Tag();
            tag.setId(id);
            tag.setName(name);
            //tag.setBgc(bgc);
            //tag.setFgc(fgc);
            BaseApplication.getInstance().getTags().add(tag);
        }

        mDataManager.writeTags();
        doFinish();
        */

        Tag tag = new Tag();
        tag.setName(name);
        tag.setFgc("");
        tag.setBgc("");
        postData(Config.URL_TAG_SAVE, tag);
    }

    private void postData(String url, final Tag tag) {
        mBtnSave.setVisibility(View.GONE);
        mLoProcessing.setVisibility(View.VISIBLE);
        //mIvProcessing.startAnimation(mRotateAnimation);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "> response: " + response);
                mBtnSave.setVisibility(View.VISIBLE);
                mLoProcessing.setVisibility(View.GONE);

                try {
                    JSONObject data = new JSONObject(response);
                    BaseApplication.getInstance().getTags().clear();
                    mDataManager.parseTags(data);
                    doFinish();
                } catch (JSONException e) {
                    Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(mId));
                if (tag != null) {
                    params.put("name", tag.getName());
                    params.put("fgc", tag.getFgc());
                    params.put("bgc", tag.getBgc());
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void removeData() {
        /*
        ArrayList<Tag> tags = new ArrayList<>();
        for (Tag bt : BaseApplication.getInstance().getTags()) {
            if (bt.getId() == mId) {
                continue;
            }
            tags.add(bt);
        }
        BaseApplication.getInstance().getTags().clear();
        BaseApplication.getInstance().getTags().addAll(tags);

        mDataManager.writeTags();
        doFinish();
        */

        postData(Config.URL_TAG_DELETE, null);
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
