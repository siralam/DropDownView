package com.asksira.dropdownview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DropDownView extends LinearLayout {

    private Context context;

    //Views
    private LinearLayout filterContainer;
    private TextView filterTextView;
    private ImageView filterArrow;
    private ScrollView dropDownContainer;
    private LinearLayout dropDownItemsContainer;
    private View backgroundDimView;

    //Configurable Attributes
    private float filterHeight;
    private float textSize;
    private int filterTextColor;
    private int filterBarBackgroundColor;
    private float arrowWidth;
    private float arrowHeight;
    private int arrowDrawableResId;
    private boolean isArrowRotate;
    private float dividerHeight;
    private int dividerColor;
    private float dropDownItemHeight;
    private float dropDownItemTextSize;
    private int dropDownItemTextColor;
    private int dropDownItemTextColorSelected;
    private int dropDownBackgroundColor;
    private boolean isExpandDimBackground;
    private boolean isExpandIncludeSelectedItem;
    private String placeholderText;
    private int typeface;
    private int animationDuration;

    //Runtime Attributes
    /**
     * Selecting positing can be -1 which indicates nothing is selected.
     * Placeholder text will be shown, if configured.
     * This View is designed to disallow selecting -1 without a placeholder text.
     */
    private int selectingPosition;
    private int state = 1;
    public static final int COLLAPSED = 1;
    public static final int EXPANDED = 2;
    private List<String> dropDownItemList;

    public interface OnSelectionListener {
        void onItemSelected (DropDownView view, int position);
    }
    private OnSelectionListener onSelectionListener;

    public DropDownView(Context context) {
        super(context);
        init(context);
    }

    public DropDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DropDownView, 0, 0);
        try {
            filterHeight = a.getDimension(R.styleable.DropDownView_filter_height, getResources().getDimension(R.dimen.filter_default_height));
            textSize = a.getDimension(R.styleable.DropDownView_filter_text_size, getResources().getDimension(R.dimen.filter_text_selected_default_size));
            filterTextColor = a.getResourceId(R.styleable.DropDownView_filter_text_color, R.color.dropdown_default_text_color);
            filterBarBackgroundColor = a.getResourceId(R.styleable.DropDownView_filter_bar_background_color, android.R.color.transparent);
            arrowWidth = a.getDimension(R.styleable.DropDownView_arrow_width, -1);
            arrowHeight = a.getDimension(R.styleable.DropDownView_arrow_height, -1);
            arrowDrawableResId = a.getResourceId(R.styleable.DropDownView_arrow_drawable, 0);
            isArrowRotate = a.getBoolean(R.styleable.DropDownView_arrow_rotate, true);
            dividerHeight = a.getDimension(R.styleable.DropDownView_divider_height, getResources().getDimension(R.dimen.filter_divider_default_height));
            dividerColor = a.getResourceId(R.styleable.DropDownView_divider_color, R.color.dropdown_default_divider_coloir);
            dropDownItemHeight = a.getDimension(R.styleable.DropDownView_dropDownItem_height, getResources().getDimension(R.dimen.filter_dropDownItem_default_height));
            dropDownItemTextSize = a.getDimension(R.styleable.DropDownView_dropDownItem_text_size, getResources().getDimension(R.dimen.filter_text_default_size));
            dropDownItemTextColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_text_color, R.color.dropdown_default_text_color);
            dropDownItemTextColorSelected = a.getResourceId(R.styleable.DropDownView_dropDownItem_text_color_selected, R.color.dropdown_default_text_color);
            dropDownBackgroundColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_background_color, android.R.color.white);
            isExpandDimBackground = a.getBoolean(R.styleable.DropDownView_expand_dim_background, true);
            isExpandIncludeSelectedItem = a.getBoolean(R.styleable.DropDownView_expand_include_selected_item, true);
            placeholderText = a.getString(R.styleable.DropDownView_placeholder_text);
            typeface = a.getResourceId(R.styleable.DropDownView_dropdown_typeface, 0);
            animationDuration = a.getInteger(R.styleable.DropDownView_dropdown_animation_duration, 300);
        } finally {
            a.recycle();
        }
        init(context);
    }

    public DropDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(VERTICAL);
        inflate(context, R.layout.widget_dropdownview, this);
        filterContainer = findViewById(R.id.filter_container);
        filterTextView = findViewById(R.id.filter_text);
        filterArrow = findViewById(R.id.filter_arrow);
        dropDownContainer = findViewById(R.id.sv_dropdown_container);
        dropDownItemsContainer = findViewById(R.id.ll_dropdown_items_container);
        backgroundDimView = findViewById(R.id.background_dim);

        //Configure filter bar
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) filterContainer.getLayoutParams();
        lp.height = (int)filterHeight;
        filterContainer.setLayoutParams(lp);
        filterContainer.setBackgroundColor(ContextCompat.getColor(context, filterBarBackgroundColor));
        filterContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        //Configure filter text
        if (typeface != 0) filterTextView.setTypeface(ResourcesCompat.getFont(context, typeface));
        filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        filterTextView.setTextColor(ContextCompat.getColor(context, filterTextColor));
        if (placeholderText != null && !placeholderText.isEmpty()) {
            filterTextView.setText(placeholderText);
            selectingPosition = -1;
        } else {
            selectingPosition = 0;
        }

        //Configure arrow
        if (arrowWidth > -1 || arrowHeight > -1) {
            LinearLayout.LayoutParams arrowLp = (LinearLayout.LayoutParams) filterArrow.getLayoutParams();
            if (arrowHeight > -1) arrowLp.height = (int) arrowHeight;
            if (arrowWidth > -1) arrowLp.width = (int) arrowWidth;
            filterArrow.setLayoutParams(arrowLp);
        }
        if (arrowDrawableResId != 0) {
            filterArrow.setImageResource(arrowDrawableResId);
        }

        //Configure background dim
        backgroundDimView.setBackgroundColor(ContextCompat.getColor(context,
                isExpandDimBackground ? R.color.dropdown_background_dim : android.R.color.transparent));
        backgroundDimView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse(true);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && state == EXPANDED) {
            collapse(true);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.state = this.state;
        ss.selectingPosition = this.selectingPosition;
        ss.dropDownItems = this.dropDownItemList;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.state = ss.state;
        this.selectingPosition = ss.selectingPosition;
        this.dropDownItemList = ss.dropDownItems;

        updateDropDownItems();
        if (selectingPosition >= 0) setSelectingPosition(this.selectingPosition);
        if (this.state == EXPANDED) {
            setFocusableInTouchMode(true);
            requestFocus();
            updateDropDownItems();
            filterArrow.setRotation(180);
            backgroundDimView.setVisibility(VISIBLE);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) dropDownContainer.getLayoutParams();
            lp.height = WRAP_CONTENT;
            dropDownContainer.setLayoutParams(lp);
        }
    }

    static class SavedState extends BaseSavedState {
        int state;
        int selectingPosition;
        List<String> dropDownItems;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.state = in.readInt();
            this.selectingPosition = in.readInt();
            in.readStringList(this.dropDownItems);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.state);
            out.writeInt(this.selectingPosition);
            out.writeStringList(this.dropDownItems);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public void toggle() {
        switch (state) {
            case COLLAPSED:
                expand(true);
                break;
            case EXPANDED:
                collapse(true);
                break;
            default:
                throw new IllegalStateException("This should not happen. If you see this, please submit an issue to Github");
        }
    }

    public void expand (boolean animate) {
        if (state == EXPANDED) return;
        setFocusableInTouchMode(true);
        requestFocus();
        updateDropDownItems();
        if (isArrowRotate) {
            if (animate) {
                filterArrow.setRotation(0);
                filterArrow.animate().rotationBy(-180).setDuration(animationDuration).start();
            } else {
                filterArrow.setRotation(-180);
            }
        }
        if (animate) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new Fade());
            transitionSet.setDuration(animationDuration);
            TransitionManager.beginDelayedTransition(this, transitionSet);
        }
        backgroundDimView.setVisibility(VISIBLE);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) dropDownContainer.getLayoutParams();
        lp.height = WRAP_CONTENT;
        dropDownContainer.setLayoutParams(lp);
        state = EXPANDED;
    }

    public void collapse (boolean animate) {
        if (state == COLLAPSED) return;
        if (isArrowRotate) {
            if (animate) {
                filterArrow.setRotation(180);
                filterArrow.animate().rotationBy(180).setDuration(animationDuration).start();
            } else {
                filterArrow.setRotation(0);
            }
        }
        if (animate) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new Fade());
            transitionSet.setDuration(animationDuration);
            transitionSet.excludeTarget(filterTextView, true);
            TransitionManager.beginDelayedTransition(this, transitionSet);
        }
        backgroundDimView.setVisibility(INVISIBLE);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) dropDownContainer.getLayoutParams();
        lp.height = 0;
        dropDownContainer.setLayoutParams(lp);
        state = COLLAPSED;
    }

    public void setDropDownListItem (List<String> items) {
        this.dropDownItemList = items;
        updateDropDownItems();
    }

    private void updateDropDownItems () {
        dropDownItemsContainer.removeAllViews();
        for (int i=0; i < dropDownItemList.size(); i++) {
            if (!isExpandIncludeSelectedItem) {
                if (i == selectingPosition) continue;
            }
            dropDownItemsContainer.addView(generateDropDownItem(dropDownItemList.get(i), i));
            dropDownItemsContainer.addView(generateDivider());
        }
    }

    private TextView generateDropDownItem (String itemName, final int index) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, (int)dropDownItemHeight);
        textView.setLayoutParams(lp);
        textView.setText(itemName);
        if (typeface != 0) textView.setTypeface(ResourcesCompat.getFont(context, typeface));
        textView.setBackgroundColor(ContextCompat.getColor(context, dropDownBackgroundColor));
        if (Build.VERSION.SDK_INT >= 23) {
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            textView.setForeground(getContext().getDrawable(typedValue.resourceId));
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dropDownItemTextSize);
        if (index == selectingPosition) {
            textView.setTextColor(ContextCompat.getColor(context, dropDownItemTextColorSelected));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, dropDownItemTextColor));
        }
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectingPosition(index);
            }
        });
        return textView;
    }

    private View generateDivider () {
        View view = new View(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                MATCH_PARENT, (int)dividerHeight);
        view.setLayoutParams(lp);
        view.setBackgroundColor(ContextCompat.getColor(context, dividerColor));
        return view;
    }


    /**
     * Below are views that opens get method to users.
     */
    public LinearLayout getFilterContainer() {
        return filterContainer;
    }

    public TextView getFilterTextView() {
        return filterTextView;
    }

    public ImageView getFilterArrow() {
        return filterArrow;
    }

    public View getBackgroundDimView() {
        return backgroundDimView;
    }



    /**
     * Functional methods
     */

    public void setOnSelectionListener(OnSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public int getSelectingPosition() {
        return selectingPosition;
    }

    public void setSelectingPosition(int selectingPosition) {
        this.selectingPosition = selectingPosition;
        filterTextView.setText(dropDownItemList.get(selectingPosition));
        if (onSelectionListener != null) onSelectionListener.onItemSelected(DropDownView.this, selectingPosition);
        collapse(true);
    }




    /**
     * View configurations
     */

    public void setFilterHeight(float filterHeightPixels) {
        this.filterHeight = filterHeightPixels;
        invalidate();
    }

    public void setTextSize(float textSizePixels) {
        this.textSize = textSizePixels;
        invalidate();
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setFilterTextColor(int filterTextColor) {
        this.filterTextColor = filterTextColor;
        invalidate();
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setFilterBarBackgroundColor(int filterBarBackgroundColor) {
        this.filterBarBackgroundColor = filterBarBackgroundColor;
        invalidate();
    }

    public void setArrowDrawableResId(int arrowDrawableResId) {
        this.arrowDrawableResId = arrowDrawableResId;
        invalidate();
    }

    public void setArrowRotate(boolean arrowRotate) {
        isArrowRotate = arrowRotate;
        filterArrow.setRotation(0);
    }

    public void setDividerHeight(float dividerHeightPixels) {
        this.dividerHeight = dividerHeightPixels;
        invalidate();
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDropDownItemHeight(float dropDownItemHeightPixels) {
        this.dropDownItemHeight = dropDownItemHeightPixels;
    }

    public void setDropDownItemTextSize(float dropDownItemTextSizePixels) {
        this.dropDownItemTextSize = dropDownItemTextSizePixels;
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setDropDownItemTextColor(int dropDownItemTextColor) {
        this.dropDownItemTextColor = dropDownItemTextColor;
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setDropDownItemTextColorSelected(int dropDownItemTextColorSelected) {
        this.dropDownItemTextColorSelected = dropDownItemTextColorSelected;
    }

    /**
     * This method accepts a color value, do not pass in a color resource id.
     */
    public void setDropDownBackgroundColor(int dropDownBackgroundColor) {
        this.dropDownBackgroundColor = dropDownBackgroundColor;
    }

    public void setExpandDimBackground(boolean expandDimBackground) {
        isExpandDimBackground = expandDimBackground;
    }

    public void setExpandIncludeSelectedItem(boolean expandIncludeSelectedItem) {
        isExpandIncludeSelectedItem = expandIncludeSelectedItem;
    }

    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
        invalidate();
    }

    public void setTypeface(int fontResourceId) {
        this.typeface = fontResourceId;
        invalidate();
    }

    public void setAnimationDuration(int ms) {
        this.animationDuration = ms;
    }
}
