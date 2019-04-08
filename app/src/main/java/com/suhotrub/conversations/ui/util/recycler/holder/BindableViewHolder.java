package com.suhotrub.conversations.ui.util.recycler.holder;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

/**
 * ViewHolder поддерживающий отображение данных
 *
 * @param <T>
 */
public abstract class BindableViewHolder<T> extends BaseViewHolder {

    public BindableViewHolder(ViewGroup parent, @LayoutRes int layoutRes) {
        super(parent, layoutRes);
    }

    public BindableViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T data);

    public void recycle() {

    }
}