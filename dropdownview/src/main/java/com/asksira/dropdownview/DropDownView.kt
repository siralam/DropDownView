package com.asksira.dropdownview

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.FontRes
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import com.transitionseverywhere.ChangeBounds
import com.transitionseverywhere.Fade
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet

open class DropDownView : LinearLayout {

    companion object {
        const val COLLAPSED = 1
        const val EXPANDED = 2

        const val REVEAL = 0
        const val DRAWER = 1

        private const val START = 1
        private const val END = 2
    }

    //Views
    /**
     * Below are views that opens get method to users.
     */
    lateinit var filterContainer: RelativeLayout
        private set
    lateinit var filterTextView: TextView
        private set
    lateinit var filterArrow: ImageView
        private set
    lateinit var dropDownContainer: ScrollView
    lateinit var dropDownItemsContainer: LinearLayout
    lateinit var backgroundDimView: View

    //Configurable Attributes
    @Px
    var filterHeight: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @Px
    var textSize: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @ColorRes
    var filterTextColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    @ColorRes
    var filterBarBackgroundColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    @Px
    var arrowStartMargin: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @Px
    var arrowEndMargin: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @Px
    var arrowWidth: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @Px
    var arrowHeight: Float = 0.toFloat()
        set(value) {
            field = value
            requestLayout()
        }
    @DrawableRes
    var arrowDrawableResId: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var isArrowRotate: Boolean = false
    @Px
    var dividerHeight: Float = 0.toFloat()
    @ColorRes
    var dividerColor: Int = 0
    @Px
    var dropDownItemHeight: Float = 0.toFloat()
    @Px
    var dropDownItemTextSize: Float = 0.toFloat()
    @Px
    var dropDownItemTextSizeSelected: Float = 0.toFloat()
    @ColorRes
    var dropDownItemTextColor: Int = 0
    @ColorRes
    var dropDownItemTextColorSelected: Int = 0
    @ColorRes
    var dropDownBackgroundColor: Int = 0
    @ColorRes
    var dropDownBackgroundColorSelected: Int = 0
    var isExpandDimBackground: Boolean = false
    @ColorRes
    var dimBackgroundColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var isExpandIncludeSelectedItem: Boolean = false
    var placeholderText: String? = null
        set(value) {
            field = value
            requestLayout()
        }
    @FontRes
    var typeface: Int = 0
        set(value) {
            field = value
            requestLayout()
        }
    var animationDuration: Int = 0
    var dropdownItemGravity = Gravity.CENTER
        set(value) {
            field = when (value) {
                Gravity.CENTER_HORIZONTAL, Gravity.CENTER -> Gravity.CENTER
                Gravity.LEFT, Gravity.START -> Gravity.START or Gravity.CENTER_VERTICAL
                Gravity.RIGHT, Gravity.END -> Gravity.END or Gravity.CENTER_VERTICAL
                else -> Gravity.CENTER
            }
        }
    @DrawableRes
    var dropdownItemCompoundDrawable:Int = 0
    @ColorRes
    var topDecoratorColor: Int = 0
    @Px
    var topDecoratorHeight: Float = 0.toFloat()
    @ColorRes
    var bottomDecoratorColor: Int = 0
    @Px
    var bottomDecoratorHeight: Float = 0.toFloat()
    private var _expansionStyle: Int = DRAWER
    var expansionStyle: Int = DRAWER
        get() = _expansionStyle
        set(value) {
            if (value != REVEAL && value != DRAWER) throw IllegalArgumentException("Unexpected expansionStyle." +
                    " It should be either REVEAL(0) or DRAWER(1).")
            field = value
            _expansionStyle = value
            val lp = dropDownItemsContainer.layoutParams as FrameLayout.LayoutParams
            lp.gravity = if (value == REVEAL) {
                Gravity.TOP
            } else {
                Gravity.BOTTOM
            }
        }
    var isLastItemHasDivider = true
    var isArrowAlignEnd = false

    //Runtime Attributes
    /**
     * Selecting positing can be -1 which indicates nothing is selected.
     * Placeholder text will be shown, if configured.
     * This View is designed to disallow selecting -1 without a placeholder text.
     */
    private var _selectingPosition: Int = 0
    var selectingPosition: Int = 0
        get() = _selectingPosition
        set(value) {
            field = value
            _selectingPosition = value
            filterTextView.text = dropDownItemList[selectingPosition]
            onSelectionListener?.onItemSelected(this@DropDownView, selectingPosition)
            collapse(true)
        }
    var state = COLLAPSED
    var dropDownItemList: List<String> = ArrayList()
    var onSelectionListener: OnDropDownSelectionListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DropDownView, defStyleAttr, 0)
        try {
            filterHeight = a.getDimension(R.styleable.DropDownView_filter_height, resources.getDimension(R.dimen.filter_default_height))
            textSize = a.getDimension(R.styleable.DropDownView_filter_text_size, resources.getDimension(R.dimen.filter_text_selected_default_size))
            filterTextColor = a.getResourceId(R.styleable.DropDownView_filter_text_color, R.color.dropdown_default_text_color)
            filterBarBackgroundColor = a.getResourceId(R.styleable.DropDownView_filter_bar_background_color, android.R.color.transparent)
            arrowStartMargin = a.getDimension(R.styleable.DropDownView_arrow_start_margin, resources.getDimension(R.dimen.arrow_default_start_margin))
            arrowEndMargin = a.getDimension(R.styleable.DropDownView_arrow_end_margin, 0f)
            arrowWidth = a.getDimension(R.styleable.DropDownView_arrow_width, -1f)
            arrowHeight = a.getDimension(R.styleable.DropDownView_arrow_height, -1f)
            arrowDrawableResId = a.getResourceId(R.styleable.DropDownView_arrow_drawable, 0)
            isArrowRotate = a.getBoolean(R.styleable.DropDownView_arrow_rotate, true)
            dividerHeight = a.getDimension(R.styleable.DropDownView_divider_height, resources.getDimension(R.dimen.filter_divider_default_height))
            dividerColor = a.getResourceId(R.styleable.DropDownView_divider_color, R.color.dropdown_default_divider_color)
            dropDownItemHeight = a.getDimension(R.styleable.DropDownView_dropDownItem_height, resources.getDimension(R.dimen.filter_dropDownItem_default_height))
            dropDownItemTextSize = a.getDimension(R.styleable.DropDownView_dropDownItem_text_size, resources.getDimension(R.dimen.filter_text_default_size))
            dropDownItemTextSizeSelected = a.getDimension(R.styleable.DropDownView_dropDownItem_text_size_selected, resources.getDimension(R.dimen.filter_text_default_size))
            dropDownItemTextColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_text_color, R.color.dropdown_default_text_color)
            dropDownItemTextColorSelected = a.getResourceId(R.styleable.DropDownView_dropDownItem_text_color_selected, R.color.dropdown_default_text_color)
            dropDownBackgroundColor = a.getResourceId(R.styleable.DropDownView_dropDownItem_background_color, android.R.color.white)
            dropDownBackgroundColorSelected = a.getResourceId(R.styleable.DropDownView_dropDownItem_background_color_selected, android.R.color.white)
            isExpandDimBackground = a.getBoolean(R.styleable.DropDownView_expand_dim_background, true)
            dimBackgroundColor = a.getResourceId(R.styleable.DropDownView_dim_background_color, R.color.dropdown_background_dim)
            isExpandIncludeSelectedItem = a.getBoolean(R.styleable.DropDownView_expand_include_selected_item, true)
            placeholderText = a.getString(R.styleable.DropDownView_placeholder_text)
            typeface = a.getResourceId(R.styleable.DropDownView_dropdown_typeface, 0)
            animationDuration = a.getInteger(R.styleable.DropDownView_dropdown_animation_duration, 300)
            dropdownItemGravity = when (a.getInt(R.styleable.DropDownView_dropdownItem_text_gravity, Gravity.CENTER)) {
                START -> Gravity.START
                END -> Gravity.END
                else  -> Gravity.CENTER
            }
            dropdownItemCompoundDrawable = a.getResourceId(R.styleable.DropDownView_dropdownItem_compound_drawable_selected, 0)
            topDecoratorColor = a.getResourceId(R.styleable.DropDownView_top_decorator_color, android.R.color.transparent)
            topDecoratorHeight = a.getDimension(R.styleable.DropDownView_top_decorator_height, 0.toFloat())
            bottomDecoratorColor = a.getResourceId(R.styleable.DropDownView_bottom_decorator_color, android.R.color.transparent)
            bottomDecoratorHeight = a.getDimension(R.styleable.DropDownView_bottom_decorator_height, 0.toFloat())
            _expansionStyle = a.getInt(R.styleable.DropDownView_expansion_style, DRAWER)
            isLastItemHasDivider = a.getBoolean(R.styleable.DropDownView_last_item_has_divider, true)
            isArrowAlignEnd = a.getBoolean(R.styleable.DropDownView_arrow_align_end, false)
        } finally {
            a.recycle()
        }

        orientation = VERTICAL
        View.inflate(context, R.layout.widget_dropdownview, this)
        filterContainer = findViewById(R.id.filter_container)
        filterTextView = findViewById(R.id.filter_text)
        filterArrow = findViewById(R.id.filter_arrow)
        dropDownContainer = findViewById(R.id.sv_dropdown_container)
        dropDownItemsContainer = findViewById(R.id.ll_dropdown_items_container)
        backgroundDimView = findViewById(R.id.background_dim)

        //Configure filter bar
        val lp = filterContainer.layoutParams
        lp.height = filterHeight.toInt()
        filterContainer.layoutParams = lp
        filterContainer.setBackgroundColor(ContextCompat.getColor(context, filterBarBackgroundColor))
        filterContainer.setOnClickListener { toggle(true) }

        //Configure filter text
        if (typeface != 0) filterTextView.typeface = ResourcesCompat.getFont(context, typeface)
        filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        filterTextView.setTextColor(ContextCompat.getColor(context, filterTextColor))
        if (placeholderText?.isNotEmpty() == true) {
            filterTextView.text = placeholderText
            _selectingPosition = -1
        } else {
            _selectingPosition = 0
        }

        //Configure arrow
        if (arrowWidth > -1 || arrowHeight > -1) {
            val arrowLp = filterArrow.layoutParams as RelativeLayout.LayoutParams
            if (arrowHeight > -1) arrowLp.height = arrowHeight.toInt()
            if (arrowWidth > -1) arrowLp.width = arrowWidth.toInt()
            if (isArrowAlignEnd) {
                arrowLp.addRule(RelativeLayout.ALIGN_PARENT_END)
            } else {
                arrowLp.addRule(RelativeLayout.END_OF, R.id.filter_text)
            }
            arrowLp.marginStart = arrowStartMargin.toInt()
            arrowLp.marginEnd = arrowEndMargin.toInt()
            filterArrow.layoutParams = arrowLp
        }
        if (arrowDrawableResId != 0) {
            filterArrow.setImageResource(arrowDrawableResId)
        }

        //Configure background dim
        backgroundDimView.setBackgroundColor(ContextCompat.getColor(context,
                if (isExpandDimBackground) dimBackgroundColor else android.R.color.transparent))
        backgroundDimView.setOnClickListener { collapse(true) }

        //Configure expansion style
        expansionStyle = _expansionStyle
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && state == EXPANDED) {
            collapse(true)
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.state = this.state
        savedState.selectingPosition = this.selectingPosition
        savedState.dropDownItems = this.dropDownItemList
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val savedState = state
        super.onRestoreInstanceState(savedState.superState)

        this.state = savedState.state
        this._selectingPosition = savedState.selectingPosition
        this.dropDownItemList = savedState.dropDownItems

        updateDropDownItems()
        if (selectingPosition >= 0) {
            filterTextView.text = dropDownItemList[selectingPosition]
            onSelectionListener?.onItemSelected(this@DropDownView, selectingPosition)
        }
        if (this.state == EXPANDED) {
            isFocusableInTouchMode = true
            requestFocus()
            updateDropDownItems()
            filterArrow.rotation = 180f
            backgroundDimView.visibility = View.VISIBLE
            val lp = dropDownContainer.layoutParams as FrameLayout.LayoutParams
            lp.height = WRAP_CONTENT
            dropDownContainer.layoutParams = lp
        }
    }

    internal class SavedState : View.BaseSavedState {
        var state: Int = 0
        var selectingPosition: Int = 0
        var dropDownItems: List<String> = ArrayList()

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            this.state = source.readInt()
            this.selectingPosition = source.readInt()
            source.readStringList(this.dropDownItems)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(this.state)
            out.writeInt(this.selectingPosition)
            out.writeStringList(this.dropDownItems)
        }

        companion object {

            //required field that makes Parcelables from a Parcel
            @JvmField val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    fun toggle(animated: Boolean) {
        when (state) {
            COLLAPSED -> expand(animated)
            EXPANDED -> collapse(animated)
            else -> throw IllegalStateException("This should not happen. If you see this, please submit an issue to Github")
        }
    }

    fun expand(animate: Boolean) {
        if (state == EXPANDED) return
        isFocusableInTouchMode = true
        requestFocus()
        updateDropDownItems()
        if (isArrowRotate) {
            if (animate) {
                filterArrow.rotation = 0f
                filterArrow.animate().rotationBy(-180f).setDuration(animationDuration.toLong()).start()
            } else {
                filterArrow.rotation = -180f
            }
        }
        if (animate) {
            val transitionSet = TransitionSet()
            transitionSet.addTransition(ChangeBounds())
            transitionSet.addTransition(Fade())
            transitionSet.duration = animationDuration.toLong()
            TransitionManager.beginDelayedTransition(this, transitionSet)
        }
        backgroundDimView.visibility = View.VISIBLE
        val lp = dropDownContainer.layoutParams as FrameLayout.LayoutParams
        lp.height = WRAP_CONTENT
        dropDownContainer.layoutParams = lp
        state = EXPANDED
    }

    fun collapse(animate: Boolean) {
        if (state == COLLAPSED) return
        if (isArrowRotate) {
            if (animate) {
                filterArrow.rotation = 180f
                filterArrow.animate().rotationBy(180f).setDuration(animationDuration.toLong()).start()
            } else {
                filterArrow.rotation = 0f
            }
        }
        if (animate) {
            val transitionSet = TransitionSet()
            transitionSet.addTransition(ChangeBounds())
            transitionSet.addTransition(Fade())
            transitionSet.duration = animationDuration.toLong()
            transitionSet.excludeTarget(filterTextView, true)
            TransitionManager.beginDelayedTransition(this, transitionSet)
        }
        backgroundDimView.visibility = View.INVISIBLE
        val lp = dropDownContainer.layoutParams as FrameLayout.LayoutParams
        lp.height = 0
        dropDownContainer.layoutParams = lp
        state = COLLAPSED
    }

    fun setDropDownListItem(items: List<String>) {
        this.dropDownItemList = items
        updateDropDownItems()
    }

    private fun updateDropDownItems() {
        dropDownItemsContainer.removeAllViews()
        if (topDecoratorHeight > 0) {
            dropDownItemsContainer.addView(generateTopDecorator())
        }
        for (i in dropDownItemList.indices) {
            if (!isExpandIncludeSelectedItem) {
                if (i == selectingPosition) continue
            }
            dropDownItemsContainer.addView(generateDropDownItem(dropDownItemList[i], i))
            if (i == dropDownItemList.size - 1) {
                if (isLastItemHasDivider) dropDownItemsContainer.addView(generateDivider())
            } else {
                dropDownItemsContainer.addView(generateDivider())
            }
        }
        if (bottomDecoratorHeight > 0) {
            dropDownItemsContainer.addView(generateBottomDecorator())
        }
    }

    protected open fun generateDropDownItem(itemName: String, index: Int): View {
        val textView = TextView(context)
        val lp = LinearLayout.LayoutParams(MATCH_PARENT, dropDownItemHeight.toInt())
        textView.layoutParams = lp
        textView.text = itemName
        textView.setPadding(16.dp(), 0, 16.dp(), 0)
        if (typeface != 0) textView.typeface = ResourcesCompat.getFont(context!!, typeface)
        if (Build.VERSION.SDK_INT >= 23) {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            textView.foreground = context.getDrawable(typedValue.resourceId)
        }
        if (index == selectingPosition) {
            textView.setBackgroundColor(ContextCompat.getColor(context!!, dropDownBackgroundColorSelected))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dropDownItemTextSizeSelected)
            textView.setTextColor(ContextCompat.getColor(context!!, dropDownItemTextColorSelected))
            if (dropdownItemCompoundDrawable != 0) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, dropdownItemCompoundDrawable, 0)
            }
        } else {
            textView.setBackgroundColor(ContextCompat.getColor(context!!, dropDownBackgroundColor))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dropDownItemTextSize)
            textView.setTextColor(ContextCompat.getColor(context!!, dropDownItemTextColor))
        }
        textView.gravity = dropdownItemGravity
        textView.setOnClickListener { selectingPosition = index }
        return textView
    }

    private fun generateDivider(): View {
        val view = View(context)
        val lp = LinearLayout.LayoutParams(
                MATCH_PARENT, dividerHeight.toInt())
        view.layoutParams = lp
        view.setBackgroundColor(ContextCompat.getColor(context!!, dividerColor))
        return view
    }

    private fun generateTopDecorator() : View {
        val view = View(context)
        val lp = LinearLayout.LayoutParams(
                MATCH_PARENT, topDecoratorHeight.toInt())
        view.layoutParams = lp
        view.setBackgroundColor(ContextCompat.getColor(context!!, topDecoratorColor))
        return view
    }

    private fun generateBottomDecorator() : View {
        val view = View(context)
        val lp = LinearLayout.LayoutParams(
                MATCH_PARENT, bottomDecoratorHeight.toInt())
        view.layoutParams = lp
        view.setBackgroundColor(ContextCompat.getColor(context!!, bottomDecoratorColor))
        return view
    }
}
