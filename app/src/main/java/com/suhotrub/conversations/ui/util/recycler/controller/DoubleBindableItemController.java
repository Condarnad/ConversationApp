package com.suhotrub.conversations.ui.util.recycler.controller;

import com.suhotrub.conversations.ui.util.recycler.holder.DoubleBindableViewHolder;
import com.suhotrub.conversations.ui.util.recycler.item.DoubleBindableItem;

/**
 * Controller for item of RecyclerView with two block of data {@link DoubleBindableItem}
 * It used with {@link EasyAdapter} and {@link ItemList}
 *
 * @param <H>  type of ViewHolder
 * @param <T1> first data type
 * @param <T2> second data type
 */
public abstract class DoubleBindableItemController<T1, T2, H extends DoubleBindableViewHolder<T1, T2>>
        extends BaseItemController<H, DoubleBindableItem<T1, T2, H>> {

    @Override
    public final void bind(H holder, DoubleBindableItem<T1, T2, H> item) {
        bind(holder, item.getFirstData(), item.getSecondData());
    }

    public void bind(H holder, T1 firstData, T2 secondData) {
        holder.bind(firstData, secondData);
    }

    @Override
    public final long getItemId(DoubleBindableItem<T1, T2, H> item) {
        return getItemId(item.getFirstData(), item.getSecondData());
    }

    @Override
    public final long getItemHash(DoubleBindableItem<T1, T2, H> item) {
        return getItemHash(item.getFirstData(), item.getSecondData());
    }

    /**
     * must return unique value
     * method is used for automatically call notify... methods, see {@link EasyAdapter}
     * @return item id
     */
    protected abstract long getItemId(T1 firstData, T2 secondData);

    /**
     * method is used for automatically call notify... methods, see {@link EasyAdapter}
     * @return hash of item's data
     */
    protected long getItemHash(T1 firstData, T2 secondData) {
        return (firstData == null ? 0 : firstData.hashCode())
                + (secondData == null ? 0 : secondData.hashCode());
    }
}