/*
 * Copyright (C) 2013 The Android Open Source Project
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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * The SwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. The SwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * the listener is responsible for correctly determining when to actually
 * initiate a refresh of its content. If the listener determines there should
 * not be a refresh, it must call setRefreshing(false) to cancel any visual
 * indication of a refresh. If an activity wishes to show just the progress
 * animation, it should call setRefreshing(true). To disable the gesture and
 * progress animation, call setEnabled(false) on the view.
 * <p>
 * This layout should be made the parent of the view that will be refreshed as a
 * result of the gesture and can only support one direct child. This view will
 * also be made the target of the gesture and will be forced to match both the
 * width and the height supplied in this layout. The SwipeRefreshLayout does not
 * provide accessibility events; instead, a menu item must be provided to allow
 * refresh of the content wherever this gesture is used.
 * </p>
 */
public class SwipeRefreshLayoutBottom extends ViewGroup
{
    // Maps to ProgressBar.Large style
    public static final int LARGE = MaterialProgressDrawable.LARGE;
    // Maps to ProgressBar default style
    public static final int DEFAULT = MaterialProgressDrawable.DEFAULT;

    private static final String LOG_TAG = SwipeRefreshLayoutBottom.class.getSimpleName();

    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);

    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 56;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;

    private static final int SCALE_DOWN_DURATION = 150;

    private static final int ALPHA_ANIMATION_DURATION = 300;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 200;

    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;

    private View mTarget; // the target of the gesture
    private IOnRefreshListener mListener;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private float mTotalDragDistance = -1;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;
    // Whether or not the starting offset has been determined.
    private boolean mOriginalOffsetCalculated = false;

    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    // Whether this item is scaled up rather than clipped
    private boolean mScale;

    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.enabled};

    private CircleImageView mCircleView;
    private int mCircleViewIndex = -1;

    private int mFrom;

    private float mStartingScale;

    private int mOriginalOffsetTop;

    private MaterialProgressDrawable mProgress;

    private Animation mAlphaStartAnimation;

    private Animation mAlphaMaxAnimation;

    private float mSpinnerFinalOffset;

    private boolean mNotify;

    private int mCircleWidth;

    private int mCircleHeight;

    // Whether the client has set a custom starting position;
    private boolean mUsingCustomStart;

    private AnimationListener mRefreshListener = new AnimationListener()
    {
        @Override
        public void onAnimationStart( Animation animation )
        {
        }

        @Override
        public void onAnimationRepeat( Animation animation )
        {
        }

        @Override
        public void onAnimationEnd( Animation animation )
        {
            if( mRefreshing )
            {
                // Make sure the progress view is fully visible
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
                if( mNotify )
                {
                    if( mListener != null )
                    {
                        mListener.onRefresh();
                    }
                }
            }
            else
            {
                mProgress.stop();
                mCircleView.setVisibility(View.GONE);
                setColorViewAlpha(MAX_ALPHA);
                // Return the circle to its start position
                if( mScale )
                {
                    setAnimationProgress(0 /* animation complete and view is hidden */);
                }
                else
                {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop, true /* requires update */);
                }
            }
            mCurrentTargetOffsetTop = mCircleView.getTop();
        }
    };

    private void setColorViewAlpha( int targetAlpha )
    {
        mCircleView.getBackground().setAlpha(targetAlpha);
        mProgress.setAlpha(targetAlpha);
    }

    /**
     * One of DEFAULT, or LARGE.
     */
    public void setSize( int size )
    {
        if( size != MaterialProgressDrawable.LARGE && size != MaterialProgressDrawable.DEFAULT )
        {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if( size == MaterialProgressDrawable.LARGE )
        {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        }
        else
        {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        mCircleView.setImageDrawable(null);
        mProgress.updateSizes(size);
        mCircleView.setImageDrawable(mProgress);
    }

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public SwipeRefreshLayoutBottom( Context context )
    {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public SwipeRefreshLayoutBottom( Context context, AttributeSet attrs )
    {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);

        createProgressView();
        setChildrenDrawingOrderEnabled(true);
        // the absolute offset has to take into account that the circle starts at an offset
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
        mTotalDragDistance = mSpinnerFinalOffset;
    }

    protected int getChildDrawingOrder( int childCount, int i )
    {
        if( mCircleViewIndex < 0 )
        {
            return i;
        }
        else if( i == childCount - 1 )
        {
            // Draw the selected child last
            return mCircleViewIndex;
        }
        else if( i >= mCircleViewIndex )
        {
            // Move the children after the selected child earlier one
            return i + 1;
        }
        else
        {
            // Keep the children before the selected child the same
            return i;
        }
    }

    private void createProgressView()
    {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener( IOnRefreshListener listener )
    {
        mListener = listener;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing( boolean refreshing )
    {
        if( refreshing && mRefreshing != refreshing )
        {
            // scale and show
            mRefreshing = refreshing;
            int endTarget = 0;
            if( !mUsingCustomStart )
            {
                endTarget = (int) (mSpinnerFinalOffset + mOriginalOffsetTop);
            }
            else
            {
                endTarget = (int) mSpinnerFinalOffset;
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop, true /* requires update */);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        }
        else
        {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void startScaleUpAnimation( AnimationListener listener )
    {
        mCircleView.setVisibility(View.VISIBLE);

        mProgress.setAlpha(MAX_ALPHA);

        Animation mScaleAnimation = new Animation()
        {
            @Override
            public void applyTransformation( float interpolatedTime, Transformation t )
            {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if( listener != null )
        {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleAnimation);
    }

    /**
     * Pre API 11, this does an alpha animation.
     *
     * @param progress
     */
    private void setAnimationProgress( float progress )
    {
        mCircleView.setScaleX(progress);
        mCircleView.setScaleY(progress);
    }

    private void setRefreshing( boolean refreshing, final boolean notify )
    {
        if( mRefreshing != refreshing )
        {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if( mRefreshing )
            {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            }
            else
            {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private void startScaleDownAnimation( AnimationListener listener )
    {
        Animation mScaleDownAnimation = new Animation()
        {
            @Override
            public void applyTransformation( float interpolatedTime, Transformation t )
            {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation()
    {
        mAlphaStartAnimation = startAlphaAnimation(mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
    }

    private void startProgressAlphaMaxAnimation()
    {
        mAlphaMaxAnimation = startAlphaAnimation(mProgress.getAlpha(), MAX_ALPHA);
    }

    private Animation startAlphaAnimation( final int startingAlpha, final int endingAlpha )
    {
        Animation alpha = new Animation()
        {
            @Override
            public void applyTransformation( float interpolatedTime, Transformation t )
            {
                mProgress.setAlpha((int) (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)));
            }
        };
        alpha.setDuration(ALPHA_ANIMATION_DURATION);
        // Clear out the previous animation listeners.
        mCircleView.setAnimationListener(null);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(alpha);
        return alpha;
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    public void setColorSchemeColors( int... colors )
    {
        ensureTarget();
        mProgress.setColorSchemeColors(colors);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing()
    {
        return mRefreshing;
    }

    private void ensureTarget()
    {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if( mTarget == null )
        {
            for( int i = 0; i < getChildCount(); i++ )
            {
                View child = getChildAt(i);
                if( !child.equals(mCircleView) )
                {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    /**
     * Set the distance to trigger a sync in dips
     *
     * @param distance
     */
    public void setDistanceToTriggerSync( int distance )
    {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if( getChildCount() == 0 )
        {
            return;
        }
        if( mTarget == null )
        {
            ensureTarget();
        }
        if( mTarget == null )
        {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop, (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if( mTarget == null )
        {
            ensureTarget();
        }
        if( mTarget == null )
        {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));
        if( !mUsingCustomStart && !mOriginalOffsetCalculated )
        {
            mOriginalOffsetCalculated = true;
            mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight() - mCircleView.getMeasuredHeight();  // TODO
        }
        mCircleViewIndex = -1;
        // Get the index of the circleview.
        for( int index = 0; index < getChildCount(); index++ )
        {
            if( getChildAt(index) == mCircleView )
            {
                mCircleViewIndex = index;
                break;
            }
        }
    }

    public boolean canChildScrollDown()
    {
        return mTarget.canScrollVertically(1);
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev )
    {
        ensureTarget();

        final int action = ev.getActionMasked();

        if( mReturningToStart && action == MotionEvent.ACTION_DOWN )
        {
            mReturningToStart = false;
        }

        if( !isEnabled() || mReturningToStart || canChildScrollDown() || mRefreshing )
        {  // TODO
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch ( action )
        {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if( initialMotionY == -1 )
                {
                    return false;
                }
                mInitialMotionY = initialMotionY;

            case MotionEvent.ACTION_MOVE:
                if( mActivePointerId == INVALID_POINTER )
                {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if( y == -1 )
                {
                    return false;
                }
                //final float yDiff = y - mInitialMotionY;
                final float yDiff = mInitialMotionY - y;   // TODO
                if( yDiff > mTouchSlop && !mIsBeingDragged )
                {
                    mIsBeingDragged = true;
                    mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY( MotionEvent ev, int activePointerId )
    {
        final int index = ev.findPointerIndex(activePointerId);
        if( index < 0 )
        {
            return -1;
        }
        return ev.getY(index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent( boolean b )
    {
        // Nope.
    }

    private boolean isAnimationRunning( Animation animation )
    {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    @Override
    public boolean onTouchEvent( MotionEvent motionEvent )
    {
        final int action = motionEvent.getActionMasked();

        if( mReturningToStart && action == MotionEvent.ACTION_DOWN )
        {
            mReturningToStart = false;
        }

        if( !isEnabled() || mReturningToStart || canChildScrollDown() )
        {  // TODO
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch ( action )
        {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = motionEvent.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE:
            {
                final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                if( pointerIndex < 0 )
                {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = motionEvent.getY(pointerIndex);
                //final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                final float overscrollTop = (mInitialMotionY - y) * DRAG_RATE;  // TODO
                if( mIsBeingDragged )
                {
                    mProgress.showArrow(true);
                    float originalDragPercent = overscrollTop / mTotalDragDistance;
                    if( originalDragPercent < 0 )
                    {
                        return false;
                    }
                    float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                    float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
                    float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
                    float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset - mOriginalOffsetTop : mSpinnerFinalOffset;
                    float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent * 2;

                    // int targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                    int targetY = mOriginalOffsetTop - (int) ((slingshotDist * dragPercent) + extraMove);
                    // where 1.0f is a full circle
                    if( mCircleView.getVisibility() != View.VISIBLE )
                    {
                        mCircleView.setVisibility(View.VISIBLE);
                    }
                    if( !mScale )
                    {
                        mCircleView.setScaleX(1f);
                        mCircleView.setScaleY(1f);
                    }
                    if( overscrollTop < mTotalDragDistance )
                    {
                        if( mScale )
                        {
                            setAnimationProgress(overscrollTop / mTotalDragDistance);
                        }
                        if( mProgress.getAlpha() > STARTING_PROGRESS_ALPHA && !isAnimationRunning(mAlphaStartAnimation) )
                        {
                            // Animate the alpha
                            startProgressAlphaStartAnimation();
                        }
                        float strokeStart = adjustedPercent * .8f;
                        mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
                        mProgress.setArrowScale(Math.min(1f, adjustedPercent));
                    }
                    else
                    {
                        if( mProgress.getAlpha() < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation) )
                        {
                            // Animate the alpha
                            startProgressAlphaMaxAnimation();
                        }
                    }
                    float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
                    mProgress.setProgressRotation(rotation);
                    setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop, true /* requires update */);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int index = motionEvent.getActionIndex();
                mActivePointerId = motionEvent.getPointerId(index);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(motionEvent);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                if( mActivePointerId == INVALID_POINTER )
                {
                    if( action == MotionEvent.ACTION_UP )
                    {
                        Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                final float y = motionEvent.getY(pointerIndex);
                //final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                final float overscrollTop = (mInitialMotionY - y) * DRAG_RATE;   //TODO
                mIsBeingDragged = false;
                if( overscrollTop > mTotalDragDistance )
                {
                    setRefreshing(true, true /* notify */);
                }
                else
                {
                    // cancel refresh
                    mRefreshing = false;
                    mProgress.setStartEndTrim(0f, 0f);
                    AnimationListener listener = null;
                    if( !mScale )
                    {
                        listener = new AnimationListener()
                        {

                            @Override
                            public void onAnimationStart( Animation animation )
                            {
                            }

                            @Override
                            public void onAnimationEnd( Animation animation )
                            {
                                if( !mScale )
                                {
                                    startScaleDownAnimation(null);
                                }
                            }

                            @Override
                            public void onAnimationRepeat( Animation animation )
                            {
                            }

                        };
                    }
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
                    mProgress.showArrow(false);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    private void animateOffsetToCorrectPosition( int from, AnimationListener listener )
    {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if( listener != null )
        {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition( int from, AnimationListener listener )
    {
        if( mScale )
        {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener);
        }
        else
        {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if( listener != null )
            {
                mCircleView.setAnimationListener(listener);
            }
            mCircleView.clearAnimation();
            mCircleView.startAnimation(mAnimateToStartPosition);
        }
    }

    private final Animation mAnimateToCorrectPosition = new Animation()
    {
        @Override
        public void applyTransformation( float interpolatedTime, Transformation t )
        {
            int targetTop = 0;
            int endTarget = 0;
            if( !mUsingCustomStart )
            {
                endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset); // TODO
            }
            else
            {
                endTarget = (int) mSpinnerFinalOffset;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mCircleView.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
        }
    };

    private void moveToStart( float interpolatedTime )
    {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mCircleView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private final Animation mAnimateToStartPosition = new Animation()
    {
        @Override
        public void applyTransformation( float interpolatedTime, Transformation t )
        {
            moveToStart(interpolatedTime);
        }
    };

    private void startScaleDownReturnToStartAnimation( int from, AnimationListener listener )
    {
        mFrom = from;

        mStartingScale = mCircleView.getScaleX();

        Animation mScaleDownToStartAnimation = new Animation()
        {
            @Override
            public void applyTransformation( float interpolatedTime, Transformation t )
            {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        if( listener != null )
        {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownToStartAnimation);
    }

    private void setTargetOffsetTopAndBottom( int offset, boolean requiresUpdate )
    {
        mCircleView.bringToFront();
        mCircleView.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mCircleView.getTop();
    }

    private void onSecondaryPointerUp( MotionEvent motionEvent )
    {
        final int pointerIndex = motionEvent.getActionIndex();
        final int pointerId = motionEvent.getPointerId(pointerIndex);
        if( pointerId == mActivePointerId )
        {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = motionEvent.getPointerId(newPointerIndex);
        }
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface IOnRefreshListener
    {
        void onRefresh();
    }
}