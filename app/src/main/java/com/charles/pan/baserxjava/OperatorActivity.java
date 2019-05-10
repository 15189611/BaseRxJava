package com.charles.pan.baserxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by 17111980 on 2019/5/8.
 */

public class OperatorActivity extends AppCompatActivity {
    private static final String TAG = OperatorActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        //map 转换操作
        mapMethod();
        //flatMap
        flatMapMethod();
        //ConcatMap
        concatMapMethod();
        //merge
        mergeMethod();
        //zip
        zipMethod();
        //concat
        concatMethod();
    }


    //组合多个被观察者一起发送数据，合并后 按时间线并行执行
    private void mergeMethod() {
        Observable.merge(Observable.just(1, 2, 3), Observable.just(3, 5, 6))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("mergeMethod", integer + "");
                    }
                });
    }

    //利用 concat 的必须调用 onComplete 后才能订阅下一个 Observable 的特性，
    // 我们就可以先读取缓存数据，倘若获取到的缓存数据不是我们想要的，
    // 再调用 onComplete() 以执行获取网络数据的 Observable，如果缓存数据能应我们所需，
    // 则直接调用 onNext()，防止过度的网络请求，浪费用户的流量。
    private void concatMethod() {
        //对于单一的把两个发射器连接成一个发射器，虽然 zip 不能完成，
        //但我们还是可以自力更生，官方提供的 concat 让我们的问题得到了完美解决。
        Observable.concat(Observable.just(1, 2, 3), Observable.just(4, 5, 6))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("concatMethod", "concat : " + integer + "\n");
                    }
                });
    }

    private void zipMethod() {
        //zip 组合事件的过程就是分别从发射器 A 和发射器 B 各取出一个事件来组合，并且一个事件只能被使用一次，
        //组合的顺序是严格按照事件发送的顺序来进行的，所以上面截图中，可以看到，1 永远是和 A 结合的，2 永远是和 B 结合的。
        Observable<String> stringObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                if (!emitter.isDisposed()) {
                    emitter.onNext("A");
                    Log.e("zipMethod", "String emit : A \n");
                    emitter.onNext("B");
                    Log.e("zipMethod", "String emit : B \n");
                    emitter.onNext("C");
                    Log.e("zipMethod", "String emit : C \n");
                }
            }
        });
        Observable<Integer> integerObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                if (!emitter.isDisposed()) {
                    emitter.onNext(1);
                    Log.e("zipMethod", "Integer emit : 1 \n");
                    emitter.onNext(2);
                    Log.e("zipMethod", "Integer emit : 2 \n");
                    emitter.onNext(3);
                    Log.e("zipMethod", "Integer emit : 3 \n");
                    emitter.onNext(4);
                    Log.e("zipMethod", "Integer emit : 4 \n");
                    emitter.onNext(5);
                    Log.e("zipMethod", "Integer emit : 5 \n");
                }
            }
        });
        Observable.zip(integerObservable, stringObservable, new BiFunction<Integer, String, Object>() {
            @Override
            public Object apply(Integer integer, String s) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                Log.e("zipMethod", o.toString());
            }
        });
    }

    //能保证顺序
    private void concatMapMethod() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                Log.e("concatMapMethod", o.toString());
            }
        });
    }

    //flatMap 并不能保证事件的顺序，如果需要保证，需要用到我们下面要讲的 ConcatMap。
    private void flatMapMethod() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                Log.e("flatMapMethod", o.toString());
            }
        });
    }

    //注意它和 concat 的区别在于，不用等到 发射器 A 发送完所有的事件再进行发射器 B 的发送。
    private void mapMethod() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "mapMethod: This is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("mapMethod", "accept : " + s + "\n");
            }
        });
    }

}
