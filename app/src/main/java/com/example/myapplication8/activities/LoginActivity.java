package com.example.myapplication8.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myapplication8.R;
import com.example.myapplication8.utilities.Config;

public class LoginActivity extends AppCompatActivity
{
    private SwitchCompat switchCompat;
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
        setFieldToDemoVersion(Config.IS_DEMO_VERSION, R.color.light_grey);
    }

    private void setFieldToDemoVersion( boolean isDemoChecked, int textColor )
    {
        username.setTextColor(getResources().getColor(textColor));
        password.setTextColor(getResources().getColor(textColor));

        String usernameTxt = Config.DEFAULT_STRING_VALUE;
        String passwordTxt = Config.DEFAULT_STRING_VALUE;
        Config.IS_DEMO_VERSION = false;

        if( isDemoChecked )
        {
            usernameTxt = Config.DEMO_USERNAME;
            passwordTxt = Config.DEMO_PASSWORD;
            Config.IS_DEMO_VERSION = true;
        }

        username.setText(usernameTxt);
        password.setText(passwordTxt);
    }

    private void initLayout()
    {
        switchCompat = findViewById(R.id.demo_mode_switch);
        username = findViewById(R.id.user_login);
        password = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.login_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
    }

    private void initEvent()
    {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            requestPermissions(Config.STORAGE_PERMISSION_GROUP, Config.STORAGE_REQUEST_ID);
        }

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                showHiddenButton(loginBtn, View.GONE, R.anim.exit_to_right, R.color.blue_component_clicked);
                showHiddenButton(cancelBtn, View.VISIBLE, R.anim.enter_from_left, R.color.colorRed);
                if( Config.IS_DEMO_VERSION )
                {
                    successToLogin();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                showHiddenButton(loginBtn, View.VISIBLE, R.anim.enter_from_right, R.color.blue_component);
                showHiddenButton(cancelBtn, View.GONE, R.anim.exit_to_left, R.color.colorRedClicked);
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

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( CompoundButton compoundButton, boolean isChecked )
            {
                changesComponentDemoMode(isChecked);
            }
        });
    }

    public void successToLogin()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void showHiddenButton( Button button, int visibility, int animation, int color )
    {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), animation);

        switch ( visibility )
        {
            case View.VISIBLE:
            case View.GONE:
                button.setAnimation(anim);
                break;
            default:
                break;
        }

        button.setBackgroundColor(getApplicationContext().getResources().getColor(color));
        button.setVisibility(visibility);
    }

    private void changesComponentDemoMode( boolean isChecked )
    {
        if( isChecked )
        {
            setFieldToDemoVersion(true, R.color.light_grey);
        }
        else
        {
            setFieldToDemoVersion(false, R.color.white);
        }
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
    }
}
