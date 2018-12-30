package com.example.bing.eqin.fragment.account;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.bing.eqin.R;
import com.example.bing.eqin.controller.UserController;
import com.example.bing.eqin.model.UserProfile;
import com.tapadoo.alerter.Alerter;

import info.hoang8f.widget.FButton;

public class LoginFragment extends Fragment {

    private EditText etUsername, etUserPassword;
    private UserController controller;
    private FButton btnLogin;

    public LoginFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().findViewById(R.id.login_toolbar_title).setVisibility(View.INVISIBLE);

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        etUserPassword = view.findViewById(R.id.login_password);
        etUsername = view.findViewById(R.id.login_username);
        btnLogin = view.findViewById(R.id.login_submission);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etUserPassword.getText().toString();
                String username = etUsername.getText().toString();

                if(password.isEmpty() || username.isEmpty()){
                    Alerter.create(getActivity()).setTitle("错误").setText("必填信息为空").show();
                    return;
                }

                controller.login(username, password);
            }
        });

        return view;
    }
}