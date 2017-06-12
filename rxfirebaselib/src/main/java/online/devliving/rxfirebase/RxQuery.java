package online.devliving.rxfirebase;

import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 3/29/17.
 *
 * Observables for FireBase Query i.e StorageReference, DatabaseReference
 */

public final class RxQuery {
    /**
     * use when the reference/query points to an object; if it points to a list the whole list will be emitted as a whole
     * @param query
     * @param clazz
     * @param <T>
     * @return Observable that emits the value of the {@param query} once and completes
     */
    public static <T> Observable<T> observeSingleValue(Query query, Class<T> clazz){
        return observeRefSingle(query)
                .map(new Func1<DataSnapshot, T>() {
                    @Override
                    public T call(DataSnapshot dataSnapshot) {
                        return dataSnapshot.getValue(clazz);
                    }
                });
    }

    /**
     * use when the reference/query points to an object; if it points to a list the whole list will be emitted as a whole
     * @param query
     * @return Observable that emits the value of the {@param query} once and completes
     */
    public static Observable<DataSnapshot> observeRefSingle(Query query){
        return Observable.create(new Observable.OnSubscribe<DataSnapshot>() {
            @Override
            public void call(Subscriber<? super DataSnapshot> subscriber) {
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if(!subscriber.isUnsubscribed()){
                                subscriber.onNext(dataSnapshot);
                                subscriber.onCompleted();
                            }
                        }catch (Exception e) {
                            sendError(e);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        sendError(databaseError.toException());
                    }

                    void sendError(Throwable error){
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onError(error);
                        }
                    }
                };

                query.addListenerForSingleValueEvent(listener);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        query.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    /**
     * use when the reference/query points to an object; if it points to a list the whole list will be emitted as a whole
     * @param query
     * @return Observable that emits the value of the {@param query} once initially and then every time the value changes
     */
    public static Observable<DataSnapshot> observeRef(Query query){
        return Observable.create(new Observable.OnSubscribe<DataSnapshot>() {
            @Override
            public void call(Subscriber<? super DataSnapshot> subscriber) {
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if(!subscriber.isUnsubscribed()){
                                subscriber.onNext(dataSnapshot);
                            }
                        }catch (Exception e) {
                            sendError(e);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        sendError(databaseError.toException());
                    }

                    void sendError(Throwable error){
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onError(error);
                        }
                    }
                };

                query.addValueEventListener(listener);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        query.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    /**
     * use when the reference/query points to an object; if it points to a list the whole list will be emitted as a whole
     * @param query
     * @param clazz
     * @param <T>
     * @return Observable that emits the value of the {@param query} once initially and then every time the value changes
     */
    public static <T> Observable<T> observeValue(Query query, Class<T> clazz){
        return observeRef(query)
                .map(new Func1<DataSnapshot, T>() {
                    @Override
                    public T call(DataSnapshot dataSnapshot) {
                        return dataSnapshot.getValue(clazz);
                    }
                });
    }

    /**
     * use when the reference/query points to a list, items in the list are flattened
     * @param query
     * @param clazz
     * @param <T>
     * @return Observable that emits the values of the {@param query} once initially and then every time there is a value change
     */
    public static <T> Observable<T> observeValues(Query query, Class<T> clazz){
        return observeRef(query)
                .flatMap(new Func1<DataSnapshot, Observable<DataSnapshot>>() {
                    @Override
                    public Observable<DataSnapshot> call(DataSnapshot dataSnapshot) {
                        return Observable.from(dataSnapshot.getChildren());
                    }
                })
                .map(new Func1<DataSnapshot, T>() {
                    @Override
                    public T call(DataSnapshot dataSnapshot) {
                        return dataSnapshot.getValue(clazz);
                    }
                });
    }

    /**
     * use when the reference/query points to a list, items in the list are flattened
     * @param query
     * @param clazz
     * @param <T>
     * @return Observable that emits the values of the {@param query} once and completes
     */
    public static <T> Observable<T> observeValuesSingle(Query query, Class<T> clazz){
        return observeRefSingle(query)
                .flatMap(new Func1<DataSnapshot, Observable<DataSnapshot>>() {
                    @Override
                    public Observable<DataSnapshot> call(DataSnapshot dataSnapshot) {
                        return Observable.from(dataSnapshot.getChildren());
                    }
                })
                .map(new Func1<DataSnapshot, T>() {
                    @Override
                    public T call(DataSnapshot dataSnapshot) {
                        return dataSnapshot.getValue(clazz);
                    }
                });
    }

    /**
     * Observe child value change events. Check {@see FIRChildEvent.type} for the type of event.
     * @param query
     * @return
     */
    public static Observable<FIRChildEvent<DataSnapshot>> observeChild(Query query) {
        return Observable.create(new Observable.OnSubscribe<FIRChildEvent<DataSnapshot>>() {
            @Override
            public void call(Subscriber<? super FIRChildEvent<DataSnapshot>> subscriber) {
                ChildEventListener eventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onNext(new FIRChildEvent<DataSnapshot>(dataSnapshot, s, FIRChildEvent.ChildEventType.ADD));
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onNext(new FIRChildEvent<DataSnapshot>(dataSnapshot, s, FIRChildEvent.ChildEventType.CHANGE));
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onNext(new FIRChildEvent<DataSnapshot>(dataSnapshot, null, FIRChildEvent.ChildEventType.REMOVE));
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onNext(new FIRChildEvent<DataSnapshot>(dataSnapshot, s, FIRChildEvent.ChildEventType.MOVE));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if(!subscriber.isUnsubscribed()){
                            subscriber.onError(databaseError.toException());
                        }
                    }
                };

                query.addChildEventListener(eventListener);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        query.removeEventListener(eventListener);
                    }
                }));
            }
        });
    }
    /**
     * Observe child value change events. Check {@see FIRChildEvent.type} for the type of event.
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Observable<FIRChildEvent<T>> observeChildValue(Query query, Class<T> clazz){
        return observeChild(query)
                .map(new Func1<FIRChildEvent<DataSnapshot>, FIRChildEvent<T>>() {
                    @Override
                    public FIRChildEvent<T> call(FIRChildEvent<DataSnapshot> event) {
                        return new FIRChildEvent<T>(event.value.getValue(clazz), event.childName, event.type);
                    }
                });
    }

    /**
     * Defines a child value change event
     * @param <T>
     */
    public static class FIRChildEvent<T>{
        /**
         * Types of value change event
         */
        public enum ChildEventType{
            ADD,
            REMOVE,
            CHANGE,
            MOVE
        };

        /**
         * Changed value
         */
        T value;
        /**
         * key for the child
         */
        @Nullable String childName;
        /**
         * Type of the event
         */
        ChildEventType type;

        public FIRChildEvent(T value, String childName, ChildEventType type) {
            this.value = value;
            this.childName = childName;
            this.type = type;
        }

        public T getValue() {
            return value;
        }

        @Nullable
        public String getChildName() {
            return childName;
        }

        public ChildEventType getType() {
            return type;
        }
    }
}
