package com.example.myapplication8.controllers;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication8.R;
import com.example.myapplication8.activities.MainActivity;


public class LeftPaneController
{

    private ImageButton mButtonHideList;
    private ImageButton mButtonExpandList;
    private LinearLayout mLayoutLeft;
    private RelativeLayout mLayoutCollapse;
    private MainActivity mainActivity;

    private TextView mTextHeaderTitle;
    private TextView mTextEmpty;
    private ImageView mButtonArrowBack;

    private RelativeLayout layout_header_collapse;

    public LeftPaneController(Context context)
    {
        this.mainActivity = (MainActivity) context;
        init();
        registerEvent();
    }

    private void init()
    {
        mButtonHideList = mainActivity.findViewById(R.id.image_arrow_left);
        mButtonExpandList = mainActivity.findViewById(R.id.image_expand);
        mLayoutLeft = mainActivity.findViewById(R.id.layout_left);
        mLayoutCollapse = mainActivity.findViewById(R.id.layout_collapse);
        layout_header_collapse = mainActivity.findViewById(R.id.layout_header_collapse);
        mTextHeaderTitle = mainActivity.findViewById(R.id.text_header_title_layout_main_left);
        mTextEmpty = mainActivity.findViewById(R.id.text_empty);
        mButtonArrowBack = mainActivity.findViewById(R.id.button_arrow_back);
        mButtonArrowBack.setVisibility(View.GONE);
    }

    private void registerEvent()
    {
        mButtonHideList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLayoutLeft.setVisibility(View.GONE);
                mLayoutCollapse.setVisibility(View.VISIBLE);
            }
        });

        mButtonExpandList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLayoutCollapse.setVisibility(View.GONE);
                mLayoutLeft.setVisibility(View.VISIBLE);
            }
        });

        layout_header_collapse.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLayoutCollapse.setVisibility(View.GONE);
                mLayoutLeft.setVisibility(View.VISIBLE);
            }
        });
        mTextHeaderTitle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mButtonArrowBack.getVisibility() == View.VISIBLE)
                {
                    (mainActivity).onBackPressed();
                }
                else
                {
                    mLayoutLeft.setVisibility(View.GONE);
                    mLayoutCollapse.setVisibility(View.VISIBLE);
                }
            }
        });
        mButtonArrowBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                (mainActivity).onBackPressed();
            }
        });
    }

    public TextView getTextHeaderTitle()
    {
        return mTextHeaderTitle;
    }

    public TextView getTextEmptyList()
    {
        return mTextEmpty;
    }

    public ImageView getButtonArrowBack()
    {
        return mButtonArrowBack;
    }

}