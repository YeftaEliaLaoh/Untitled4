package com.example.myapplication8.models;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication8.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class ItemCellCalculation extends ListviewItem
{
    private CellCalculation cellCalculation;


    public ItemCellCalculation( CellCalculation cellCalculation )
    {
        this.cellCalculation = cellCalculation;
    }

    public CellCalculation getCellCalculation()
    {
        return this.cellCalculation;
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.item_cell;
    }

    @Override
    protected Object createViewHolder( View v )
    {
        ViewHolder holder = new ViewHolder();
        holder.textDateTime = v.findViewById(R.id.text_datetime);
        holder.imageView = v.findViewById(R.id.image_cell);
        holder.textCellRef = v.findViewById(R.id.text_cellref);
        holder.textCoordinat = v.findViewById(R.id.text_coordinat);
        holder.parent = v.findViewById(R.id.layout_parent_item_cell);
        return holder;
    }

    @Override
    protected void populateViewHolder( Object viewHolder )
    {
        ViewHolder holder = (ViewHolder) viewHolder;

        LatLng center = cellCalculation.getCenter();

        holder.textCellRef.setText(cellCalculation.getCellReference());
        holder.imageView.setImageResource(R.drawable.bts_scan);

        if( onSelected )
        {
            holder.parent.setBackgroundColor(Color.parseColor("#e0ffff"));
        }
        else
        {
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
            {
                holder.parent.setBackgroundColor(holder.parent.getResources().getColor(android.R.color.transparent, null));
            }
        }

        if( center != null )
        {
            holder.textCoordinat.setText("(" + String.format(Locale.getDefault(), "%.4f", center.latitude) + ", " + String.format(Locale.getDefault(), "%.4f", center.longitude) + ")");
        }

        holder.textDateTime.setText(cellCalculation.getDateTime());

    }

    @Override
    public void execute( Context context, ListviewAdapter adapter )
    {

    }

    private static class ViewHolder
    {
        public TextView textDateTime;
        public TextView textCellRef;
        public ImageView imageView;
        public TextView textCoordinat;
        public TextView textQuality;
        public RelativeLayout parent;
    }
}
