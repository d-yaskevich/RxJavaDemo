package com.examples.rxjavademo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int MAX_VALUE = 10;
    private ArrayList<Integer> values = new ArrayList<>();

    private TextView tvRandomValues;
    private ViewGroup vgProgress;

    private Observable<Integer> observable;
    private Single<Integer> single;
    private BehaviorSubject<Integer> subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_generate_random).setOnClickListener(v -> {
            setRandomValues();
        });
        tvRandomValues = findViewById(R.id.tv_random_values);
        vgProgress = findViewById(R.id.vg_progress);

        createObservable();
        createSingle();

        setRandomValues();
    }

    private void setRandomValues() {
        Random random = new Random();
        values.clear();
        values.add(random.nextInt(MAX_VALUE));
        values.add(random.nextInt(MAX_VALUE));
        values.add(random.nextInt(MAX_VALUE));
        showRandomValues();
    }

    private void createSingle() {
//        single = Single.just(n1, n2, n3);
//        Log.i(TAG, "Single.just(" + n1 + ", " + n2 + ", " + n3 + ")");

        single = Single.create(emitter -> {
            Log.i(TAG, "start single - " + Thread.currentThread().getName());
            Thread.sleep(1000);
            int first = values.get(0);
            if (first % 2 == 0) {
                emitter.onSuccess(first);
                Log.i(TAG, "onSuccess(" + first + ") - " + Thread.currentThread().getName());
            } else {
                String msg = first + " % 2 != 0";
                emitter.onError(new Throwable(msg));
                Log.i(TAG, "onError(" + msg + ") - " + Thread.currentThread().getName());
            }
        });
    }

    private void createObservable() {
//        observable = Observable.just(n1, n2, n3);
//        Log.i(TAG, "Observable.just(" + n1 + ", " + n2 + ", " + n3 + ")");

        observable = Observable.create(emitter -> {
            Log.i(TAG, "start observable - " + Thread.currentThread().getName());
            for (int value : values) {
                emitter.onNext(value);
                Log.i(TAG, "onNext(" + value + ") - " + Thread.currentThread().getName());
                Thread.sleep(1000);
            }
            int first = values.get(0);
            if (first % 2 == 0) {
                emitter.onComplete();
                Log.i(TAG, "onComplete() - " + Thread.currentThread().getName());
            } else {
                String msg = first + " % 2 != 0";
                emitter.onError(new Throwable(msg));
                Log.i(TAG, "onError(" + msg + ") - " + Thread.currentThread().getName());
            }
        });
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
//        new Thread(() -> {
        String msg = "subscribe (observable) - ";
        Log.i(TAG, msg + Thread.currentThread().getName());
        observable.compose(new AsyncObservableTransformer())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
                .map(o -> {
                    boolean bool = (Integer) o % 2 == 0;
                    Log.i(TAG, msg + "map(" + o + "->" + bool + ") - " + Thread.currentThread().getName());
                    return bool;
                })
                .doOnSubscribe(disposable -> {
                    vgProgress.setVisibility(View.VISIBLE);
                })
                .doOnTerminate(() -> {
                    vgProgress.setVisibility(View.GONE);
                })
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.i(TAG, msg + "onSubscribe() - " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(@NonNull Boolean bool) {
                        Log.i(TAG, msg + "onNext(" + bool + ") - " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i(TAG, msg + "onError(" + e.getMessage() + ") - " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, msg + "onComplete() - " + Thread.currentThread().getName());
                    }
                });
//        }).start();
    }

    public void onStartSingle(View view) {
//        new Thread(() -> {
        String msg = "subscribe (single) - ";
        Log.i(TAG, msg + Thread.currentThread().getName());
        single.compose(new AsyncSingleTransformer())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
                .map(o -> {
                    boolean bool = (Integer) o % 2 == 0;
                    Log.i(TAG, msg + "map(" + o + "->" + bool + ") - " + Thread.currentThread().getName());
                    return bool;
                })
                .doOnSubscribe(disposable -> {
                    vgProgress.setVisibility(View.VISIBLE);
                })
                .doOnTerminate(() -> {
                    vgProgress.setVisibility(View.GONE);
                })
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.i(TAG, msg + "onSubscribe() - " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onSuccess(@NonNull Boolean bool) {
                        Log.i(TAG, msg + "onSuccess(" + bool + ") - " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i(TAG, msg + "onError(" + e.getMessage() + ") - " + Thread.currentThread().getName());
                    }
                });
//        }).start();
    }

    public void onStartSubjectObservable(View view) {
        Log.i(TAG, "subscribe (observable/subject) - " + Thread.currentThread().getName());
//        subject = AsyncSubject.create();
        subject = BehaviorSubject.createDefault(-1);
        observable.compose(new AsyncObservableTransformer())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subject);
    }

    public void onStartSubject(View view) {
        String msg = "subscribe (subject) - ";
        Log.i(TAG, msg + Thread.currentThread().getName());
        subject.doOnSubscribe(disposable -> {
            vgProgress.setVisibility(View.VISIBLE);
            Log.i(TAG, msg + "doOnSubscribe()" + Thread.currentThread().getName());
        }).doOnTerminate(() -> {
            vgProgress.setVisibility(View.GONE);
            Log.i(TAG, msg + "doOnTerminate()" + Thread.currentThread().getName());
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.i(TAG, msg + "onSubscribe() - " + Thread.currentThread().getName());
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Log.i(TAG, msg + "onNext(" + integer + ") - " + Thread.currentThread().getName());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.i(TAG, msg + "onError(" + e.getMessage() + ") - " + Thread.currentThread().getName());
            }

            @Override
            public void onComplete() {
                Log.i(TAG, msg + "onComplete() - " + Thread.currentThread().getName());
            }
        });
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