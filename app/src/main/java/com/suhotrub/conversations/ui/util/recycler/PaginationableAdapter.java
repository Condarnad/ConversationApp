package com.suhotrub.conversations.ui.util.recycler;

import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.suhotrub.conversations.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * адаптер для пагинируемых списков
 */
public class PaginationableAdapter extends BasePaginationableAdapter {

    private PaginationFooterItemController paginationFooterItemController;

    @Override
    protected BasePaginationFooterController getPaginationFooterController() {
        if (paginationFooterItemController == null) {
            paginationFooterItemController = new PaginationFooterItemController();
        }
        return paginationFooterItemController;
    }


    protected static class PaginationFooterItemController extends BasePaginationFooterController<PaginationFooterItemController.Holder> {

        @Override
        protected Holder createViewHolder(ViewGroup parent, OnShowMoreListener listener) {
            return new Holder(parent, listener);
        }

        protected class Holder extends BasePaginationFooterHolder {

            private ProgressBar progressBar;
            private TextView showMoreTv;


            public Holder(ViewGroup parent, OnShowMoreListener listener) {
                super(parent, R.layout.pagination_footer_layout);
                progressBar = (ProgressBar) itemView.findViewById(R.id.pagination_footer_progress);
                showMoreTv = (TextView) itemView.findViewById(R.id.pagination_footer_text);
                showMoreTv.setOnClickListener(v -> listener.onShowMore());
                progressBar.setVisibility(GONE);
                showMoreTv.setVisibility(GONE);
            }

            @Override
            public void bind(PaginationState state) {
                switch (state) {
                    case LOADING:
                        progressBar.setVisibility(VISIBLE);
                        showMoreTv.setVisibility(GONE);
                        break;
                    case COMPLETE:
                    case READY:
                        progressBar.setVisibility(GONE);
                        showMoreTv.setVisibility(GONE);
                        break;
                    case ERROR:
                        progressBar.setVisibility(GONE);
                        showMoreTv.setVisibility(VISIBLE);
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported state: " + state);
                }
            }
        }
    }
}
