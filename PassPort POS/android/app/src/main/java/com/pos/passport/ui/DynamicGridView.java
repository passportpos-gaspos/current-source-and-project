package com.pos.passport.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.pos.passport.adapter.ItemButtonAdapter;
import com.pos.passport.model.ItemButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 11/5/15.
 */
public class DynamicGridView extends ViewGroup implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    private final int ANIMATION_DURATION = 150;
    private Context mContext;
    private ItemButtonAdapter mAdapter;
    protected int mColumnCount, mScroll = 0;
    protected float mLastDelta = 0;
    protected int mDragged = -1, mLastX = -1, mLastY = -1, mLastTarget = -1;
    protected boolean mEnabled = true, mTouching = false,  mRearrangeEnabled = true;
    protected int mChildWidth = 300, mChildHeight = 300;
    protected int mScreenWidth;
    protected boolean mIsChildCentered = false;
    protected boolean mEnableRearrange = false;
    protected int mSelectedPosition = -1;
    protected ArrayList<Integer> mNewPositions = new ArrayList<>();

    protected OnRearrangeListener mOnRearrangeListener;
    protected AdapterView.OnItemClickListener mOnItemClickListener;
    protected AdapterView.OnItemLongClickListener mOnItemLongClickListener;

    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshViewsFromAdapter();
        }

        @Override
        public void onInvalidated() {
            removeAllViews();
        }
    };

    public DynamicGridView(Context context) {
        this(context, null);
    }

    public DynamicGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicGridView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mContext = context;
        setChildrenDrawingOrderEnabled(true);
        setListeners();
        awakenScrollBars();
        setVerticalScrollBarEnabled(true);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener l) {
        this.mOnItemLongClickListener = l;
    }

    public void setOnRearrangeListener(OnRearrangeListener l) {
        this.mOnRearrangeListener = l;
    }

    protected void setListeners() {
        setOnTouchListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
        //setOnItemClickListener(this);
        //setOnItemLongClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mScreenWidth = r - l;
        mColumnCount = 0;

        int screenWidthAux = mScreenWidth - getPaddingRight() + getPaddingLeft();
        if (getChildCount() > 0) {
            mChildWidth = getChildAt(0).getMeasuredWidth();
            mChildHeight = getChildAt(0).getMeasuredHeight();
        }
        for (int i = 0; i < getChildCount(); i++) {
            if (screenWidthAux < mChildWidth) {
                break;
            }

            mColumnCount++;
            screenWidthAux -= mChildWidth;
        }

        for (int i = 0; i < getChildCount(); i++) {
            if (i != mDragged) {
                Point xy = getCoorFromIndex(i);
                getChildAt(i).layout(xy.x, xy.y, xy.x + mChildWidth, xy.y + mChildHeight);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!mEnabled || getChildCount() == 0) {
            return;
        }

        int index = getLastIndex();

        if (mOnItemClickListener != null && index != -1) {
            mOnItemClickListener.onItemClick(null, getChildAt(getLastIndex()), getLastIndex(), getLastIndex() / mColumnCount);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mEnabled || !mRearrangeEnabled || getChildCount() == 0)
            return false;


        int index = getLastIndex();
        if (index != -1) {
            ItemButtonView view = (ItemButtonView)getChildAt(index);
            if (view != null && !view.isDraggable())
                return false;
        }

        // Other functionality set with a new onItemClick listener
        boolean handled = false;
        if (mOnItemLongClickListener != null && index != -1) {
            handled = mOnItemLongClickListener.onItemLongClick(null, getChildAt(getLastIndex()), getLastIndex(), getLastIndex() / mColumnCount);
            if (handled)
                return true;
        }

        // Default behaviour: drag-drop
        if (index != -1) {
            mDragged = index;
            animateDragged();
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mEnabled = true;
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                mTouching = true;
                break;

            case MotionEvent.ACTION_MOVE:
                manageMoveEvent(event);
                break;

            case MotionEvent.ACTION_UP:
                manageUpEvent();
                break;
        }

        if (mDragged != -1) {
            return true;
        }

        return false;
    }

    protected void manageMoveEvent(MotionEvent event) {
        int delta = mLastY - (int) event.getY();
        invalidate();

        if (mDragged != -1) {
            //change draw location of dragged visual
            int x = (int) event.getX();
            int y = (int) event.getY();
            int l = x - (3 * mChildWidth / 4);
            int t = y - (3 * mChildHeight / 4);
            getChildAt(mDragged).layout(l, t, l + (mChildWidth * 3 / 2), t + (mChildHeight * 3 / 2));

            //check for new target hover
            int target = getTargetFromCoor(x, y);
            if (mLastTarget != target) {
                if (target != -1) {
                    animateGap(target);
                    mLastTarget = target;
                }
            }
        } else {
            mScroll += delta;
            clampScroll();
            if (Math.abs(delta) > 2) {
                mEnabled = false;
            }
            requestLayout();
        }

        mLastX = (int) event.getX();
        mLastY = (int) event.getY();
        mLastDelta = delta;
    }

    protected void manageUpEvent() {
        if (mDragged != -1) {
            View v = getChildAt(mDragged);

            if (mLastTarget != -1) {
                reorderChildren();
            } else {
                Point xy = getCoorFromIndex(mDragged);
                v.layout(xy.x, xy.y, xy.x + mChildWidth, xy.y + mChildHeight);
            }

            if (v instanceof ImageView) {
                ((ImageView) v).setImageAlpha(255);
            }

            mLastTarget = -1;
            mDragged = -1;
        }
        mTouching = false;
        cancelAnimations();
    }

    public int getLastIndex() {
        return getIndexFromCoor(mLastX, mLastY);
    }

    public int getIndexFromCoor(int x, int y) {
        int col = getColFromCoor(x);
        int row = getRowFromCoor(y + mScroll);

        if (col == -1 || row == -1) { //touch is between columns or rows
            return -1;
        }

        int index = row * mColumnCount + col;

        if (index >= getChildCount()) {
            return -1;
        }

        return index;
    }

    protected int getColFromCoor(int coor) {

        coor -= getPaddingLeft();

        if (coor > mChildWidth * mColumnCount)
            return -1;
        //int widthForHorizontalCentering = 0;

        // For centering the children if there isn't room for more children
        /*
        float emptySpaceInGrid = mScreenWidth - getPaddingLeft() - getPaddingRight()
                - (mChildWidth * mColumnCount);

        if (mIsChildCentered || emptySpaceInGrid < mChildWidth) {
            widthForHorizontalCentering = Math.round(emptySpaceInGrid / 2);
        }*/

        //coor -= widthForHorizontalCentering;

        for (int i = 0; coor > 0; i++) {
            if (coor < mChildWidth) {
                return i;
            }

            coor -= mChildWidth;
        }
        return -1;
    }

    protected int getRowFromCoor(int coor) {
        coor -= getPaddingTop();

        for (int i = 0; coor > 0; i++) {
            if (coor < mChildHeight) {
                return i;
            }

            coor -= mChildHeight;
        }
        return -1;
    }

    protected int getTargetFromCoor(int x, int y) {
        if (getRowFromCoor(y + mScroll) == -1) { //touch is between rows
            return -1;
        }

        int leftPos = getIndexFromCoor(x - (mChildWidth / 4), y);
        int rightPos = getIndexFromCoor(x + (mChildWidth / 4), y);

        if (leftPos == -1 && rightPos == -1) { //touch is in the middle of nowhere
            return -1;
        } else if (leftPos == rightPos) { //touch is in the middle of a visual
            return -1;
        }

        int target = -1;

        if (rightPos > -1) {
            target = rightPos;

        } else if (leftPos > -1) {
            target = leftPos + 1;
        }

        if (mDragged < target) {
            return target - 1;
        }

        //Toast.makeText(getContext(), "Target: " + target + ".", Toast.LENGTH_SHORT).show();
        return target;
    }

    protected Point getCoorFromIndex(int index) {
        int col = index % mColumnCount;
        int row = index / mColumnCount;
        int widthForHorizontalCentering = 0;

        // For centering the children if there isn't room for more children
        //float emptySpaceInGrid = mScreenWidth - getPaddingLeft() - getPaddingRight()- (mChildWidth * mColumnCount);

        //if (mIsChildCentered || emptySpaceInGrid < mChildWidth) {
            //widthForHorizontalCentering = Math.round(emptySpaceInGrid / 2);
        //}

        // Take care about the padding of each child and the padding of the grid view itself
        // You return the coordinates of the top left point of the child view
        return new Point(widthForHorizontalCentering + getPaddingLeft() + mChildWidth * col,
                getPaddingTop() + mChildHeight * row - mScroll);
    }

    protected void clampScroll() {
        int stretch = 3, overreach = getHeight() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);

        if (mScroll < -overreach) {
            mScroll = -overreach;
            mLastDelta = 0;

        } else if (mScroll > max + overreach) {
            mScroll = max + overreach;
            mLastDelta = 0;

        } else if (mScroll < 0) {
            if (mScroll >= -stretch) {
                mScroll = 0;
            } else if (!mTouching) {
                mScroll -= mScroll / stretch;
            }

        } else if (mScroll > max) {
            if (mScroll <= max + stretch) {
                mScroll = max;
            } else if (!mTouching) {
                mScroll += (max - mScroll) / stretch;
            }
        }
    }

    protected int getMaxScroll() {
        int rowCount = (int) Math.ceil((double) getChildCount() / mColumnCount);
        return rowCount * mChildHeight + getPaddingTop() + getPaddingBottom() - getHeight();
    }

    public ItemButtonAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(ItemButtonAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            this.mAdapter.registerDataSetObserver(mObserver);
        }
        initViewsFromAdapter();
    }

    protected void initViewsFromAdapter() {
        removeAllViews();
        removeAllViewsInLayout();
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                View view = mAdapter.getView(i, null, this);
                addView(view, i);
                mNewPositions.add(-1);
            }
        }

        requestLayout();
    }

    protected void refreshViewsFromAdapter() {
        int childCount = getChildCount();
        int adapterSize = mAdapter.getCount();
        int reuseCount = Math.min(childCount, adapterSize);

        removeAllViews();
        removeAllViewsInLayout();
        mNewPositions.clear();
        for (int i = 0; i < adapterSize; i++) {
            addView(mAdapter.getView(i, null, this));
            mNewPositions.add(-1);
        }
        /*
        //removeAllViews();
        for (int i = 0; i < reuseCount; i++) {
             ItemButtonView view = (ItemButtonView)getChildAt(i);
            //view.setTitle(mAdapter.get);
            mAdapter.getView(i, view, this);
        }

        if (childCount < adapterSize) {
            for (int i = childCount; i < adapterSize; i++) {
                View view = mAdapter.getView(i, null, this);
                addView(view, i);
                mNewPositions.set(i, -1);
            }
        } else if (childCount > adapterSize)             for (int i = childCount - 1; i >= adapterSize; i--) {
                removeViewAt(i);
                mNewPositions.remove(i);
            }
        }
        */

        requestLayout();
    }

    private int findEligibleLastTarget(List<View> children, int start, int end) {
        for (int i = start + 1; i <= end; i++) {
            ItemButtonView itemButtonView = (ItemButtonView)children.get(i);
            if (itemButtonView.isDraggable())
                return i;
        }

        return -1;
    }

    private int findEligibleLastTargetReverse(List<View> children, int start, int end) {
        for (int i = start; i >= end; i--) {
            ItemButtonView itemButtonView = (ItemButtonView)children.get(i);
            if (itemButtonView.isDraggable())
                return i;
        }

        return -1;
    }


    protected void reorderChildren() {
        //FIXME: FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND RECONSTRUCTING THE LIST!!!

        ArrayList<View> children = new ArrayList<>();
        List<ItemButton> buttons = mAdapter.getItemButtons();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).clearAnimation();
            children.add(getChildAt(i));
        }

        removeAllViews();
        if (mOnRearrangeListener != null) {
            mOnRearrangeListener.onRearrange(mDragged, mLastTarget);
        }
        if (!((ItemButtonView)children.get(mLastTarget)).isDraggable()) {
            if (mDragged < mLastTarget)
                mLastTarget = findEligibleLastTargetReverse(children, mLastTarget, mDragged);
            else
                mLastTarget = findEligibleLastTarget(children, mLastTarget, mDragged);;
        }

        while (mDragged != mLastTarget) {
            int newPos;
            if (mDragged < mLastTarget) { // shift to the right
                newPos = getNextSwappable(children, mDragged, mLastTarget);
                Collections.swap(children, mDragged, newPos);
                Collections.swap(buttons, mDragged, newPos);
                mDragged = newPos;

            } else if (mDragged > mLastTarget) { // shift to the left
                newPos = getPreviousSwappable(children, mDragged, mLastTarget);
                Collections.swap(children, mDragged, newPos);
                Collections.swap(buttons, mDragged, newPos);
                mDragged = newPos;
            }
        }

        for (int i = 0; i < children.size(); i++) {
            mNewPositions.set(i, -1);
            addView(children.get(i));
        }

        requestLayout();
    }

    private int findEmptySpot(List<View> children) {
        if (children == null)
            return -1;
        for (int i = 0; i < children.size(); i++) {
            View v = children.get(i);
            int index = getIndexFromCoor((v.getRight() - v.getLeft()) / 2, (v.getBottom() - v.getTop()) / 2);
            if (index == -1)
                return i;
        }
        return -1;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mDragged == -1) {
            return i;
        } else if (i == childCount - 1) {
            return mDragged;

        } else if (i >= mDragged) {
            return i + 1;
        }

        return i;
    }

    private Animation createTranslateAnimation(Point oldOffset, Point newOffset) {
        TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, oldOffset.x,
                Animation.ABSOLUTE, newOffset.x,
                Animation.ABSOLUTE, oldOffset.y,
                Animation.ABSOLUTE, newOffset.y);
        translate.setDuration(ANIMATION_DURATION);
        translate.setFillEnabled(true);
        translate.setFillAfter(true);
        translate.setInterpolator(new AccelerateDecelerateInterpolator());

        return translate;
    }

    private int getPreviousSwappable(List<View> children, int dragged, int lastTarget) {
        for (int i = dragged - 1; i >= lastTarget; i--) {
            ItemButtonView view = (ItemButtonView)children.get(i);
            if (view.isDraggable())
                return i;
        }
        return lastTarget;
    }

    private int getNextSwappable(List<View> children, int dragged, int lastTarget) {
        for (int i = dragged + 1; i <= lastTarget; i++) {
            ItemButtonView view = (ItemButtonView)children.get(i);
            if (view.isDraggable())
                return i;
        }
        return lastTarget;
    }

    private int getPreviousDraggable(int dragged, int curPos) {
        for (int i = curPos - 1; i >= dragged; i--) {
            ItemButtonView view = (ItemButtonView)getChildAt(i);
            if (view.isDraggable())
                return i;
        }
        return curPos;
    }

    private int getNextDraggable(int dragged, int curPos) {
        for (int i = curPos + 1; i <= dragged; i++) {
            ItemButtonView view = (ItemButtonView)getChildAt(i);
            if (view.isDraggable())
                return i;
        }
        return curPos;
    }

    protected void animateGap(int target) {
        for (int i = 0; i < getChildCount(); i++) {
            ItemButtonView v = (ItemButtonView)getChildAt(i);

            if (i == mDragged || !v.isDraggable()) {
                continue;
            }

            int newPos = i;
            if (mDragged < target && i >= mDragged + 1 && i <= target) {
                newPos = getPreviousDraggable(mDragged, i);
                //newPos--;
            } else if (target < mDragged && i >= target && i < mDragged) {
                newPos = getNextDraggable(mDragged, i);
                //newPos++;
            }

            int oldPos = i;
            if (mNewPositions.get(i) != -1) {
                oldPos = mNewPositions.get(i);
            }

            if (oldPos == newPos) {
                continue;
            }

            Point oldXY = getCoorFromIndex(oldPos);
            Point newXY = getCoorFromIndex(newPos);
            Point oldOffset = new Point(oldXY.x - v.getLeft(), oldXY.y - v.getTop());
            Point newOffset = new Point(newXY.x - v.getLeft(), newXY.y - v.getTop());

            AnimationSet animSet = new AnimationSet(true);
            animSet.addAnimation(createFastRotateAnimation());
            animSet.addAnimation(createTranslateAnimation(oldOffset, newOffset));

            v.clearAnimation();
            v.startAnimation(animSet);

            mNewPositions.set(i, newPos);
        }
    }

    protected void animateDragged() {
        View v = getChildAt(mDragged);
        int x = getCoorFromIndex(mDragged).x + mChildWidth / 2;
        int y = getCoorFromIndex(mDragged).y + mChildHeight / 2;
        int l = x - (3 * mChildWidth / 4);
        int t = y - (3 * mChildHeight / 4);
        v.layout(l, t, l + (mChildWidth * 3 / 2), t + (mChildHeight * 3 / 2));

        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(1, 1.1f, 1, 1.1f, mChildWidth * 0.5f, mChildHeight * 0.5f);
        scale.setDuration(ANIMATION_DURATION);
        AlphaAnimation alpha = new AlphaAnimation(1, .5f);
        alpha.setDuration(ANIMATION_DURATION);

        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);

        v.clearAnimation();
        v.startAnimation(animSet);
    }

    private Animation createFastRotateAnimation() {
        Animation rotate = new RotateAnimation(-3.0f,
                3.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        rotate.setRepeatMode(Animation.REVERSE);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(100);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

        return rotate;
    }

    private void cancelAnimations() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.clearAnimation();
        }
    }

    public void setRearrangeEnabled(boolean enabled) {
        mRearrangeEnabled = enabled;
    }

    public int getPositionForView(View view) {
        View listItem = view;
        try {
            View v;
            while (!(v = (View) listItem.getParent()).equals(this)) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list view
            return -1;
        }

        // Search the children for the list item
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(listItem)) {
                return  i;
            }
        }

        // Child not found!
        return -1;
    }
}
