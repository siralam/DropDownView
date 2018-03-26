package com.asksira.dropdownview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
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

public class DropDownView extends LinearLayout implements View.OnClickListener{

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
    private int textColorSelected;
    private int arrowDrawableResId;
    private boolean isArrowRotate;
    private float dividerHeight;
    private int dividerColor;
    private float dropDownItemHeight;
    private float dropDownItemTextSize;
    private int dropDownItemTextColor;
    private int dropDownBackgroundColor;
    private boolean isExpandDimBackground;
    private boolean isExpandIncludeSelectedItem;
    private String placeholderText;

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

    public DropDownView(Context context) {
        super(context);
        init(context);
    }

    public DropDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DropDownView, 0, 0);
        try {
            filterHeight = a.getDimension(R.styleable.DropDownView_filter_height, getResources().getDimension(R.dimen.filter_default_height));
            textSize = a.getDimension(R.styleable.DropDownView_text_size, getResources().getDimension(R.dimen.filter_text_selected_default_size));
            textColorSelected = a.getResourceId(R.styleable.DropDownView_text_color_selected, R.color.dropdown_default_text_color);
            arrowDrawableResId = a.getResourceId(R.styleable.DropDownView_arrow_drawable, 0);
            isArrowRotate = a.getBoolean(R.styleable.DropDownView_arrow_rotate, true);
            dividerHeight = a.getDimension(R.styleable.DropDownView_divider_height, getResources().getDimension(R.dimen.filter_divider_default_height));
            dividerColor = a.getResourceId(R.styleable.DropDownView_divider_color, R.color.dropdown_default_divider_coloir);
            dropDownItemHeight = a.getDimension(R.styleable.DropDownView_dropDownItem_height, getResources().getDimension(R.dimen.filter_dropDownItem_default_height));
            dropDownItemTextSize = a.getDimension(R.styleable.DropDownView_dropDownItem_text_size, getResources().getDimension(R.dimen.filter_text_default_size));
            dropDownItemTextColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_text_color, R.color.dropdown_default_text_color);
            dropDownBackgroundColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_background_color, android.R.color.white);
            isExpandDimBackground = a.getBoolean(R.styleable.DropDownView_expand_dim_background, true);
            isExpandIncludeSelectedItem = a.getBoolean(R.styleable.DropDownView_expand_include_selected_item, true);
            placeholderText = a.getString(R.styleable.DropDownView_placeholder_text);
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

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) filterContainer.getLayoutParams();
        lp.height = (int)filterHeight;
        filterContainer.setLayoutParams(lp);

        filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        filterTextView.setTextColor(ContextCompat.getColor(context, textColorSelected));
        if (placeholderText != null && !placeholderText.isEmpty()) {
            filterTextView.setText(placeholderText);
            selectingPosition = -1;
        } else {
            selectingPosition = 0;
        }

        if (arrowDrawableResId != 0) {
            filterArrow.setImageResource(arrowDrawableResId);
        }

        filterContainer.setOnClickListener(this);

        backgroundDimView.setBackgroundColor(ContextCompat.getColor(context,
                isExpandDimBackground ? R.color.dropdown_background_dim : android.R.color.transparent));
        backgroundDimView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse();
            }
        });
    }

    @Override
    public void onClick(View v) {
        toggle();
    }

    public void toggle() {
        switch (state) {
            case COLLAPSED:
                expand();
                break;
            case EXPANDED:
                collapse();
                break;
            default:
                throw new IllegalStateException("This should not happen. If you see this, please submit an issue to Github");
        }
    }

    public void expand () {
        if (state == EXPANDED) return;
        if (isArrowRotate) {
            filterArrow.setRotation(0);
            filterArrow.animate().rotationBy(-180).setDuration(300).start();
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new Fade());
        transitionSet.setDuration(300);
        TransitionManager.beginDelayedTransition(this, transitionSet);
        backgroundDimView.setVisibility(VISIBLE);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) dropDownContainer.getLayoutParams();
        lp.height = WRAP_CONTENT;
        dropDownContainer.setLayoutParams(lp);
        state = EXPANDED;
    }

    public void collapse () {
        if (state == COLLAPSED) return;
        if (isArrowRotate) {
            filterArrow.setRotation(180);
            filterArrow.animate().rotationBy(180).setDuration(300).start();
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new Fade());
        transitionSet.setDuration(300);
        TransitionManager.beginDelayedTransition(this, transitionSet);
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
            dropDownItemsContainer.addView(generateDropDownItem(dropDownItemList.get(i)));
            dropDownItemsContainer.addView(generateDivider());
        }
    }

    private TextView generateDropDownItem (String itemName) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, (int)dropDownItemHeight);
        textView.setLayoutParams(lp);
        textView.setText(itemName);
        textView.setBackgroundColor(ContextCompat.getColor(context, dropDownBackgroundColor));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dropDownItemTextSize);
        textView.setTextColor(ContextCompat.getColor(context, dropDownItemTextColor));
        textView.setGravity(Gravity.CENTER);
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

}
