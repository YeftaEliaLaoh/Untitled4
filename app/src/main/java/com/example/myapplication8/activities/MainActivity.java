package com.example.myapplication8.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.example.myapplication8.R;
import com.example.myapplication8.utilities.Config;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private ImageView toolsButtonShowOsm;
    private ImageView mapSpinner;
    private RelativeLayout mapLayout;
    private GoogleMap googleMap;
    private PopupWindow popup;
    private MapView mapView;
    private SupportMapFragment supportMapFragment;
    private int selectedMapType = 0;
    private GestureDetector detector;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayout();
        initData();
        initEvent();
        //showGoogleMap();
    }

    private void initLayout()
    {
        mapLayout = findViewById(R.id.map_fragment);
        mapSpinner = findViewById(R.id.map_spinner);
        toolsButtonShowOsm = findViewById(R.id.button_tools_show_osm_map);
        toolsButtonShowOsm.setVisibility(View.GONE);
        mapSpinner.setVisibility(View.VISIBLE);
        mapView = findViewById(R.id.view_osm_map);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void initData()
    {
//        supportMapFragment.setRetainInstance(true);
//        supportMapFragment.getMapAsync(this);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
    }

    private void initEvent()
    {

        mapSpinner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayPopupWindow(v);
            }
        });

        detector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener()
        {

            @Override
            public boolean onDown( MotionEvent e )
            {
                return true;
            }

            @Override
            public boolean onDoubleTap( MotionEvent e )
            {

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed( MotionEvent e )
            {
                return true;
            }
        });


    }

    private void displayPopupWindow(View anchorView)
    {
        mapSpinner.setVisibility(View.INVISIBLE);
        popup = new PopupWindow(this);
        View layout = getLayoutInflater().inflate(R.layout.popup_menu_switcher, null);
        ImageView mapMinSpinner = layout.findViewById(R.id.map_min_spinner);
        int mapLayoutWidth = mapLayout.getResources().getDisplayMetrics().widthPixels / 2;

        popup.setContentView(layout);

        RadioGroup switcher = layout.findViewById(R.id.radioSwitcher);

        //set button map
        if (selectedMapType == Config.GOOGLEMAP)
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
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == R.id.googlemap)
                {
                    selectedMapType = Config.GOOGLEMAP;
                }
                else
                {
                    selectedMapType = Config.OPENSTREETMAP;
                }
                replaceTiles();
            }
        });
        popup.setBackgroundDrawable(null);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(mapLayoutWidth);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);

        popup.setOnDismissListener(new PopupWindow.OnDismissListener()
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
            public void onClick(View v)
            {
                popup.dismiss();
                mapSpinner.setVisibility(View.VISIBLE);
            }
        });

        popup.showAsDropDown(anchorView, -242, -32);
    }

    private void replaceTiles()
    {
        if (selectedMapType == Config.OPENSTREETMAP)
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
        toolsButtonShowOsm.setVisibility(View.VISIBLE);
        toolsButtonShowOsm.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return detector.onTouchEvent(event);
            }
        });

    }

    private void showGoogleMap()
    {

        //MapSingleton.getInstance().setSelectedMap(Config.GOOGLEMAP);
        supportMapFragment.getView().setVisibility(View.VISIBLE);
        toolsButtonShowOsm.setVisibility(View.GONE);
        /*if( googleMap != null )
        {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) (mapWrapper.zoomLevelGoogle + 2)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo((float) (mapWrapper.zoomLevelGoogle + 2)));
        }
        mapWrapper.redrawLocations();*/

    }

    private void hideGoogleMap()
    {
        if( supportMapFragment != null )
        {
            supportMapFragment.getView().setVisibility(View.GONE);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {

    }
}
