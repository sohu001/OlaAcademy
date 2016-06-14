package com.snail.olaxueyuan.ui.me.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.snail.olaxueyuan.R;
import com.snail.olaxueyuan.protocol.manager.SEAuthManager;
import com.snail.olaxueyuan.protocol.result.SEUserResult;
import com.snail.olaxueyuan.ui.MainActivity;
import com.snail.olaxueyuan.ui.activity.SEBaseActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserLoginActivity extends SEBaseActivity {

    private EditText phoneET;
    private EditText passET;
    private Button loginBtn;
    private TextView forgetTV;

    public static int PASS_FOREGT = 0x0101;
    public static int USER_REG = 0x0202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        setTitleText("登录");
        setLeftImageInvisibility();
        setRightImageInvisibility();

        setLeftText("取消");
        setLeftTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setRightText("注册");
        setRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLoginActivity.this,UserRegActivity.class);
                startActivityForResult(intent,USER_REG);
            }
        });

        phoneET = (EditText) findViewById(R.id.et_phone);
        passET = (EditText) findViewById(R.id.et_pass);

        forgetTV = (TextView) findViewById(R.id.tv_forget);
        forgetTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLoginActivity.this, UserPasswordActivity.class);
                startActivityForResult(intent, PASS_FOREGT);
            }
        });

        loginBtn = (Button) findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        loginBtn.setEnabled(false);
        SEAuthManager am = SEAuthManager.getInstance();
        am.authWithUsernamePassword(phoneET.getText().toString(), passET.getText().toString(), new Callback<SEUserResult>() {
            @Override
            public void success(SEUserResult result, Response response) {
                loginBtn.setEnabled(true);
                int isVisitor = getIntent().getIntExtra("isVisitor", 0);
                if (isVisitor == 1) {
                    Intent intent = getIntent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", result.data);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                loginBtn.setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PASS_FOREGT && resultCode == RESULT_OK) {
            phoneET.setText(data.getStringExtra("phone"));
            passET.setText(data.getStringExtra("password"));
        }
    }
}
