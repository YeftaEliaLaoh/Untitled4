package com.example.myapplication8.fragments.

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.myapplication8.R;
import com.example.myapplication8.activities.MainActivity;
import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.CellCalculation;
import com.example.myapplication8.models.ItemCellCalculation;
import com.example.myapplication8.models.ListviewAdapter;
import com.example.myapplication8.models.ListviewItem;
import com.example.myapplication8.models.MapSingleton;
import com.example.myapplication8.models.MeasuredLocation;
import com.example.myapplication8.models.Session;
import com.example.myapplication8.utilities.Config;
import com.example.myapplication8.utilities.SwipeRefreshLayoutBottom;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import java.util.ArrayList;

public class ScannedListFragment extends Fragment
{
    static
    {
        ListviewItem.addTypeCount(ItemCellCalculation.class);
    }

    private ListviewAdapter mAdapter;
    private SwipeRefreshLayoutBottom mSwipe;
    private ArrayList<ListviewItem> mData = new ArrayList<>();
    private ArrayList<CellCalculation> selectedRadio;
    private MainActivity mainActivity;

    private int selectedCell;
    private Session session;

    private boolean isPointIncluded = false;
    private boolean addWifiToRadioList = false;
    private boolean addCellToRadioList = false;

    private int selectedItem = 0;

    private SparseArray<MeasuredLocation> locationHashMap;
    private boolean selectionOnClear = false;

    private LatLngBounds.Builder builder;
    private LatLngBounds bounds;

    public ScannedListFragment()
    {
        locationHashMap = new SparseArray<>();
        builder = new LatLngBounds.Builder();
    }

    public ArrayList<CellCalculation> getSelectedRadio()
    {
        return this.selectedRadio;
    }

    public void clearListviewData()
    {
        mData.clear();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_listview, container, false);
        mAdapter = new ListviewAdapter(this.getActivity(), 0, mData);

        //use to refresh list
        mSwipe = new SwipeRefreshLayoutBottom(getActivity());
        mSwipe.setOnRefreshListener(new SwipeRefreshLayoutBottom.IOnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                onRefreshScannedResults();
            }
        });

        ListView mListView = new ListView(getActivity());
        mSwipe.addView(mListView);
        mListView.setCacheColorHint(Color.WHITE);
        mListView.requestFocus(0);
        FrameLayout mRootLayout = view.findViewById(R.id.root_layout);
        mRootLayout.setForeground(new ColorDrawable(Color.BLACK));
        mRootLayout.getForeground().setAlpha(0);
        mRootLayout.addView(mSwipe);
        mListView.setAdapter(mAdapter);

        selectedRadio = new ArrayList<>();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                ListviewItem item = (ListviewItem) parent.getItemAtPosition(position);
                selectUnselectRow(item);
            }
        });

        selectedCell = 0;

        Bundle args = getArguments();
        if( null != args.getParcelable("session") )
        {
            session = args.getParcelable("session");
        }

        populateScannedList(session);

        return view;
    }

    public void populateScannedList( Session session )
    {
        if( mainActivity instanceof MainActivity )
        {
            populateCellInfoAsync(mainActivity, session);
        }

    }

    public ArrayList<ListviewItem> getData()
    {
        return mData;
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach(context);
        if( context instanceof MainActivity )
        {
            mainActivity = (MainActivity) context;
        }
    }

    private synchronized void populateCellInfoAsync( final MainActivity mActivity, final Session session )
    {
        ArrayList<Cell> list;

        if( session.getCellList().size() > 0 )
        {
            list = session.getCellList();
        }
        else
        {
            list = mainActivity.getAppDatabase().cellDao().getUniqueCellBySessionIdLimited(session.getId(), 0);
        }

        session.setCellList(list);
        addCellToRadioList = selectedCell != session.getCellList().size();

        selectedCell = 0;
        for( Cell cell : list )
        {
            new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cell);
        }
    }

    private void selectUnselectRow( ListviewItem item )
    {
        item.onSelected = !item.onSelected;

        ItemCellCalculation itemCellCalculation = (ItemCellCalculation) item;
        CellCalculation cellCalculation = itemCellCalculation.getCellCalculation();

        if( !itemCellCalculation.onSelected )
        {
            selectedItem--;
            unselectRadioItem(cellCalculation);
            selectedRadio.remove(cellCalculation);

        }
        else
        {
            selectedItem++;
            selectRadioItem(cellCalculation);
            selectedRadio.add(cellCalculation);
        }

        mainActivity.getMapWrapper().drawClusterOsmOnMap();
        mAdapter.notifyDataSetChanged();

    }

    public void selectRadioItem( CellCalculation cellCalculation )
    {
        if( selectedItem == 1 )
        {
            //its not clear marker yet in osm
            mainActivity.getMapWrapper().clearMap();
        }

        // Step 2
        //clear cluster item to handle select radio item in osm
        if( cellCalculation.getCenterBound() != null )
        {
            mainActivity.getMapWrapper().zoomToBound(cellCalculation.getCenterBound());
        }

        mainActivity.getMapWrapper().clearClusterOsmOnMap();

        // Step 3
        if( cellCalculation.getCurrentScannedType() == Config.SCANNED_TYPE_CELL )
        {
            mainActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedCell(), Config.SCANNED_TYPE_CELL);
        }
        else if( cellCalculation.getCurrentScannedType() == Config.SCANNED_TYPE_WIFI )
        {
            mainActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedWifi(), Config.SCANNED_TYPE_WIFI);
        }

        // Step 4
        mainActivity.getMapWrapper().drawLocationBasedOnSelectedProfile(cellCalculation, profileSingleton.getSelectedLocation());
    }

    /**
     * Step 1: Remove the drawing generated from Step 4 of selectRadioItem function
     * Step 2: Remove the drawing generated from Step 3 of selectRadioItem function
     * Step 3: If no Radio Item is selected, draw the overview of this session based on 'Overview Location' sub profile
     * Step 4: Draw the radio details of all the radio captured in this session based on 'Overview Cell or Wifi' sub profile
     */
    public void unselectRadioItem( CellCalculation cellCalculation )
    {
        // Step 1
        mainActivity.getMapWrapper().removeLocationBasedOnSelectedProfile(cellCalculation);

        // Step 2
        if( cellCalculation.getCurrentScannedType() == Config.SCANNED_TYPE_CELL )
        {
            mainActivity.getMapWrapper().removeRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedCell());

        }
        else if( cellCalculation.getCurrentScannedType() == Config.SCANNED_TYPE_WIFI )
        {
            mainActivity.getMapWrapper().removeRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedWifi());
        }


        if( selectedItem == 0 )
        {
            selectedCell = session.getCellList().size();
            selectedWifi = session.getWifiList().size();

            // Step 3
            redrawOnClearSelected();

            // Step 4
            for( ListviewItem listviewItem : mData )
            {
                ItemCellCalculation itemCellCalc = (ItemCellCalculation) listviewItem;
                CellCalculation cellCalc = itemCellCalc.getCellCalculation();
                if( cellCalc.getCurrentScannedType() == Config.SCANNED_TYPE_CELL && selectedCell < profileSingleton.getOverviewCell().getRadioNumber().getAmountRadio() )
                {
                    mainActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalc, profileSingleton.getOverviewCell(), Config.SCANNED_TYPE_CELL);
                    selectedCell++;
                }
                else if( cellCalc.getCurrentScannedType() == Config.SCANNED_TYPE_WIFI && selectedWifi < profileSingleton.getOverviewWifi().getRadioNumber().getAmountRadio() )
                {
                    mainActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalc, profileSingleton.getOverviewWifi(), Config.SCANNED_TYPE_WIFI);
                    selectedWifi++;
                }

            }
        }

    }

    @Override
    public void onStop()
    {
        // There was a 'googlemap.clear() command here
        // was removed in respond to fix issue-8370-CellTrax_task_scan_result_missing
        super.onStop();
    }

    public int getSelectedItem()
    {
        return selectedItem;
    }


    private class CellAsyncTask extends AsyncTask<Cell, CellCalculation, Void>
    {
        @Override
        protected Void doInBackground( Cell... cell )
        {
            CellCalculation cellCalculation = new CellCalculation(Config.SCANNED_TYPE_CELL);
            cellCalculation = mainActivity.getCellTable().getByCellRefAndSessionId(cellCalculation, session.getId(), cell[0].getCellReference());

            cellCalculation.calculateCenter();
            publishProgress(cellCalculation);
            return null;
        }

        @Override
        protected synchronized void onProgressUpdate( CellCalculation... cellCalculations )
        {
            ItemCellCalculation item = new ItemCellCalculation(cellCalculations[0]);
            item.onSelected = false;

            new CellLocationAsyncTask().executeOnExecutor(THREAD_POOL_EXECUTOR, cellCalculations[0].getCellList());

            // Draw the cells
            if( !profileSingleton.getOverviewCell().getRadioNumber().isShowing() || selectedCell < profileSingleton.getOverviewCell().getRadioNumber().getAmountRadio() )
            {
                mainActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculations[0], profileSingleton.getOverviewCell(), Config.SCANNED_TYPE_CELL);
            }


            if( !selectionOnClear )
            {
                selectedCell++;
            }

            if( addCellToRadioList )
            {
                mData.add(item);
                mAdapter.notifyDataSetChanged();

                if( selectedCell == session.getCellList().size() )
                {
                    addCellToRadioList = false;
                    if( mSwipe.isRefreshing() )
                    {
                        mSwipe.setRefreshing(false);
                    }
                }
                else
                {
                    addCellToRadioList = true;
                }

                if( mainActivity.getLeftPaneController().getTextEmptyList().getVisibility() == View.VISIBLE )
                {
                    mainActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        protected void onPostExecute( Void params )
        {
            if( mData.size() < 1 )
            {
                mainActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.VISIBLE);
                mainActivity.getLeftPaneController().getTextEmptyList().setText("There is no scan result");
            }
            else
            {
                if( MapSingleton.getInstance().getSelectedMap() == Config.OPEN_STREET_MAP && selectedCell == session.getCellList().size() )
                {
                    mainActivity.getMapWrapper().drawClusterOsmOnMap();
                }
            }
        }
    }

    private class CellLocationAsyncTask extends AsyncTask<ArrayList<Cell>, Cell, Void>
    {
        @Override
        protected Void doInBackground( ArrayList<Cell>... cells )
        {
            for( Cell cell : cells[0] )
            {
                publishProgress(cell);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate( Cell... cell )
        {
            drawLocationOnMapAsync(cell[0].getMeasuredLocation());

        }

        @Override
        protected void onPostExecute( Void params )
        {
            if( selectionOnClear )
            {
                selectionOnClear = false;
            }
        }
    }

    public void onRefreshScannedResults()
    {
        ArrayList<Cell> cellListRefresh = mainActivity.getCellTable().getUniqueCellBySessionIdLimited(session.getId(), session.getCellList().size());

        if( cellListRefresh.size() != 0 )
        {
            addCellToRadioList = true;
            for( Cell cellAdded : cellListRefresh )
            {
                session.getCellList().add(cellAdded);
                new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cellAdded);
            }
        }
    }

    public void redrawOnClearSelected()
    {
        selectionOnClear = true;

        if( session.getCellList().size() != 0 )
        {
            for( Cell cell : session.getCellList() )
            {
                new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cell);
            }
        }
        
    }

    public void drawLocationOnMapAsync( MeasuredLocation measuredLocation )
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        PathOverlay pathOverlay = new PathOverlay(Color.BLACK, mainActivity);
        LatLng point;

        point = new LatLng(measuredLocation.getLatitude(), measuredLocation.getLongitude());
        builder.include(point);
        polylineOptions.add(point);
        pathOverlay.addPoint(new GeoPoint(measuredLocation.getLatitude(), measuredLocation.getLongitude()));

        if( !isPointIncluded )
        {
            isPointIncluded = true;
        }

        if( profileSingleton.getOverviewLocation().getLineOfRoute().isShowing() )
        {
            pathOverlay.setColor(profileSingleton.getOverviewLocation().getLineOfRoute().getColor());
            polylineOptions.color(profileSingleton.getOverviewLocation().getLineOfRoute().getColor());
            mainActivity.getMapWrapper().addLocationPolyline(polylineOptions, pathOverlay);
        }

        if( isPointIncluded )
        {
            bounds = builder.build();
            mainActivity.getMapWrapper().zoomToBound(bounds);
        }

        locationHashMap.put(measuredLocation.getId(), measuredLocation);
        mainActivity.getMapWrapper().drawLocationBasedOnProfile(mainActivity, measuredLocation, profileSingleton.getOverviewLocation());
        
    }

    public SparseArray<MeasuredLocation> getLocationHashMap()
    {
        return locationHashMap;
    }

    public void resetLocationSelected()
    {
        this.selectionOnClear = false;
    }

    public LatLngBounds.Builder getBuilder()
    {
        return builder;
    }

    public void setBuilder( LatLngBounds.Builder builder )
    {
        this.builder = builder;
    }

}
