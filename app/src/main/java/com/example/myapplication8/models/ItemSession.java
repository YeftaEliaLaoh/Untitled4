package com.example.myapplication8.models;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication8.R;
import com.example.myapplication8.utilities.Utility;


public class ItemSession extends ListviewItem
{
    private Session session;
    private ViewHolder holder;

    public ItemSession( Session session )
    {
        this.session = session;
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.item_session;
    }

    @Override
    protected Object createViewHolder( View v )
    {

        holder = new ViewHolder();

        holder.textDateTime = v.findViewById(R.id.text_datetime);
        holder.textTotalCell = v.findViewById(R.id.text_total_cell_tower);
        holder.parent = v.findViewById(R.id.layout_parent);

        return holder;
    }

    @Override
    protected void populateViewHolder( Object viewHolder )
    {
        holder = (ViewHolder) viewHolder;
        holder.textDateTime.setText(Utility.milisToDateFormat(session.getDateTime(), "dd/MM/yyyy kk:mm:ss"));
        holder.textTotalCell.setText(String.valueOf(session.getCellCount()));
    }

    @Override
    public void execute( Context context, ListviewAdapter adapter )
    {

    }

    private static class ViewHolder
    {
        TextView textDateTime;
        TextView textTotalCell;
        View parent;

    }

    public Session getSession()
    {
        return session;
    }

    /**
     * Prepare to update cell count
     *
     * @param cellAmount the value was saved in Global.TAG_SESSION_CELL
     */
    public void addTotalCell( int cellAmount )
    {
        if( null == holder || cellAmount == 0 )
        {
            return;
        }
        holder.textTotalCell.setText(String.valueOf(cellAmount));
        session.setCellCount(cellAmount);

    }
}
