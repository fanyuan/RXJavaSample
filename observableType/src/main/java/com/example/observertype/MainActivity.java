package com.example.observertype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void observable(View view) {
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

    public void flowable(View view) {
        Flowable flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < 20; i++) { //int i = 0; i < 1000000000; i++
                    emitter.onNext(i);
                    //这里还是发射了20条数据
                    Log.d("log", "subscribe: " + i);
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d("log", "onSubscribe");
                //这里体现的是响应式拉取
                //s.request(Long.MAX_VALUE);//指定下游（观察者）接收数据的最大值
                s.request(10);
            }

            @Override
            public void onNext(Integer integer) {
                //拉取10条数据
                Log.d("log", "onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.d("log", "onError " + t);
            }

            @Override
            public void onComplete() {
                Log.d("log", "onComplete");
            }
        };

        flowable.subscribe(subscriber);
    }

    public void single(View view) {
        Single single = Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> emitter) throws Exception {
                for(int i = 0;i< 5; i++){
                    Utils.log("single subscribe ");
                    emitter.onSuccess("这是一消息 123");
                    emitter.onSuccess("这是一消息");
                }
            }
        });

        SingleObserver singleObserver = new SingleObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Utils.log("singleObserver onSubscribe " + d);
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                Utils.log("singleObserver onSuccess " + o);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Utils.log("singleObserver onError " + e);
            }
        };

        single.subscribe(singleObserver);
    }

    public void completableFromAction(View view) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("Completable.fromAction ");
            }
        }).subscribe();

    }
    public void completableFromActionLambda(View view) {
        Completable.fromAction(()->{Utils.log("Completable.fromAction lambda");}).subscribe();
    }
    public void completableAndThen(View view) {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter emitter) throws Exception {

                Utils.log("completableAndThen subscribe   " + Thread.currentThread().getName());
                try {
                    TimeUnit.SECONDS.sleep(2);
                    emitter.onComplete();
                } catch (InterruptedException e) {
                    emitter.onError(e);
                }
            }
        })      .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .andThen(Observable.range(1, 10))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Utils.log("completableAndThen accept " + integer + "  " + Thread.currentThread().getName());
                    }
                });
    }

    public void completableAndThenLambda(View view) {
        Completable.create((emitter -> {
            Utils.log("completableAndThen subscribe lambda  " + Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(1);
                emitter.onComplete();
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        }))
//           .subscribeOn(Schedulers.io())
//           .observeOn(AndroidSchedulers.mainThread())
           .andThen(Observable.range(0,9))
           .subscribe((integer -> {
               Utils.log("completableAndThen accept lambda " + integer + "  " + Thread.currentThread().getName());
           }));
    }


    public void maybe(View view) {
        Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> emitter) throws Exception {
                emitter.onSuccess("testA");
                emitter.onSuccess("testB");
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log("maybe accept " + s + "  " + Thread.currentThread().getName());
            }
        });
    }

    public void maybeLambda(View view) {
        Maybe.create(emitter -> {
            emitter.onSuccess("testA");
            emitter.onSuccess("testB");
        })
          .subscribe(str -> {
              Utils.log("maybe accept lambda " + str + "  " + Thread.currentThread().getName());
          });
    }
}