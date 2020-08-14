package com.example.myapplication8.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.firstwap.mobiletrax.Databases.MobileTraxDB;
import com.firstwap.mobiletrax.Databases.ParameterTable;
import com.firstwap.mobiletrax.R;
import com.firstwap.mobiletrax.broadcast_receivers.LoginRespondReceiver;
import com.firstwap.mobiletrax.broadcast_receivers.SMSSwitchReceiver;
import com.firstwap.mobiletrax.controller.LoginController;
import com.firstwap.mobiletrax.controller.PermissionController;
import com.firstwap.mobiletrax.services.MobileTraxServices;
import com.firstwap.mobiletrax.singleton.UserSingleton;
import com.firstwap.mobiletrax.util.Config;
import com.firstwap.mobiletrax.util.Global;
import com.firstwap.mobiletrax.util.LogWriter;
import com.firstwap.mobiletrax.util.MobileTraxPref;
import com.firstwap.mobiletrax.util.Utility;
import com.firstwap.mobiletrax.util.phonebook.ContactReceiver;
import com.firstwap.mobiletrax.util.phonebook.utils.Contact;
import com.firstwap.mobiletrax.util.socket.interfaces.SocketInterface;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The activity manage login page
 * Created by rizki on 23/05/16.
 */
public class LoginActivity extends AppCompatActivity{
    private static MobileTraxDB database;
    private LoginController loginController;
    private MobileTraxPref mobileTraxPref;
    private SocketInterface socketInterface;
    private LoginRespondReceiver loginRespondReceiver;

    private RelativeLayout loginLayoutParent;
    private RelativeLayout loginLayoutActivity;

    private RelativeLayout demoModeLayout;
    private Switch demoModeSwitch;

    private EditText username;
    private EditText password;
    private EditText serverNumber;
    private Button loginBtn;
    private Button cancelBtn;
    private TextView loadingStatus;
    private RelativeLayout loadingLayout;
    private PermissionController permissionController;
    private BroadcastReceiver contactDialogReceiver;
    private Contact contact;
    private ParameterTable parameterTable;

    private int count = Global.DEFAULT_INT_VALUE;
    private long startMillis = Global.DEFAULT_INT_VALUE;

    private SMSSwitchReceiver smsSwitchReceiver;

    private boolean IS_SECURED_COMMUNICATION_ALLOWED;

    public static MobileTraxDB getDatabase(){
        return database;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        permissionController = new PermissionController(this, Global.LOGIN_CONFIG);
        parameterTable = MobileTraxDB.getInstance().getParameterTable();
        IS_SECURED_COMMUNICATION_ALLOWED = MobileTraxDB.getInstance().getParameterTable().getProtocol();


        initData();
        initLayout();
        initEvent();

        boolean isDemoModeAllowed = parameterTable.isDemoModeAllowed();

        if(isDemoModeAllowed)
        {
            initLayoutDemoMode();
        }
        else
        {
            if(Config.IS_DEMO_VERSION)
            {
                setFieldToDemoVersion(false, true, R.color.light_grey);
            }
        }
    }

    private void setFieldToDemoVersion(boolean isFieldEnable, boolean isDemoChecked, int textColor){

        username.setEnabled(isFieldEnable);
        password.setEnabled(isFieldEnable);
        serverNumber.setEnabled(isFieldEnable);

        username.setTextColor(getResources().getColor(textColor));
        password.setTextColor(getResources().getColor(textColor));
        serverNumber.setTextColor(getResources().getColor(textColor));

        String usernameTxt = Global.DEFAULT_STRING_VALUE;
        String passwordTxt = usernameTxt;

        if(isDemoChecked)
        {
            usernameTxt = username.getText().toString();

            if(usernameTxt.equals(Global.DEFAULT_STRING_VALUE))
            {
                usernameTxt = Global.DEMO_USERNAME;
                passwordTxt = Global.DEMO_PASSWORD;
            }
            else if(usernameTxt.equals(Global.DEMO_USERNAME2))
                passwordTxt = Global.DEMO_PASSWORD;
        }

        username.setText(usernameTxt);
        password.setText(passwordTxt);

        String virtualNumber = MobileTraxDB.getInstance().getParameterTable().getServerPhoneNumber();

        if((null != virtualNumber) && !virtualNumber.equals(Global.DEFAULT_STRING_VALUE))
        {
            serverNumber.setText(virtualNumber);
            return;
        }

        serverNumber.setText(Global.DEMO_VIRTUAL_NUMBER);

    }

    private void initLayout(){

        loginLayoutParent = (RelativeLayout) findViewById(R.id.login_layout_parent);
        loginLayoutActivity = (RelativeLayout) findViewById(R.id.login_layout_activity);
        RelativeLayout loginHeaderLayout = (RelativeLayout) findViewById(R.id.login_layout_header);
        LinearLayout loginContentLayout = (LinearLayout) findViewById(R.id.login_layout_content);
        LinearLayout loginFooterLayout = (LinearLayout) findViewById(R.id.login_layout_footer);
        demoModeLayout = (RelativeLayout) findViewById(R.id.demo_mode_layout);
        demoModeLayout.setVisibility(View.GONE);
        demoModeSwitch = (Switch) findViewById(R.id.demo_mode_switch);

        socketInterface = new SocketInterface(this);

        username = (EditText) findViewById(R.id.user_login);
        password = (EditText) findViewById(R.id.password_login);
        loginBtn = (Button) findViewById(R.id.login_btn);
        TextView copyRight = (TextView) findViewById(R.id.copyright);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        copyRight.setTextSize(12);
        copyRight.setText(String.format(getString(R.string.text_footer), "v" + UserSingleton.getInstance().getCurrentVersionName(), Utility.getYear()));

        String serverPhoneNumber = MobileTraxDB.getInstance().getParameterTable().getServerPhoneNumber();
        serverNumber.setText(serverPhoneNumber);
    }

    private void initLayoutDemoMode(){

        boolean isDemoModeAllowed = parameterTable.isDemoModeAllowed();

        if(isDemoModeAllowed)
        {
            demoModeLayout.setVisibility(View.VISIBLE);
            initEventDemo();
        }

    }

    private void initEvent(){
        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick( View v){
                initLogin();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick( View v){
                if(loginRespondReceiver!=null)
                    unregisterReceiver(loginRespondReceiver);
                loginRespondReceiver = null;
                MobileTraxDB.getInstance().getOutgoingMessageTable().updateOutgoingStatus(UserSingleton.getInstance().getRequestId(), Global.SMS_STATUS_CANCELLED);
                loginController.showHiddenButton(loginBtn, View.VISIBLE, R.anim.enter_from_right, R.color.blue_component);
                loginController.showHiddenButton(cancelBtn, View.GONE, R.anim.exit_to_left, R.color.colorRedClicked);

                if(UserSingleton.getInstance().isSocketAllowed() && UserSingleton.getInstance().isSocket())
                {
                    socketInterface.cancelSocketAction(Global.CATEGORY_LOGIN);
                }

            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    initLogin();
                    return true;
                }
                return false;
            }
        });

        loginLayoutActivity.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch( View v, MotionEvent event){
                tapSeveralTimes();
                return false;
            }
        });


        loginLayoutParent.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch( View v, MotionEvent event){
                tapSeveralTimes();
                return false;
            }
        });


        prepareService();
    }

    private void initLogin(){
        if(Config.isAndroidVersionHigherAPI22)
        {
            String[] loginPermissions = Global.LOGIN_ACTION_PERMISSION_GROUP;
            String usernameTxt = username.getText().toString();
            String passwordTxt = password.getText().toString();
            String serverPhoneNumber = serverNumber.getText().toString();
            boolean existingUser = database.getUserTable().isExistingUser(usernameTxt, passwordTxt);

            if(!existingUser)
            {
                loginController.sendUserToDatabase(usernameTxt, passwordTxt);
                loginController.isExistingLogin(usernameTxt, passwordTxt, serverPhoneNumber);
            }
            else
                loginController.isExistingLogin(usernameTxt, passwordTxt, serverPhoneNumber);

            if(IS_SECURED_COMMUNICATION_ALLOWED)
            {
                loginPermissions = permissionController.getSslPermissions(loginPermissions);
            }

            ArrayList<String> deniedPermissionListLogin = permissionController.getDeniedPermissionList(loginPermissions);
            if(!deniedPermissionListLogin.isEmpty())
            {
                permissionController.onRequestPermission(deniedPermissionListLogin.toArray(new String[deniedPermissionListLogin.size()]), Global.MULTIPLE_LOGIN_REQUEST_ID, false);
            }
            else
            {
                initiateLoginRequest();
            }
        }
        else
        {
            initiateLoginRequest();
        }
    }

    private void initEventDemo(){

        demoModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked){
                changesComponentDemoMode(isChecked);
            }
        });

    }

    private void changesComponentDemoMode(boolean isChecked){

        boolean isDataCommunicationAllowed = MobileTraxDB.getInstance().getParameterTable().isDataCommunicationAllowed();
        mobileTraxPref.setDemoChecked(isChecked);

        if(isChecked)
        {

            if(isDataCommunicationAllowed)
            {
                if(socketInterface.getSocketSwitch().isChecked())
                {
                    socketInterface.getHostField().setVisibility(View.GONE);
                    socketInterface.getPortField().setVisibility(View.GONE);
                    socketInterface.getSocketSwitch().setChecked(false);
                }

                socketInterface.getPortField().setVisibility(View.GONE);
                socketInterface.getHostField().setVisibility(View.GONE);
                socketInterface.getSocketLayout().setVisibility(View.INVISIBLE);
            }

            setFieldToDemoVersion(false, isChecked, R.color.light_grey);

        }
        else
        {

            if(isDataCommunicationAllowed)
            {
                socketInterface.getSocketLayout().setVisibility(View.VISIBLE);
            }

            setFieldToDemoVersion(true, isChecked, R.color.white);
        }


    }

    private void tapSeveralTimes(){
        int tapEmptyAreas = 7;      // user tap empty space arround 7 times
        int tapInsideTime = 5000;   // 5 second
        int firsTimetap = 1;

        long time = System.currentTimeMillis();

        if(startMillis == Global.DEFAULT_INT_VALUE || (time - startMillis > tapInsideTime))
        {
            startMillis = time;
            count = firsTimetap;
        }
        else
        {
            count++;
        }

        if(count == tapEmptyAreas)
        {
            if(Config.isAndroidVersionHigherAPI22)
            {
                permissionController.setStorageEverAsked(false);
                if(!permissionController.getDeniedPermissionList(Global.STORAGE_PERMISSION_GROUP).isEmpty())
                {
                    permissionController.onRequestPermission(Global.STORAGE_PERMISSION_GROUP, Global.STORAGE_REQUEST_ID, false);
                }
                else
                {
                    try
                    {
                        Utility.writeToExternalStorage(getBaseContext());
                    }
                    catch( IOException e)
                    {
                        e.printStackTrace();
                        Log.d("Empty", "" + tapEmptyAreas);
                    }
                }
            }
            else
            {
                try
                {
                    Utility.writeToExternalStorage(getBaseContext());
                }
                catch( IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        if(smsSwitchReceiver == null)
        {
            smsSwitchReceiver = new SMSSwitchReceiver(this);
            registerReceiver(smsSwitchReceiver, new IntentFilter(Global.INTENT_SWITCH_SMS));
        }

        if(!database.isDatabaseOpened())
        {
            database.open();
        }

        if(loginRespondReceiver == null)
        {
            loginRespondReceiver = new LoginRespondReceiver(this);
            registerReceiver(loginRespondReceiver, new IntentFilter(Global.INTENT_LOGIN));
        }


        if(Config.isAndroidVersionHigherAPI22)
        {
            if(permissionController.getDeniedPermissionList(Global.STORAGE_PERMISSION_GROUP).isEmpty())
            {
                permissionController.dismissStorageDialog();
            }

            if(permissionController.getDeniedPermissionList(Global.CONTACT_PERMISSION_GROUP).isEmpty())
            {
                if(null != loginController && null != loginController.getContactDialog())
                {
                    loginController.getContactDialog().registerPhoneBookObserver();
                }

                if(null == contactDialogReceiver)
                {
                    contactDialogReceiver = new ContactReceiver(this);
                    registerReceiver(contactDialogReceiver, new IntentFilter(Contact.INTENT_PHONE_BOOK));
                }
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();

        if(Config.isAndroidVersionHigherAPI22)
        {
            if(permissionController.getDeniedPermissionList(Global.CONTACT_PERMISSION_GROUP).isEmpty())
            {
                if(null != contactDialogReceiver)
                {
                    unregisterReceiver(contactDialogReceiver);
                    contactDialogReceiver = null;
                }
            }
            if(!permissionController.getDeniedPermissionList(Global.STORAGE_PERMISSION_GROUP).isEmpty())
            {
                MobileTraxPref.getInstance(this).putBoolean(Global.PREF_STORAGE_PERMISSION, false);
            }
        }

        if(null != loginRespondReceiver)
        {
            unregisterReceiver(loginRespondReceiver);
            loginRespondReceiver = null;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(smsSwitchReceiver != null)
        {
            unregisterReceiver(smsSwitchReceiver);
        }

        if(Config.isAndroidVersionHigherAPI22)
        {
            if(permissionController.getDeniedPermissionList(Global.CONTACT_PERMISSION_GROUP).isEmpty())
            {
                if(null != loginController.getContactDialog())
                {
                    loginController.getContactDialog().unregisterPhoneBookObserver();
                }
            }
        }

    }

    /**
     * Get MobileTrax Preferences
     *
     * @return mobileTraxPref
     */
    public MobileTraxPref getMobileTraxPref(){
        return mobileTraxPref;
    }

    private boolean isMyServiceRunning( Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for( ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    private void prepareService(){
        if(!isMyServiceRunning(MobileTraxServices.class))
        {
            Intent i = new Intent(this, MobileTraxServices.class);
            startService(i);
        }
        else
        {
            Log.d("LoginActivity", "Service Is Running");
        }
    }

    /**
     * Initialize Data
     */
    private void initData(){
        contact = new Contact(this);
        loginController = new LoginController(this);
        mobileTraxPref = MobileTraxPref.getInstance(this);
        String username = mobileTraxPref.getString(Global.LOGIN_USERNAME_KEY, Global.DEFAULT_STRING_VALUE);
        String password = mobileTraxPref.getString(Global.LOGIN_PASSWORD_KEY, Global.DEFAULT_STRING_VALUE);
        String serverPhoneNumber = mobileTraxPref.getString(Global.LOGIN_SERVERNUMBER_KEY, Global.DEFAULT_STRING_VALUE);
        long sessionId = mobileTraxPref.getLong(Global.LOGIN_SESSIONID_KEY, Global.DEFAULT_INT_VALUE);

        if(null == database)
        {
            database = new MobileTraxDB(this);
            database.open();
        }
        else if(!database.isDatabaseOpened())
        {
            database.open();
        }

        loadingLayout = (RelativeLayout) findViewById(R.id.login_layout_progress);
        loadingStatus = (TextView) findViewById(R.id.login_loading_status);
        serverNumber = (EditText) findViewById(R.id.server_number);
        loginController.checkInstallationStatus(loadingStatus, database, loadingLayout, serverNumber);
        loginController.isExistingLogin(username, password, serverPhoneNumber);
    }

    public void initiateLoginRequest(){
        try
        {
            if(validate())
            {

                /**
                 * Introduced by <a href="https://project.1rstwap.com/issues/23595">23595</a>
                 */
                if(socketInterface.isConfigurationSet())
                {

                    int userId = database.getUserTable().getUserId(username.getText().toString(), password.getText().toString());
                    loginController.showHiddenButton(loginBtn, View.GONE, R.anim.exit_to_right, R.color.blue_component_clicked);
                    loginController.showHiddenButton(cancelBtn, View.VISIBLE, R.anim.enter_from_left, R.color.colorRed);

                    loginController.login(userId, username.getText().toString(), password, serverNumber.getText().toString());

                }

            }
        }
        catch( Exception e)
        {
            LogWriter.getInstance(this).appendDebugLog("Login Activity service - Error occured during initiateLoginRequest.");
            LogWriter.getInstance(this).appendErrorLog(e.getMessage());
        }
    }

    /**
     * Validate Login Form
     *
     * @return boolean
     */
    private boolean validate(){
        boolean valid = true;
        boolean isNeedToValidateVN = socketInterface.isNeedToValidateVN();

        if(username.getText().toString().equalsIgnoreCase(Global.DEFAULT_STRING_VALUE))
        {
            valid = false;
            username.setError(getResources().getString(R.string.text_err_user_empty_login));
        }

        if(password.getText().toString().equalsIgnoreCase(Global.DEFAULT_STRING_VALUE))
        {
            valid = false;
            password.setError(getResources().getString(R.string.text_err_pass_empty_login));
        }

        if(isNeedToValidateVN)
        {
            if(!android.util.Patterns.PHONE.matcher(serverNumber.getText().toString()).matches())
            {
                valid = false;
                if(serverNumber.getText().toString().equalsIgnoreCase(Global.DEFAULT_STRING_VALUE))
                {
                    serverNumber.setError(getResources().getString(R.string.text_err_sn_empty_login));
                }
                else if(!android.util.Patterns.PHONE.matcher(serverNumber.getText().toString()).matches())
                {
                    serverNumber.setError(getResources().getString(R.string.text_err_sn_format_login));
                }
            }
        }


        UserSingleton.getInstance().setAbleToSwitchSMS(isNeedToValidateVN);

        UserSingleton.getInstance().setAbleToSwitchSMS(isNeedToValidateVN);

        return valid;
    }

    public LoginController getLoginController(){
        return loginController;
    }

    public PermissionController getPermissionController(){
        return permissionController;
    }

    public Button getLoginBtn(){
        return loginBtn;
    }

    public Button getCancelBtn(){
        return cancelBtn;
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("Login Activity", "Request Permission Result");

        permissionController.onResponsePermission(requestCode, permissions, grantResults);
    }

    public EditText getServerNumber(){
        return serverNumber;
    }

    public Contact getContact(){
        return contact;
    }

    public TextView getLoadingStatus(){
        return loadingStatus;
    }

    public RelativeLayout getLoadingLayout(){
        return loadingLayout;
    }
}
