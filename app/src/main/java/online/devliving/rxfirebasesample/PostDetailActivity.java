package online.devliving.rxfirebasesample;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import online.devliving.rxfirebase.RxGMSTask;
import online.devliving.rxfirebase.RxQuery;
import online.devliving.rxfirebasesample.helpers.FirebaseHelper;
import online.devliving.rxfirebasesample.models.Comment;
import online.devliving.rxfirebasesample.models.Post;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);

        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_author);
        mTitleView = (TextView) findViewById(R.id.post_title);
        mBodyView = (TextView) findViewById(R.id.post_body);
        mCommentField = (EditText) findViewById(R.id.field_comment_text);
        mCommentButton = (Button) findViewById(R.id.button_post_comment);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        //load post data
        RxQuery.observeValue(mPostReference, Post.class)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(post -> {
                    mAuthorView.setText(post.author);
                    mTitleView.setText(post.title);
                    mBodyView.setText(post.body);
                },
                    error -> showToast("Error: " + error.getMessage())
                );

        // Listen for comments
        mAdapter.startObservingComments();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
    }

    private void postComment() {
        String commentText = mCommentField.getText().toString();
        showProgressDialog("posting...");
        FirebaseHelper.getUser()
                .flatMap(user -> {
                    if(user == null) return Observable.error(new DatabaseException("user not found"));

                    String uid = FirebaseHelper.getUid();
                    Comment comment = new Comment(uid, user.username, commentText);

                    // Push the comment, it will appear in the list
                    return RxGMSTask.just(mCommentsReference.push().setValue(comment));
                })
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(done -> mCommentField.setText(null),
                        error -> {
                            hideProgressDialog();
                            showToast("Error: " + error.getMessage());
                        },
                        () -> hideProgressDialog());
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = (TextView) itemView.findViewById(R.id.comment_author);
            bodyView = (TextView) itemView.findViewById(R.id.comment_body);
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private BaseActivity mActivity;
        private DatabaseReference mDatabaseReference;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final BaseActivity activity, DatabaseReference ref) {
            mActivity = activity;
            mDatabaseReference = ref;
        }

        public void startObservingComments(){
            RxQuery.observeChild(mDatabaseReference)
                    .compose(mActivity.bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> handleEvent(event),
                            error -> mActivity.showToast("Error: " + error.getMessage()));
        }

        private void handleEvent(RxQuery.FIRChildEvent<DataSnapshot> event){
            switch (event.getType()){
                case ADD:
                    // A new comment has been added, add it to the displayed list
                    Comment comment = event.getValue().getValue(Comment.class);

                    // Update RecyclerView
                    mCommentIds.add(event.getValue().getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    break;

                case CHANGE:
                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = event.getValue().getValue(Comment.class);
                    String commentKey = event.getValue().getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    break;

                case REMOVE:
                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    commentKey = event.getValue().getKey();

                    // [START_EXCLUDE]
                    commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    break;
            }
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

    }
}
