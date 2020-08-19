package com.example.myapplication8.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.example.myapplication8.R;
import com.example.myapplication8.controllers.LeftPaneController;
import com.example.myapplication8.controllers.MapController;
import com.example.myapplication8.databases.AppDatabase;
import com.example.myapplication8.fragment.SessionListFragment;
import com.example.myapplication8.models.MapSingleton;
import com.example.myapplication8.models.Session;
import com.example.myapplication8.utilities.Config;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private ImageView mapSpinner;
    private GoogleMap googleMap;
    private PopupWindow popupWindow;
    private MapView mapView;
    private SupportMapFragment supportMapFragment;
    private int selectedMapType = 0;
    private MapController mapController;
    private LeftPaneController leftPaneController;
    public AsyncTask<Void, Session, ArrayList<Session>> sessionAsyncTask;
    private SessionListFragment sessionListFragment;

    public AppDatabase getAppDatabase()
    {
        return appDatabase;
    }

    public void setAppDatabase( AppDatabase appDatabase )
    {
        this.appDatabase = appDatabase;
    }

    private AppDatabase appDatabase;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayout();
        initData();
        initEvent();
        populateSessions();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if( sessionAsyncTask != null )
        {
            sessionAsyncTask.cancel(false);
        }
    }

    private void populateSessions()
    {
        leftPaneController.getTextEmptyList().setVisibility(View.GONE);
        sessionAsyncTask = new AsyncTask<Void, Session, ArrayList<Session>>()
        {

            @Override
            protected ArrayList<Session> doInBackground( Void... params )
            {
                ArrayList<Session> sessionList = (ArrayList<Session>) appDatabase.sessionDao().getAll();
                for( Session session : sessionList )
                {
                    publishProgress(session);
                }

                return sessionList;
            }

            @Override
            protected void onProgressUpdate( Session... session )
            {
                sessionListFragment.addSession(session[0]);
            }

            @Override
            protected void onPostExecute( ArrayList<Session> result )
            {
                if( result.size() < 1 )
                {
                    leftPaneController.getTextEmptyList().setVisibility(View.VISIBLE);
                    leftPaneController.getTextEmptyList().setText(getString(R.string.label_no_session));
                }
            }
        };
        sessionAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initLayout()
    {
        leftPaneController = new LeftPaneController(this);
        mapSpinner = findViewById(R.id.map_spinner);
        mapView = findViewById(R.id.view_osm_map);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void initData()
    {
        mapSpinner.setVisibility(View.VISIBLE);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        supportMapFragment.setRetainInstance(true);
        supportMapFragment.getMapAsync(this);

        if( (sessionListFragment == null || !sessionListFragment.isAdded()) && getSupportFragmentManager().getBackStackEntryCount() == 0 )
        {
            sessionListFragment = new SessionListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, sessionListFragment).commit();
        }
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name"
        ).allowMainThreadQueries().build();
    }

    private void initEvent()
    {
        mapController = new MapController(mapView, this);
        mapSpinner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                displayPopupWindow(v);
            }
        });
    }

    private void displayPopupWindow( View anchorView )
    {
        mapSpinner.setVisibility(View.INVISIBLE);
        popupWindow = new PopupWindow(this);
        View layout = getLayoutInflater().inflate(R.layout.popup_menu_switcher, null);
        ImageView mapMinSpinner = layout.findViewById(R.id.map_min_spinner);
        popupWindow.setContentView(layout);

        RadioGroup switcher = layout.findViewById(R.id.radioSwitcher);

        //set button map
        if( selectedMapType == Config.GOOGLE_MAP )
        {
            switcher.check(R.id.googlemap);
        }
        else
        {
            switcher.check(R.id.osm);
        }

        switcher.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( RadioGroup group, int checkedId )
            {
                if( checkedId == R.id.googlemap )
                {
                    selectedMapType = Config.GOOGLE_MAP;
                }
                else
                {
                    selectedMapType = Config.OPEN_STREET_MAP;
                }
                replaceTiles();
            }
        });
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                mapSpinner.setVisibility(View.VISIBLE);
            }
        });

        mapMinSpinner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                popupWindow.dismiss();
                mapSpinner.setVisibility(View.VISIBLE);
            }
        });

        popupWindow.showAsDropDown(anchorView, -242, -32);
    }

    private void replaceTiles()
    {
        if( selectedMapType == Config.OPEN_STREET_MAP )
        {
            showOsmMap();
        }
        else
        {
            showGoogleMap();
        }
    }

    private void showOsmMap()
    {
        hideGoogleMap();
        mapController.getOpenStreetMapController().showDetailsOsmMap();
        mapController.getOpenStreetMapController().registerEventOsm(mapController.getRegisterEventOsm());
    }

    private void hideGoogleMap()
    {
        if( supportMapFragment != null )
        {
            supportMapFragment.getView().setVisibility(View.GONE);
        }
    }

    private void showGoogleMap()
    {
        hideOsmMap();
        MapSingleton.getInstance().setSelectedMap(Config.GOOGLE_MAP);
        supportMapFragment.getView().setVisibility(View.VISIBLE);
        if( googleMap != null )
        {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) (mapController.zoomLevelGoogle + 2)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo((float) (mapController.zoomLevelGoogle + 2)));
        }
    }

    private void hideOsmMap()
    {
        if( mapView != null )
        {
            mapView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady( GoogleMap googleMap )
    {
        this.googleMap = googleMap;
        mapView.setVisibility(View.GONE);
        mapController.setGoogleMap(this, this.googleMap);
        mapController.registerEventMapWrapper();
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
