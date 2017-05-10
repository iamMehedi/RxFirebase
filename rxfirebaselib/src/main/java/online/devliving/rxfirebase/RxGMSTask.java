package online.devliving.rxfirebase;

import com.google.android.gms.tasks.Task;

import rx.Observable;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 2/1/17.
 */

public final class RxGMSTask {
    static <T> Observable<T> just(Task<T> task, boolean nullable){
        return Observable.create(sub -> {
            task.continueWith(t -> {
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
            });
        });
    }

    /**
     * emissions can be null
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Observable<T> just(Task<T> task){
        return just(task, true);
    }

    public static <T> Observable<T> justNonNullable(Task<T> task){
        return just(task, false);
    }

    public static <T> Observable<T> defer(Task<T> task){
        return Observable.defer(() -> just(task));
    }

    public static <T> Observable<T> deferNonNullable(Task<T> task){
        return Observable.defer(() -> justNonNullable(task));
    }
}
