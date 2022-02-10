package com.example.createoperators;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

//import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toActivity2(View view) {
        startActivity(new Intent(this, Activity2.class));
    }

    public void create(View view) {
        //Observable observable = Observable.create((String)->{});
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                Utils.log("subscribe --> " + Thread.currentThread().getName());
                emitter.onNext("hello  " );//发送事件时，观察者会回调onNext方法
                emitter.onComplete();//这个顺序不能颠倒，如果onNext方法放在最后，onNext就不会执行了
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Utils.log("onSubscribe --> " + Thread.currentThread().getName() + " \n" + d  );
            }

            @Override
            public void onNext(@NonNull String s) {
                Utils.log("onNext --> " + Thread.currentThread().getName() + " \n" + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Utils.log("onError --> " + Thread.currentThread().getName() + " \n" + e);
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete --> " + Thread.currentThread().getName());
            }
        };

        observable.subscribe(observer);
    }


}