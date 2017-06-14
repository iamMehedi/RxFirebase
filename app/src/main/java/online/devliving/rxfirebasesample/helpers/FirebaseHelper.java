package online.devliving.rxfirebasesample.helpers;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

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

    public static Observable<StorageReference> getUserPicRef(){
        return Observable.just(FirebaseStorage.getInstance().getReference().child("users").child(getUid()).child("propic.jpg"));
    }

    // upload the pic to storage, save the link to database, update user profile
    public static Observable<Void> changeProfilePhoto(Uri fileUri){
        return getUserPicRef()
                .flatMap(ref -> RxGMSTask.just(ref.putFile(fileUri, getImageMetadata())))
                .flatMap(taskResult -> getUserRef()
                            .map(userRef -> userRef.child("photo_url"))
                            .flatMap(picRef -> RxGMSTask.just(picRef.setValue(taskResult.getDownloadUrl().toString())))
                            .map(done -> taskResult.getDownloadUrl())
                )
                .flatMap(uri -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();
                    return RxGMSTask.just(user.updateProfile(request));
                });
    }

    private static String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private static StorageMetadata getImageMetadata(){
        return new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
    }
}
