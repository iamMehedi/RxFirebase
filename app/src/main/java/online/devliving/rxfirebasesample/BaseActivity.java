package online.devliving.rxfirebasesample;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.Observable;


public class BaseActivity extends RxAppCompatActivity {

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public boolean isProgressShowing(){
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    Observable<DatabaseReference> getUserRef(){
        return Observable.just(FirebaseDatabase.getInstance())
                .map(fireDB -> fireDB.getReference().child("users").child(getUid()));
    }
}
