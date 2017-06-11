package online.devliving.rxfirebasesample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import online.devliving.rxfirebase.RxGMSTask;
import online.devliving.rxfirebase.RxQuery;
import online.devliving.rxfirebasesample.models.Post;
import online.devliving.rxfirebasesample.models.User;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);
        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_post);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        showProgressDialog("posting...");

        getUser()
                .flatMap(user -> {
                    if(user == null) return Observable.error(new DatabaseException("user not found"));
                    else {
                        String uid = getUid();
                        return writeNewPost(uid, user.username, title, body);
                    }
                })
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(done -> finish(),
                        error -> {
                            hideProgressDialog();
                            showToast("Error: " + error.getMessage());
                            setEditingEnabled(true);
                        },
                        () -> {
                            hideProgressDialog();
                            setEditingEnabled(true);
                        });
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private Observable<User> getUser(){
        return getUserRef()
                .flatMap(userRef -> RxQuery.observeSingleValue(userRef, User.class));
    }

    private Observable<Map<String, Object>> buildPostData(String userId, String username, String title, String body){
        return Observable.fromCallable(() -> {
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            String key = mDatabase.child("posts").push().getKey();
            Post post = new Post(userId, username, title, body);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

            return childUpdates;
        });
    }
    // [START write_fan_out]
    private Observable<Void> writeNewPost(String userId, String username, String title, String body) {
        return buildPostData(userId, username, title, body)
                .flatMap(dataMap -> RxGMSTask.just(mDatabase.updateChildren(dataMap)));
    }
    // [END write_fan_out]
}
