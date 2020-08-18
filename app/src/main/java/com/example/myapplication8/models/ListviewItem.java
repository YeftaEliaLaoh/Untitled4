package com.example.myapplication8.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class ListviewItem
{

    public static int typeCount = 0;
    public boolean onSelected = false;
    public static ArrayList<Class> typeClasses = new ArrayList<>();
    private ArrayList<ListviewItem> childs = new ArrayList<>();

    private int position;


    public void addChild( ListviewItem item )
    {
        childs.add(item);
    }

    public static void addTypeCount( Class<?> kelas )
    {
        if( !typeClasses.contains(kelas) )
        {
            typeCount++;
            typeClasses.add(kelas);
        }
    }

    protected abstract int getLayoutId();

    protected abstract Object createViewHolder( View v );

    protected abstract void populateViewHolder( Object viewHolder );

    public abstract void execute( Context context, ListviewAdapter adapter );

    public View getView( Context context, View convertView, ViewGroup parent )
    {
        if( convertView == null )
        {// ||
            // !isViewHolderOwner(convertView.getTag()))
            // {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // int lid = getLayoutId();
            convertView = li.inflate(getLayoutId(), parent, false);

            convertView.setTag(createViewHolder(convertView));
        }
        populateViewHolder(convertView.getTag());

        return convertView;
    }

    public void setPosition( int position )
    {
        this.position = position;
    }

    public int getPosition()
    {
        return position;
    }


}
