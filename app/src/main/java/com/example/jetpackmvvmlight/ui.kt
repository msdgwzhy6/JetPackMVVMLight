package com.example.jetpackmvvmlight

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.jetpackmvvmlight.app.utils.HtmlUtil
import com.example.jetpackmvvmlight.entity.Page
import com.fondesa.recyclerviewdivider.dividerBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * viewpager2 滑动监听
 */
fun ViewPager2.setOnPageChangeListener(method: (position: Int) -> Unit): ViewPager2 {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            method.invoke(position)
        }
    })
    return this
}

/**
 * editext 代替addTextChangedListener
 */
fun EditText.setOnTextChanged(method: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit): EditText {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            method.invoke(s, start, before, count)
        }
    })
    return this
}


/**
 * 点击事件扩展方法
 */
fun View.onClick(method: () -> Unit): View {
    setOnClickListener { method.invoke() }
    return this
}

/**
 * 设置View的可见
 */
fun View.isVisible(isVisible: Boolean): View {
    visibility = if (isVisible) View.VISIBLE else View.GONE
    return this
}

/**
 * 清除图片bitmap
 */
fun ImageView.resetImage() {
    setImageBitmap(null)
    setImageDrawable(null)
    background = null
}

/**
 * Activity
 * 隐藏软键盘
 */
fun Activity.hidekeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive && currentFocus != null) {
        if (currentFocus!!.windowToken != null) {
            imm.hideSoftInputFromWindow(
                currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

/**
 * View
 * 隐藏软键盘
 */
fun View.hideKeyboard() {
    val imm = context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * TextView
 * 添加drawableView
 */
fun TextView.drawableView(
    context: Context,
    id: Int,
    isLeft: Boolean,
    isTop: Boolean,
    isRight: Boolean,
    isBottom: Boolean
) {
    val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.getDrawable(id)
    } else {
        TODO("VERSION.SDK_INT < LOLLIPOP")
    }
    drawable!!.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight())
    if (isLeft) {
        setCompoundDrawables(drawable, null, null, null)
        return
    }
    if (isTop) {
        setCompoundDrawables(null, drawable, null, null)
        return
    }
    if (isRight) {
        setCompoundDrawables(null, null, drawable, null)
        return
    }
    if (isBottom) {
        setCompoundDrawables(null, null, null, drawable)
        return
    }
}

/**
 * 多个stringId传值
 */
fun getString(context: Context, stringID: Int, vararg args: Any): String {
    return context.getString(stringID, *args)
}

/**
 * ImageView
 * 加载图片，直接传入url
 */
fun ImageView.loadImage(url: String) {
    loadImage(context, url, R.color.colorAccent)
}

/**
 * ImageView
 * 加载图片，直接传入context,url
 */
fun ImageView.loadImage(context: Context, url: String?) {
    loadImage(context, url, R.color.colorAccent)
}

/**
 * ImageView
 * 加载图片自定义加载图
 */
fun ImageView.loadImage(context: Context, url: String?, placeholder: Int) {
    url?.let {
        Glide.with(context)
            .load(it)
            .placeholder(placeholder)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(this)
    }
}

/**
 * dp转px
 */
fun dip2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 中间线
 */
fun TextView.centerLine() {
    paint.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
}

/**
 * 管理refresh的下拉刷新和上拉加载
 */
fun SmartRefreshLayout.finishMore(pageData: Page<*>) {
    if (pageData.current_page >= pageData.last_page) finishLoadMoreWithNoMoreData()
}

/**
 * 接口可能出错，完成调用接口后执行
 */
fun SmartRefreshLayout.complete() {
    if (isRefreshing) finishRefresh()
    if (isLoading) finishLoadMore()
}

/**
 * 倒计时
 * @param totalTime 倒计时时间
 * @param interval 间隔
 * @param start 开始监听
 * @param schedule 进度
 * @param completion 完成倒计时
 */
fun LifecycleOwner.lifecycleCountdown(
    totalTime: Int,
    interval: Long,
    start: (() -> Unit),
    schedule: ((time: Int) -> Unit),
    completion: (() -> Unit)
) {
    lifecycleScope.launch {
        start.invoke()
        repeat(totalTime) {
            schedule.invoke(totalTime - it)
            if (it == (totalTime - 1)) {
                completion.invoke()
            }
            delay(interval * 1000)
        }
    }
}

/**
 * 是否隐藏密码
 */
fun pwdState(et_pwd: EditText, iv_eye: ImageView) {
    iv_eye.isSelected = !iv_eye.isSelected
    if (iv_eye.isSelected) {
        et_pwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
    } else {
        et_pwd.transformationMethod = PasswordTransformationMethod.getInstance()
    }
    et_pwd.setSelection(et_pwd.text.length)
}

/**
 * 弹出软键盘
 */
fun EditText.upKeyboard() {
    postDelayed({
        requestFocus()
        val imm: InputMethodManager = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED)
    }, 50)
}

/**
 * TextView
 * 赋值html格式文本
 */
fun TextView.setHtml(html: String) {
    this.text = HtmlUtil.fromHtml(html)
}

/**
 * 添加recyclerviewLine
 */
fun Context.addDivider(rvList: RecyclerView, dpValue: Float) {
    dividerBuilder()
        .size(dip2px(this, dpValue))
        .showFirstDivider()
        .showLastDivider()
        .showSideDividers()
        .asSpace()
        .build()
        .addTo(rvList)
}

/**
 * BottomSheetDialog
 * 从底部向上弹dialog，自定义布局
 */
fun BottomSheetDialog.initBottomSheetDialog(context: Context, layout: Int): View {
    val view = getLayoutView(context, layout)
    setContentView(view)
    (view.parent as View).setBackgroundColor(
        ContextCompat.getColor(
            context,
            android.R.color.transparent
        )
    )
    show()
    return view
}


