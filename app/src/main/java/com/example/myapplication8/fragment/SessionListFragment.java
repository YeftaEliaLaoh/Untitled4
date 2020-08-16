package com.example.myapplication8.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.Fragment;

import com.example.myapplication8.models.Session;

import java.util.ArrayList;
import java.util.List;

public class SessionListFragment extends Fragment
{

    static
    {
        ListviewItem.addTypeCount(ItemSession.class);
    }

    public ListviewAdapter adapter;
    public View view;
    public ArrayList<ListviewItem> data = new ArrayList<>();
    public CelltraxDB celltraxDB;

    private LeftSessionController controller;

    private ArrayList<Session> sessionList;

    public SessionListFragment()
    {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {

        // Inflate the layout for this fragment
        if( view == null )
        {
            view = inflater.inflate(R.layout.layout_sesion_fragment, container, false);
            adapter = new ListviewAdapter(this.getActivity(), 0, data);
            sessionList = new ArrayList<>();
            init();
        }

        return view;
    }

    // initialize the left session controller object and register for events inside left session
    private void init()
    {
        controller = new LeftSessionController(this);
        controller.registerEvent();
    }

    public LeftSessionController getLeftSessionController()
    {
        return this.controller;
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach(context);
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        celltraxDB = new CelltraxDB(getActivity());
        celltraxDB.open(Global.MAIN_CONFIG);
    }

    public void addSession( Session session )
    {
        ItemSession item = new ItemSession(session);
        sessionList.add(session);
        data.add(item);
        adapter.notifyDataSetChanged();
    }

    // add new session to the top of the listview
    public void addNewSession( Session session )
    {
        ItemSession item = new ItemSession(session);
        int firstIndex = 0;

        if( controller.isScanning() )
        {
            firstIndex += 1;
        }

        controller.getMainActivity().hideEmptyText();

        sessionList.add(firstIndex, session);
        data.add(firstIndex, item);
        adapter.notifyDataSetChanged();
    }

    public boolean isExistingSessionCountUpdated( Session session )
    {

        int indexFound = findIndexBySessionId(session.getId());

        if( indexFound > -1 )
        {
            sessionList.get(indexFound).setCellCount(session.getCellCount());
            sessionList.get(indexFound).setWifiCount(session.getWifiCount());
            adapter.notifyDataSetChanged();

            return true;
        }

        return false;
    }

    public void deleteSessionByObject( long sessionId )
    {
        int index = findIndexBySessionId(sessionId);

        if( index != -1 )
        {
            sessionList.remove(index);
            data.remove(index);
            adapter.notifyDataSetChanged();
        }
    }
    //issue 8442

    private int findIndexBySessionId( long sessionId )
    {
        int index = 0;

        for( Session session : sessionList )
        {
            if( session.getId() == sessionId )
            {
                return index;
            }
            index++;
        }

        return -1;
    }

    // get the last session of the list, return null if the list is empty
    public ItemSession getLastSession()
    {
        if( data.size() <= 0 )
            return null;
        return (ItemSession) data.get(0);
    }

    // update the stop time of the current active session, the session list should not be empty
    public void updateActiveSessionStopTime( long stopTime )
    {
        if( sessionList.size() > 0 )
        {
            ItemSession item = (ItemSession) data.get(0);
            Session session = sessionList.get(0);
            session.setStopTime(stopTime);
            item.updateStopTime(stopTime);
        }

    }

    public void refreshList()
    {
        adapter.notifyDataSetChanged();
    }

    public List<Session> getSessionList()
    {
        return sessionList;
    }

    public ArrayList<ListviewItem> getData()
    {
        return data;
    }

}
