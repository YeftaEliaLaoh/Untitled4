/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication8.utilities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.animation.Animation;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;

/**
 * Private class created to work around issues with AnimationListeners being
 * called before the animation is actually complete and support shadows on older
 * platforms.
 *
 * @hide
 */
class CircleImageView extends AppCompatImageView
{

    private static final int SHADOW_ELEVATION = 4;

    private Animation.AnimationListener mListener;

    public CircleImageView( Context context, int color )
    {
        super(context);
        final float density = getContext().getResources().getDisplayMetrics().density;

        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        ViewCompat.setElevation(this, SHADOW_ELEVATION * density);

        circle.getPaint().setColor(color);
        setBackgroundDrawable(circle);
    }


    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setAnimationListener( Animation.AnimationListener listener )
    {
        mListener = listener;
    }

    @Override
    public void onAnimationStart()
    {
        super.onAnimationStart();
        if( mListener != null )
        {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd()
    {
        super.onAnimationEnd();
        if( mListener != null )
        {
            mListener.onAnimationEnd(getAnimation());
        }
    }

    /**
     * Update the background color of the circle image view.
     */
    public void setBackgroundColor( int colorRes )
    {
        if( getBackground() instanceof ShapeDrawable )
        {
            ((ShapeDrawable) getBackground()).getPaint().setColor(Color.parseColor(String.valueOf(colorRes)));
        }
    }
}
