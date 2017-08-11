package com.example.liu.utext.view.activity;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liu.utext.App;
import com.example.liu.utext.R;
import com.example.liu.utext.component.DaggerForgetPasswordComponent;
import com.example.liu.utext.module.ForgetPasswordModule;
import com.example.liu.utext.presenter.ForgetPasswordPresenter;
import com.example.liu.utext.util.BindView;
import com.example.liu.utext.util.CountTimerView;
import com.example.liu.utext.util.SMSEventHandle;
import com.example.liu.utext.util.ToastUtils;
import com.example.liu.utext.view.BaseView;

import java.util.List;

import cn.smssdk.SMSSDK;

public class ForgetPasswordActivity extends BaseActivity<ForgetPasswordPresenter> implements BaseView{

    @BindView(R.id.fg_phoneNumber)
    private EditText mEditText1;
    @BindView(R.id.fg_password)
    private EditText mEditText2;
    @BindView(R.id.fg_verificationCode)
    private EditText mEditText3;
    @BindView(R.id.fg_getVerificationCode)
    private Button mButton1;
    @BindView(R.id.fg_verify)
    private Button mButton2;
    private MyEventHandle mMyEventHandle;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_forget_password;
    }

    @Override
    protected void setPresenter() {
        DaggerForgetPasswordComponent.builder()
                .appComponent(((App) getApplication()).getAppComponent())
                .forgetPasswordModule(new ForgetPasswordModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void init() {
        mMyEventHandle = new MyEventHandle(this);
        SMSSDK.registerEventHandler(mMyEventHandle);
        mEditText1.setText(getIntent().getStringExtra("phoneNumber"));
    }

    @Override
    protected void ViewOnClick() {
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPhoneNum(mEditText1.getText().toString())) {
                    SMSSDK.getVerificationCode("+86", mEditText1.getText().toString());
                }
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SMSSDK.submitVerificationCode("+86",
                        mEditText1.getText().toString(), mEditText3.getText().toString());
            }
        });
    }

    private boolean checkPhoneNum(String phone) {
        if (TextUtils.isEmpty(phone)) {
            ToastUtils.show(this, "请输入手机号码");
            return false;
        }
        if (phone.length() != 11) {
            ToastUtils.show(this, "手机号码长度不对");
            return false;
        }
        return true;
    }

    private void updatePassword() {
        presenter.requestData(1, mEditText1.getText().toString(), mEditText2.getText().toString());
    }

    private void returnMainActivity() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mMyEventHandle);
    }

    @Override
    public void showData(int id, String message, List mData) {
        ToastUtils.show(this, message);
    }

    @Override
    public void showErrorMessage(int id, String message) {
        ToastUtils.show(this, message);
    }

    private class MyEventHandle extends SMSEventHandle {

        private MyEventHandle(Activity activity) {
            super(activity);
        }

        @Override
        public void afterGetCode() {
            CountTimerView timerView = new CountTimerView(mButton1, R.string.resend_code);
            timerView.start();
            Toast.makeText(ForgetPasswordActivity.this, "短信已发送", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void afterSubmitCode() {
            updatePassword();
            returnMainActivity();
        }
    }
}
