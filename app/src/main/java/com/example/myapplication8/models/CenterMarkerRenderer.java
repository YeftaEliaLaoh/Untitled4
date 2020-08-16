package com.example.myapplication8.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;

import com.example.myapplication8.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

public class CenterMarkerRenderer extends DefaultClusterRenderer<ClusteredCenterMarker>
{
    private Context context;

    private final float density = 4.0f;

    public CenterMarkerRenderer(Context context, GoogleMap googleMap, ClusterManager<ClusteredCenterMarker> clusterManager)
    {
        super(context, googleMap, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusteredCenterMarker clusteredCenterMarker, MarkerOptions markerOptions)
    {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bts_scan);
        markerOptions.icon(bitmapDescriptor).title(clusteredCenterMarker.getName());
    }

    @Override
    protected void onClusterItemRendered(ClusteredCenterMarker clusteredCenterMarker, Marker marker)
    {
        super.onClusterItemRendered(clusteredCenterMarker, marker);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusteredCenterMarker> cluster, MarkerOptions markerOptions)
    {
        final IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setContentView(this.makeSquareTextView(context));
        iconGenerator.setTextAppearance(com.google.maps.android.R.style.amu_ClusterIcon_TextAppearance);
        Bitmap bitmap = iconGenerator.makeIcon(Integer.toString(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster)
    {
        return cluster.getSize() > 1;
    }

    private SquareTextView makeSquareTextView(Context context)
    {
        int twelveDpi = (int) (12.0F * this.density);
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(com.google.maps.android.R.id.amu_text);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }
}