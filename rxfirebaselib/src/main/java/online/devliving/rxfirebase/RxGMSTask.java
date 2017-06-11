package online.devliving.rxfirebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 2/1/17.
 *
 * Create Observables for {@see com.google.android.gms.tasks.Task}
 */

public final class RxGMSTask {

    /**
     * returns Observable that emits the result of {@param task}
     * @param task
     * @param nullable if false then NULL value is not emitted
     * @param <T>
     * @return
     */
    static <T> Observable<T> just(Task<T> task, boolean nullable){
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> sub) {
                task.continueWith(new Continuation<T, T>() {

                    @Override
                    public T then(@NonNull Task<T> t) throws Exception {
                        if(sub.isUnsubscribed()) return null;

                        if(t.isSuccessful()){
                            T  result = t.getResult();
                            if(result != null || nullable) sub.onNext(result);

                            sub.onCompleted();
                        }
                        else{
                            sub.onError(task.getException());
                        }

                        return null;
                    }
                });
            }
        });
    }

    /**
     * returns Observable that emits the result of {@param task}, might emit null
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Observable<T> just(Task<T> task){
        return just(task, true);
    }

    /**
     * returns Observable that emits the result of {@param task}, null value is not emitted
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Observable<T> justNonNullable(Task<T> task){
        return just(task, false);
    }

    /**
     * returns Observable that emits the result of {@param task}, might emit null
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Observable<T> defer(Task<T> task){
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return just(task);
            }
        });
    }

    /**
     * returns Observable that emits the result of {@param task}, null value is not emitted
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Observable<T> deferNonNullable(Task<T> task){
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return justNonNullable(task);
            }
        });
    }
}
