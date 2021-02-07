package com.duxl.baselib.http;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.duxl.baselib.ui.status.IRefreshContainer;
import com.duxl.baselib.ui.status.IStatusViewContainer;
import com.duxl.baselib.utils.EmptyUtils;
import com.scwang.smart.refresh.layout.constant.RefreshState;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * <pre>
 * BaseRecyclerViewHttpObserver，适用于RecyclerView分页加载的情况
 * create by duxl 2021/1/15
 * </pre>
 * @param <R>
 * @param <T>
 */
public abstract class BaseRecyclerViewHttpObserver<R extends BaseRoot, T> extends BaseHttpObserver<R> {

    private BaseQuickAdapter mAdapter;
    private IRefreshContainer mIRefreshContainer;
    private IStatusViewContainer mIStatusViewContainer;

    public BaseRecyclerViewHttpObserver(BaseQuickAdapter adapter) {
        this(adapter, null, null);
    }

    /**
     * @param adapter
     * @param refreshContainer    刷新容器
     * @param statusViewContainer 状态容器
     */
    public BaseRecyclerViewHttpObserver(BaseQuickAdapter adapter, IRefreshContainer refreshContainer, IStatusViewContainer statusViewContainer) {
        mAdapter = adapter;
        if (mAdapter == null) {
            throw new IllegalArgumentException("The adapter parameter cannot be null");
        }
        mIRefreshContainer = refreshContainer;
        mIStatusViewContainer = statusViewContainer;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        super.onSubscribe(d);
        if (isFirstPage() && mIStatusViewContainer != null) {
            if (EmptyUtils.isEmpty(mAdapter.getData())) {
                mIStatusViewContainer.getStatusView().showLoading();
            }
        }
    }

    @Override
    public void onNext(@NonNull R root) {
        loadComplete();
        if (root.isSuccess()) {
            try {
                onSuccess(root);
                List<T> list = getListData(root);
                if (list != null) {
                    if (isFirstPage()) {
                        mAdapter.setNewInstance(list);
                    } else {
                        mAdapter.addData(list);
                    }
                }

                if (EmptyUtils.isEmpty(mAdapter.getData())) {
                    if (mIStatusViewContainer != null) {
                        mIStatusViewContainer.getStatusView().showEmpty();
                    }
                } else {
                    if (mIStatusViewContainer != null) {
                        mIStatusViewContainer.getStatusView().showContent();
                    }
                    if (hasMoreData(root)) {
                        if (mIRefreshContainer != null) {
                            mIRefreshContainer.resetNoMoreData();
                        }
                    } else {
                        if (mIRefreshContainer != null) {
                            mIRefreshContainer.finishLoadMoreWithNoMoreData();
                        }
                    }
                }
            } catch (Exception e) {
                onError(e);
            }

        } else {
            onError(root.getCode(), root.getMsg(), root);
        }
    }

    @Override
    public void onError(int code, String msg, R root) {
        super.onError(code, msg, root);
        loadComplete();
        if (mIStatusViewContainer != null) {
            if (EmptyUtils.isEmpty(mAdapter.getData())) {
                mIStatusViewContainer.getStatusView().showEmpty();
            }
        }
    }

    protected void loadComplete() {
        if (mIRefreshContainer != null) {
            if (mIRefreshContainer.getRefreshLayout().getState() == RefreshState.Refreshing) {
                mIRefreshContainer.getRefreshLayout().finishRefresh();
            } else if (mIRefreshContainer.getRefreshLayout().getState() == RefreshState.Loading) {
                mIRefreshContainer.getRefreshLayout().finishLoadMore(0);
            }
        }
    }

    @Override
    public void onSuccess(R root) {
    }

    public abstract List<T> getListData(R root);

    public abstract boolean isFirstPage();

    public abstract boolean hasMoreData(R root);
}