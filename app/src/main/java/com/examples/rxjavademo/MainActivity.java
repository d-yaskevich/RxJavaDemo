package com.examples.rxjavademo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int MAX_VALUE = 10;
    private ArrayList<Integer> values = new ArrayList<>();

    private TextView tvRandomValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_generate_random).setOnClickListener(v -> {
            Random random = new Random();
            values.clear();
            values.add(random.nextInt(MAX_VALUE));
            values.add(random.nextInt(MAX_VALUE));
            values.add(random.nextInt(MAX_VALUE));

            showRandomValues();
        });
        tvRandomValues = findViewById(R.id.tv_random_values);
    }

    private void showRandomValues() {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            int value = values.get(i);
            msg.append(value);

            if (i != values.size() - 1) {
                msg.append(", ");
            }
        }
        tvRandomValues.setText(msg);
    }

    public void onStartObservable(View view) {
//        Observable observable = Observable.just(n1, n2, n3);
//        Log.i(TAG, "Observable.just(" + n1 + ", " + n2 + ", " + n3 + ")");

        Observable<Integer> observable = Observable.create(emitter -> {
            for (int value : values) {
                emitter.onNext(value);
            }
            int first = values.get(0);
            if (first % 2 == 0) {
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(first + " % 2 != 0"));
            }
        });

        new Thread(() -> {
            Log.i(TAG, "subscribe - " + Thread.currentThread().getName());
            observable.compose(new AsyncObservableTransformer())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
                    .map(o -> {
                        boolean bool = (Integer) o % 2 == 0;
                        Log.i(TAG, "map(" + o + "->" + bool + ") - " + Thread.currentThread().getName());
                        return bool;
                    })
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            Log.i(TAG, "onSubscribe() - " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onNext(@NonNull Boolean bool) {
                            Log.i(TAG, "onNext(" + bool + ") - " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.i(TAG, "onError(" + e.getMessage() + ") - " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onComplete() {
                            Log.i(TAG, "onComplete() - " + Thread.currentThread().getName());
                        }
                    });
        }).start();
    }

    public void onStartSingle(View view) {
        Single<Integer> single = Single.create(emitter -> {
            int first = values.get(0);
            if (first % 2 == 0) {
                emitter.onSuccess(first);
            } else {
                emitter.onError(new Throwable(first + " % 2 != 0"));
            }

        });

        new Thread(() -> {
            Log.i(TAG, "subscribe - " + Thread.currentThread().getName());
            single.compose(new AsyncSingleTransformer())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
                    .map(o -> {
                        boolean bool = (Integer) o % 2 == 0;
                        Log.i(TAG, "map(" + o + "->" + bool + ") - " + Thread.currentThread().getName());
                        return bool;
                    })
                    .subscribe(new SingleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            Log.i(TAG, "onSubscribe() - " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onSuccess(@NonNull Boolean bool) {
                            Log.i(TAG, "onSuccess(" + bool + ") - " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.i(TAG, "onError(" + e.getMessage() + ") - " + Thread.currentThread().getName());
                        }
                    });
        }).start();
    }

    public class AsyncObservableTransformer implements ObservableTransformer {
        @Override
        public ObservableSource apply(Observable upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public class AsyncSingleTransformer implements SingleTransformer {
        @Override
        public @NonNull SingleSource apply(@NonNull Single upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

}