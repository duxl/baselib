package com.duxl.baselib.rx;

import android.content.Context;

import com.duxl.baselib.R;
import com.duxl.baselib.utils.EmptyUtils;
import com.duxl.baselib.widget.dialog.ProgressDialog;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;

/**
 * create by duxl 2021/3/25
 */
public class ProgressTransformer<T> implements ObservableTransformer<T, T> {

    protected ProgressDialog mProgressDialog;
    private WeakReference<Context> mContextReference;

    public ProgressTransformer(Context context) {
        this(context, false, true);
    }

    public ProgressTransformer(Context context, boolean cancelTouchOutside, boolean cancel) {
        this(context, context.getString(R.string.brvah_loading), cancelTouchOutside, cancel);
    }

    public ProgressTransformer(Context context, CharSequence msg, boolean cancelTouchOutside, boolean cancel) {
        this.mContextReference = new WeakReference(context);
        this.mProgressDialog = getProgressDialog();
        if (EmptyUtils.isNotNull(mProgressDialog)) {
            mProgressDialog.setCancelTouchOutside(cancelTouchOutside);
            mProgressDialog.setCancelable(cancel);
            mProgressDialog.show(msg);

        }
    }

    public void setMsg(CharSequence msg) {
        if (EmptyUtils.isNotNull(mProgressDialog)) {
            mProgressDialog.setMessage(msg);
        }
    }

    protected ProgressDialog getProgressDialog() {
        if (EmptyUtils.isNotNull(mContextReference.get())) {
            return new ProgressDialog(mContextReference.get());
        }
        return null;
    }

    @Override
    public @NonNull ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return upstream
                .doOnSubscribe(disposable -> {
                })
                .doOnTerminate(() -> {
                    if (EmptyUtils.isNotNull(mProgressDialog)) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                })
                .doOnDispose(() -> {
                    if (EmptyUtils.isNotNull(mProgressDialog)) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                });
    }
}
