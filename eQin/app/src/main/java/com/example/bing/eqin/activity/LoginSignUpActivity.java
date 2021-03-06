package com.example.bing.eqin.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bing.eqin.R;
import com.example.bing.eqin.controller.UserController;
import com.example.bing.eqin.fragment.account.AccountIndexFragment;
import com.example.bing.eqin.fragment.account.LoginFragment;
import com.example.bing.eqin.model.UserProfile;
import com.example.bing.eqin.utils.CommonUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginSignUpActivity extends AppCompatActivity {

    Tencent mTencent;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private ImageView navigationIcon;
    private FragmentManager fragmentManager;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);
        toolbar = findViewById(R.id.login_toolbar);
        toolbarTitle = findViewById(R.id.login_toolbar_title);
        userController = new UserController();
        fragmentManager =  getSupportFragmentManager();
        toolbar.setTitle("");
        toolbarTitle.setText("登录");
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragment loginFragment = new LoginFragment();
                FragmentTransaction transaction =  fragmentManager.beginTransaction();
                transaction.replace(R.id.login_container, loginFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        navigationIcon = findViewById(R.id.login_toolbar_icon);
        navigationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentManager.getBackStackEntryCount()!=0)
                    fragmentManager.popBackStack();
                else
                    LoginSignUpActivity.this.finish();
            }
        });

        setSupportActionBar(toolbar);


        AccountIndexFragment accountIndexFragment = new AccountIndexFragment();
        fragmentManager.beginTransaction().replace(R.id.login_container, accountIndexFragment).commit();

        mTencent = Tencent.createInstance("101535967", getApplicationContext());
    }

    public void test(View view) {
        mTencent.login(LoginSignUpActivity.this, "all", new BaseUiListener());
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Tencent.onActivityResultData(requestCode, resultCode, data, new BaseUiListener());

        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, new BaseUiListener());
            }
        }
    }

    private class BaseUiListener implements IUiListener {

        public void onComplete(Object response) {
            Toast.makeText(getApplicationContext(), "登录成功", LENGTH_SHORT).show();
            try {
                String openidString = ((JSONObject) response).getString("openid");
                mTencent.setOpenId(openidString);
                mTencent.setAccessToken(((JSONObject) response).getString("access_token"),((JSONObject) response).getString("expires_in"));


            } catch (JSONException e) {
                e.printStackTrace();
            }

            QQToken qqToken = mTencent.getQQToken();
            UserInfo info = new UserInfo(getApplicationContext(), qqToken);

            info.getUserInfo(new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    try {
                        JSONObject userInfo =  (JSONObject) o;
                        UserProfile profile = new UserProfile();
                        profile.setNickname(userInfo.getString("nickname"));
                        profile.setGender(userInfo.getString("gender"));
                        profile.setProvince(userInfo.getString("province"));
                        profile.setCity(userInfo.getString("city"));
                        profile.setBirth_year(userInfo.getString("year"));
                        profile.setAvatarSmallUrl(userInfo.getString("figureurl_qq_1"));
                        profile.setAvatarBigUrl(userInfo.getString("figureurl_qq_2"));
                        Intent intent = new Intent();
                        ParseQuery<ParseUser> users = ParseUser.getQuery();
                        users.whereEqualTo("username", profile.getNickname());
                        List<ParseUser> userList;
                        try {
                             userList =  users.find();
                             if(userList.size()==0){ //QQ第一次登陆
                                 userController.register(profile, "loginByQQ", true);
                             }else{ //非第一次登陆
                                 ParseUser user = userList.get(0);
                                 profile.setAvatarBigUrl(user.getString("avatar"));
                                 profile.setNickname(user.getUsername());
                                 UserController.getInstance().login(profile.getNickname(),"loginByQQ");
                             }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        intent.putExtra("userAvatar", profile.getAvatarBigUrl());
                        intent.putExtra("userNickname", profile.getNickname());
                        setResult(0, intent);
                        LoginSignUpActivity.this.finish();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    Log.v("UserInfo","onError");
                }

                @Override
                public void onCancel() {
                    Log.v("UserInfo","onCancel");
                }
            });
            Toast.makeText(getApplicationContext(), response+"", LENGTH_SHORT).show();
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(getApplicationContext(), "onError", LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "onCancel", LENGTH_SHORT).show();
        }


    }
}
