package com.charles.pan.baserxjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //不支持背压，会导致内存暴增，最后导致oom
    public void ClickOne(View view) {
        //被观察者在主线程中，每1ms发送一个事件
        Observable.interval(1, TimeUnit.MICROSECONDS)
                //将观察者的工作放在新线程环境中
                .observeOn(Schedulers.newThread())
                //观察者处理每1000ms才处理一个事件
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //这是新加入的方法，在订阅后发送数据之前，
                        //回首先调用这个方法，而Disposable可用于取消订阅
                    }

                    @Override
                    public void onNext(Long aLong) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.w("TAG", "---->" + aLong);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void ClickTwo(View view) {
        //支持背压操作
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < 10000; i++) {
                    emitter.onNext(i);
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.DROP)  //指定背压策略
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d("TAG", integer.toString());
                        Thread.sleep(1000);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d("TAG", throwable.toString());
                    }
                });


//        Flowable.range(1, 10)
//                .subscribe(new Subscriber<Integer>() {
//                    @Override
//                    public void onSubscribe(Subscription s) {
//                        Log.w("TAG", "onSubscribe start");
//                        s.request(Integer.MAX_VALUE);
//                        Log.w("TAG", "onSubscribe end");
//                    }
//
//                    @Override
//                    public void onNext(Integer aLong) {
//                        Log.w("TAG", "---->" + aLong);
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        Log.w("TAG", "onError");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.w("TAG", "onComplete");
//                    }
//                });
    }

    //Single操作 单一操作
    //只发射一条单一的数据，或者一条异常通知，不能发射完成通知，其中数据与通知只能发射一个。
    public void ClickThree(View view) {
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                emitter.onSuccess("成功");
            }
        }).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("TAG", "Disposable");
            }

            @Override
            public void onSuccess(String s) {
                Log.e("TAG", s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", e.toString());
            }
        });
    }

    //Completable操作
    //只发射一条完成通知，或者一条异常通知，不能发射数据，其中完成通知与异常通知只能发射一个
    public void ClickFour(View view) {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                emitter.onComplete();
            }
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("TAG", "onSubscribe");
            }

            @Override
            public void onComplete() {
                Log.e("TAG", "onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "onError" + e.getMessage());
            }
        });
    }

    //Maybe操作
    //Maybe发射单一数据和完成通知
    public void ClickFive(View view) {
        Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> emitter) throws Exception {
                emitter.onError(new Exception("异常测试"));
                //emitter.onSuccess("111");
                //emitter.onComplete();
            }
        }).subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("TAG", "onSubscribe");
            }

            @Override
            public void onSuccess(String s) {
                Log.e("TAG", "onSuccess" + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "onError" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e("TAG", "onComplete");
            }
        });
    }

    //操作符演示
    public void ClickSix(View view) {
        startActivity(new Intent(this, OperatorActivity.class));
    }


}
