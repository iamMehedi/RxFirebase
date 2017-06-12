package online.devliving.rxfirebasesample;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;


public class BaseActivity extends RxAppCompatActivity {

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        showProgressDialog("loading...");
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(message);
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
}
