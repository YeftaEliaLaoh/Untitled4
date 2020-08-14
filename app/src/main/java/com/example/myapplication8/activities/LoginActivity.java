package com.example.myapplication8.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication8.R;
import com.example.myapplication8.utilities.Config;

public class LoginActivity extends AppCompatActivity
{

    private RelativeLayout demoModeLayout;
    private Switch demoModeSwitch;

    private EditText username;
    private EditText password;
    private Button loginBtn;
    private Button cancelBtn;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initLayout();
        initEvent();

        if( Config.IS_DEMO_VERSION )
        {
            setFieldToDemoVersion(false, true, R.color.light_grey);
        }
    }


    private void setFieldToDemoVersion( boolean isFieldEnable, boolean isDemoChecked, int textColor )
    {

        username.setEnabled(isFieldEnable);
        password.setEnabled(isFieldEnable);

        username.setTextColor(getResources().getColor(textColor));
        password.setTextColor(getResources().getColor(textColor));

        String usernameTxt = Config.DEFAULT_STRING_VALUE;
        String passwordTxt = usernameTxt;

        if( isDemoChecked )
        {
            usernameTxt = username.getText().toString();

            if( usernameTxt.equals(Config.DEFAULT_STRING_VALUE) )
            {
                usernameTxt = Config.DEMO_USERNAME;
                passwordTxt = Config.DEMO_PASSWORD;
            }
        }

        username.setText(usernameTxt);
        password.setText(passwordTxt);


    }

    private void initLayout()
    {

        LinearLayout loginContentLayout = (LinearLayout) findViewById(R.id.login_layout_content);
        demoModeLayout = (RelativeLayout) findViewById(R.id.demo_mode_layout);
        demoModeLayout.setVisibility(View.GONE);
        demoModeSwitch = (Switch) findViewById(R.id.demo_mode_switch);


        username = (EditText) findViewById(R.id.user_login);
        password = (EditText) findViewById(R.id.password_login);
        loginBtn = (Button) findViewById(R.id.login_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);

    }


    private void initEvent()
    {
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("Login Activity", "Request Permission Result");

    }

}
