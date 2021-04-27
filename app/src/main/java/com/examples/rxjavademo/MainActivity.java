package com.examples.rxjavademo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartObservable(View view) {
        Random random = new Random();
        int n1 = random.nextInt(10);
        int n2 = random.nextInt(10);
        int n3 = random.nextInt(10);

//        Observable observable = Observable.just(n1, n2, n3);
//        Log.i(TAG, "Observable.just(" + n1 + ", " + n2 + ", " + n3 + ")");

        Observable<Integer> observable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            emitter.onNext(n1);
            Thread.sleep(1000);
            emitter.onNext(n2);
            Thread.sleep(1000);
            emitter.onNext(n3);
            Thread.sleep(1000);
            if (n1 % 2 == 0) {
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(n1 + " % 2 != 0"));
            }
        });
        Log.i(TAG, "Observable.create(" + n1 + ", " + n2 + ", " + n3 + ") - " + Thread.currentThread().getName());


        new Thread(() -> {
            Log.i(TAG, "subscribe - " + Thread.currentThread().getName());
            observable.compose(new AsyncTransformer())
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

    public class AsyncTransformer implements ObservableTransformer {
        @Override
        public ObservableSource apply(Observable upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

}