package com.example.myapplication8.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.myapplication8.R;
import com.example.myapplication8.activities.MainActivity;
import com.example.myapplication8.fragment.SessionListFragment;
import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.ItemSession;
import com.example.myapplication8.models.ListviewItem;
import com.example.myapplication8.models.MapSingleton;
import com.example.myapplication8.models.MeasuredLocation;
import com.example.myapplication8.models.Session;
import com.example.myapplication8.models.SessionSingleton;
import com.example.myapplication8.utilities.Config;
import com.example.myapplication8.utilities.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class LeftSessionController
{
    private ListView mListView;
    private CheckBox mCheckBox;
    private TextView mTextSelectedCount;
    private Button mButtonImport;
    private Button mButtonClearLog;
    private SessionListFragment fragment;
    private int selectedCount = 0;
    private MainActivity mainActivity;
    private Session currentSession;

    private ArrayList<Session> selectedSession;
    private List<Session> sessionList;
    //isLongPressedAllowed is long click item event flag
    private boolean isLongPressedAllowed;

    private boolean isAllowedSendingToServer;

    public LeftSessionController( SessionListFragment fragment )
    {
        mainActivity = (MainActivity) fragment.getActivity();
        this.fragment = fragment;
        this.isLongPressedAllowed = true;
        this.mCheckBox = fragment.view.findViewById(R.id.layout_left_checkbox);
        mainActivity = (MainActivity) fragment.getActivity();
        mListView = fragment.view.findViewById(R.id.listview);
        mButtonClearLog = fragment.view.findViewById(R.id.button_clear_log);
        mButtonImport = fragment.view.findViewById(R.id.button_import);

        mTextSelectedCount = fragment.view.findViewById(R.id.layout_left_counter);
        mTextSelectedCount.setVisibility(View.VISIBLE);
        selectedSession = new ArrayList<>();
        sessionList = fragment.getSessionList();

        updateCount(0);
        initActiveSession();
    }

    public void registerEvent()
    {
        mListView.setCacheColorHint(Color.WHITE);
        mListView.requestFocus(0);
        mListView.setAdapter(fragment.adapter);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
            {
                if( isChecked )
                {
                    selectedSession.clear();
                    selectAllOnScan();
                    //update flag when select all item
                    isLongPressedAllowed = false;
                    updateCount(selectedCount);
                }
                else
                {
                    deselectAll();
                    //update flag when deselect all item
                    isLongPressedAllowed = true;
                }
            }
        });
        mListView.setOnItemLongClickListener(new SessionListOnLongPressListener());

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                //when isTapAllowed is false, it will be direct to session result
                //when it's true,it will be select the item
                if( isLongPressedAllowed )
                {
                    selectItemToViewSession(parent, position);
                }
                else
                {
                    selectItemToHighlight(position);
                }
            }

        });
        mButtonClearLog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                onClearLogClicked();
            }
        });

        mButtonImport.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {

                importExportSendFile();
            }
        });

        mTextSelectedCount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if( mCheckBox.isChecked() )
                {
                    mCheckBox.setChecked(false);
                }
                else
                {
                    mCheckBox.setChecked(true);
                }
            }
        });
    }

    public void importExportSendFile()
    {

        FileChooser fileChooser = new FileChooser(mainActivity);

        fileChooser.setFileListener(new FileChooser.IFileSelectedListener()
        {
            @Override
            public void fileSelected( File file )
            {
                insertImportSession(file);
            }
        }).showDialog();

    }

    public Session getCurrentSession()
    {
        return currentSession;
    }

    // update the number of selected item
    private void updateCount( int count )
    {
        if( count > 0 )
        {
            mTextSelectedCount.setText("Selected \n(" + count + ")");
            mButtonImport.setVisibility(View.GONE);
            mButtonClearLog.setEnabled(true);
        }
        else
        {
            mTextSelectedCount.setText("Select All");

            mButtonImport.setVisibility(View.VISIBLE);
            mButtonClearLog.setEnabled(false);
        }
    }


    private class SessionListOnLongPressListener implements AdapterView.OnItemLongClickListener
    {

        @Override
        public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id )
        {
            if( isLongPressedAllowed )
            {
                selectItemToHighlight(position);
            }
            return true;
        }
    }

    private void selectItemToViewSession( AdapterView<?> parent, int position )
    {
        ListviewItem item = (ListviewItem) parent.getItemAtPosition(position);
        if( item instanceof ItemSession )
        {
            viewScanResultList(item);
        }
    }

    private void selectItemToHighlight( int position )
    {
        //counter when scanning/not state
        int countWhenNotScanning = mListView.getAdapter().getCount();
        int countWhenScanning = mListView.getAdapter().getCount() - 1;

        selectionNormalMode(position, countWhenNotScanning);

        updateCount(selectedCount);
        isLongPressedAllowed = selectedCount == 0;
        fragment.adapter.notifyDataSetChanged();
    }

    // get the number of selected item from the list
    private int getSelectionCount( int position )
    {
        int selectedCount = 0;

        ItemSession selectedItemSession = (ItemSession) fragment.data.get(position);

        if( position != -1 )
        {
            selectedItemSession.onSelected = !selectedItemSession.onSelected;
        }

        //issue 8442 remove and add selectedSession into sessionlist
        if( selectedItemSession.onSelected )
        {
            selectedSession.add(sessionList.get(position));
            selectedCount++;
        }
        else
        {
            selectedSession.remove(sessionList.get(position));
            selectedCount--;
        }
        //end issue
        return selectedCount;
    }

    private void selectAllOnScan()
    {
        selectedCount = 0;

        //issue 8442 to prevent when list session is empty
        if( mListView.getAdapter().getCount() == 1 )
        {
            showWarningDialog(mainActivity.getString(R.string.msg_title_mlistview), mainActivity.getString(R.string.warning_correct_session_list));
            mCheckBox.setChecked(false);
        }
        else
        {
            for( ListviewItem item : fragment.data )
            {
                if( item.getPosition() != 0 )
                {
                    ItemSession itemSession = (ItemSession) item;
                    itemSession.onSelected = true;
                    selectedSession.add(sessionList.get(item.getPosition()));
                    selectedCount++;
                }
            }
        }
        //end issue
        mButtonImport.setVisibility(View.GONE);

        fragment.adapter.notifyDataSetChanged();
    }
    //end issue

    // deselect all the item on the list by setting onselected false
    private void deselectAll()
    {
        for( ListviewItem item : fragment.data )
        {
            ItemSession itemSession = (ItemSession) item;
            itemSession.onSelected = false;

            selectedSession.remove(sessionList.get(item.getPosition()));
            selectedCount--;

        }

        mButtonImport.setEnabled(true);

        fragment.adapter.notifyDataSetChanged();
        updateCount(0);
    }


    public void addNewSession( Session session )
    {
        fragment.addNewSession(session);
    }

    public void updateActiveSessionStopTime( long stopTime )
    {
        fragment.updateActiveSessionStopTime(stopTime);
    }


    private void onClearLogClicked()
    {
        if( selectedCount <= 0 )
        {
            showWarningDialog(fragment.getActivity().getString(R.string.warning_title_active_session_selection), mainActivity.getString(R.string.warning_select_item));
        }
        else
        {
            //issue 8442 dialog box clearing log click event
            DialogInterface.OnClickListener dlc = new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    clearSelectedSession(which);
                    mCheckBox.setChecked(false);
                    updateCount(selectedCount);
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setMessage(mainActivity.getString(R.string.msg_alert_delete)).setPositiveButton(mainActivity.getString(R.string.msg_yes), dlc).setNegativeButton(R.string.msg_no, dlc).setTitle(R.string.title_clear).show();
            //end issue
        }
    }

    private void showWarningDialog( String title, String message )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(mainActivity.getString(R.string.button_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {

            }
        });
        builder.show();
    }

    private void clearSelectedSession( int which )
    {
        switch ( which )
        {
            case DialogInterface.BUTTON_POSITIVE:
                for( Session session : selectedSession )
                {
                    final long sessionID = session.getId();
                    Log.d("clear", "session id: " + sessionID);
                    mainActivity.getAppDatabase().sessionDao().deleteById(sessionID);
                    mainActivity.getAppDatabase().locationDao().deleteBySessionId(sessionID);
                    mainActivity.getAppDatabase().cellDao().deleteBySessionId(sessionID);
                    fragment.deleteSessionByObject(sessionID);
                    selectedCount--;
                }
                isLongPressedAllowed = true;
                selectedSession.clear();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }


    /**
     * Check the export id was existed or not
     * Skipped the session if the wifi and cell count is 0 or it was existed
     * Inserting import file in single session
     *
     * @param importedFile
     */
    private void insertImportSession( File importedFile )
    {

        DialogProgressController progressController = new DialogProgressController(mainActivity, R.string.label_importing, 1);
        progressController.showDialog();
        new ImportJSONAsync(progressController).execute(importedFile);

    }

    /**
     * Get selected count
     *
     * @return selectedCount
     */
    public int getSelectedCount()
    {
        return selectedCount;
    }

    /**
     * Clear selected list when it's not empty
     */
    public void clearSelectedList()
    {
        deselectAll();
        //update flag when deselect all item
        selectedCount = 0;
        mCheckBox.setChecked(false);
        isLongPressedAllowed = true;
    }

    /**
     * Used to view scan result list by tap current session
     *
     * @param item
     */
    private void viewScanResultList( ListviewItem item )
    {
        ItemSession itemSession = (ItemSession) item;
        FragmentManager fm = fragment.getFragmentManager();

        currentSession = itemSession.getSession();


        Bundle bundle = new Bundle();
        bundle.putParcelable("session", itemSession.getSession());

        MapSingleton.getInstance().setTouchStatus(true);

        // Change textview 'Session List' to 'Scan Result'
        ((TextView) fragment.getActivity().findViewById(R.id.text_header_title_layout_main_left)).setText(fragment.getActivity().getString(R.string.label_scan_result_title));

        //fm.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, scannedListFragment, Config.SCAN_FRAGMENT).commit();
        //((MainActivity) fragment.getActivity()).getLeftPaneController().getButtonArrowBack().setVisibility(View.VISIBLE);
    }

    /**
     * Used to set "select all" checkbox is checked or not in scanning mode
     * When using this mode, the first index will not be selected when
     * "Select All" checkbox is checked.
     * This mode will be activated when CellTrax is currently scanning.
     *
     * @param position
     * @param total
     */
    private void selectionScanMode( int position, int total )
    {
        //begin to count
        selectedCount += getSelectionCount(position);

        //check the checkbox if selected count is equal to total data when scanning
        if( selectedCount < total )
        {
            mCheckBox.setChecked(false);
        }
        else if( selectedCount == total )
        {
            mCheckBox.setChecked(true);
        }
        else
        {
            mCheckBox.setChecked(false);
        }
    }

    /**
     * Used to set "selected all" checkbox is checked or not when in normal mode
     * When using this mode, the first index will also be selected when
     * "Select All" checkbox is checked
     *
     * @param position
     * @param total
     */
    private void selectionNormalMode( int position, int total )
    {
        selectedCount += getSelectionCount(position);

        //check the checkbox  if selected count is equal to total data when not in scanning mode
        if( selectedCount < total )
        {
            mCheckBox.setChecked(false);
        }
        else if( selectedCount == total )
        {
            mCheckBox.setChecked(true);
        }
        else
        {
            mCheckBox.setChecked(false);
        }
    }

    /**
     * Initialize active session, set cellHashMap and wifiHashMap to prevent lost data when the app terminated
     */
    private void initActiveSession()
    {
        if( sessionList.size() > 0 )
        {
            //set cellHashMap and wifiHashMap
            long sessionId = sessionList.get(0).getId();
            SessionSingleton.getInstance().setCellHashMap(getExistingCellHashMap(sessionId));
        }
    }


    /**
     * Get Existing cellHashMap to prevent lost count cell when the app is terminated
     *
     * @param sessionId the active sessionId
     * @return
     */
    private HashMap getExistingCellHashMap( long sessionId )
    {
        HashMap cellHashMap = new HashMap();
        List<Cell> cellList = mainActivity.getAppDatabase().cellDao().getAllBySessionId(sessionId);
        for( Cell cell : cellList )
        {
            cellHashMap.put(cell.getCellReference(), Config.HASHMAP_DEFAULT_VALUE);
        }
        return cellHashMap;
    }


    public MainActivity getMainActivity()
    {
        return mainActivity;
    }

    public ArrayList<Session> getSelectedSession()
    {
        return selectedSession;
    }


    public boolean isAllowedSendingToServer()
    {
        return isAllowedSendingToServer;
    }

    public void setAllowedSendingToServer( boolean isAllowedSendingToServer )
    {
        this.isAllowedSendingToServer = isAllowedSendingToServer;
    }

    public class ImportJSONAsync extends AsyncTask<File, Session, Integer>
    {

        private DialogProgressController progressController;

        public ImportJSONAsync( DialogProgressController progressController )
        {

            this.progressController = progressController;

        }

        @Override
        protected Integer doInBackground( File... files )
        {

            File processedFile = files[0];
            int status = Config.ERROR_UNKNOWN;

            try
            {
                status = new JSONFileController(mainActivity).read(processedFile, this);
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }

            return status;
        }

        @Override
        protected void onProgressUpdate( Session[] newSession )
        {

            Session importedSession = newSession[0];

            boolean isSessionCountUpdated = fragment.isExistingSessionCountUpdated(importedSession);

            if( !isSessionCountUpdated )
            {
                fragment.addNewSession(importedSession);
            }

            progressController.updateProgressDialog();

        }

        @Override
        protected void onPostExecute( Integer status )
        {
            progressController.dismissProgress();

            switch ( status )
            {

                case Config.ERROR_UNKNOWN:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.warning_import_unknown));

                    break;
                case Config.ERROR_IMPORT_INVALID_FORMAT_FILE:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.unsupported_file_format));

                    break;
                case Config.ERROR_IMPORT_INVALID_CHECKSUM:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.warning_import_checksum_invalid));

                    break;
                case Config.ERROR_IMPORT_INSERTED_BEFORE:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.warning_import_skipped));

                    break;

                case Config.ERROR_IMPORT_INVALID_FORMAT_JSON:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.unsupported_json_format));

                    break;

                default:

                    showWarningDialog(mainActivity.getString(R.string.warning_title_import), mainActivity.getString(R.string.warning_import_success));

                    break;

            }

        }

        public void broadcastResults( Session session )
        {
            publishProgress(session);
        }

    }

}
