package com.partypopper.app.features.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.partypopper.app.R;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.utils.BaseActivity;

public class AuthenticationActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        configureGoogleSignIn();

        mAuth = FirebaseAuth.getInstance();

        initializeUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.auth_email_login_button:
                signInWithEmail();
                break;
            case R.id.auth_google_login_button:
                singInWithGoogle();
                break;
            case R.id.auth_register_button:
                registerNewUser();
                break;
            case R.id.auth_forgot_password_button:
                resetPassword();
                break;
        }
    }

    private void signInWithEmail() {
        String email, password;

        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            showText("Please enter email...");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showText("Please enter password!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showText("Login successful!");

                            Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            showText("Login failed! Please try again later");
                        }
                    }
                });
    }

    private void registerNewUser() {
        String email, password;

        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            showText("Please enter email...");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showText("Please enter password!");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showText("Registration successful!");
                            showProgressDialog("Logge ein ...");

                            Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        } else {
                            showText("Registration failed! Please try again later");
                            hideProgressDialog();
                        }
                    }
                });
    }

    private void resetPassword() {
        String email = emailInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            showText("Please enter email...");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showText("Email sent.");
                            Log.d(TAG, "Email sent.");
                        } else {
                            showText("Something went wrong!");
                        }
                    }
                });
    }

    private void singInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.auth_email_input);
        passwordInput = findViewById(R.id.auth_password_input);

        findViewById(R.id.auth_email_login_button).setOnClickListener(this);
        findViewById(R.id.auth_register_button).setOnClickListener(this);
        findViewById(R.id.auth_forgot_password_button).setOnClickListener(this);
        findViewById(R.id.auth_google_login_button).setOnClickListener(this);

    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (NullPointerException | ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog("Logge ein ...");
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showText("Authentication Failed.");
                        }
                        hideProgressDialog();
                    }
                });
    }
}
