package online.devliving.rxfirebasesample.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import online.devliving.rxfirebase.RxGMSTask;
import online.devliving.rxfirebase.RxQuery;
import online.devliving.rxfirebasesample.models.User;
import rx.Observable;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 6/12/17.
 */

public final class FirebaseHelper {
    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static Observable<DatabaseReference> getUserRef(){
        return Observable.just(FirebaseDatabase.getInstance())
                .map(fireDB -> fireDB.getReference().child("users").child(getUid()));
    }

    public static Observable<Void> saveUser(FirebaseUser user){
        return getUserRef()
                .flatMap(userRef -> {
                    String username = usernameFromEmail(user.getEmail());
                    User dbUser = new User(username, user.getEmail());

                    return RxGMSTask.just(userRef.setValue(dbUser));
                });
    }

    public static Observable<User> getUser(){
        return getUserRef()
                .flatMap(userRef -> RxQuery.observeSingleValue(userRef, User.class));
    }

    private static String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}
