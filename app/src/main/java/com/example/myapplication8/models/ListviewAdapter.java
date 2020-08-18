package com.example.myapplication8.models;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import java.util.List;

public class ListviewAdapter extends ArrayAdapter<ListviewItem>
{

    private List<ListviewItem> items;
    private Context mContext;

    public ListviewAdapter( Context context, int textViewResourceId, List<ListviewItem> objects )
    {
        super(context, textViewResourceId, objects);
        mContext = context;
        items = objects;
    }

    @Override
    public int getViewTypeCount()
    {
        if( ListviewItem.typeCount < 1 )
            return 1;
        return ListviewItem.typeCount;
    }

    @Override
    public int getItemViewType( int position )
    {
        ListviewItem i = items.get(position);

        int j = 0;

        for( Class<?> kelas : ListviewItem.typeClasses )
        {
            if( kelas.isInstance(i) )
                break;
            j++;
        }

        return j;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        ListviewItem i = this.getItem(position);
        i.setPosition(position);
        return i.getView(mContext, convertView, parent);
    }

    public static OnItemClickListener createOnItemClickListener()
    {
        return new OnItemClickListener()
        {

            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                ListviewAdapter adapter = (ListviewAdapter) parent.getAdapter();
                ListviewItem item = adapter.items.get(position);
                item.execute(parent.getContext(), adapter);
            }
        };
    }

}
