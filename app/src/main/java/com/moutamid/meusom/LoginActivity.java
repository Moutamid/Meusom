package com.moutamid.meusom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Context context = LoginActivity.this;
    private Utils utils = new Utils();
    private boolean layout_register = true;
    private Button loginButton;
    private EditText emailEditText, passwordEditText;
    private ProgressDialog progressDialog;
    private String emailStr;
    private String passwordStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context,"en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context,"pr");
        }
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        emailEditText = findViewById(R.id.emailEt);
        passwordEditText = findViewById(R.id.passwordEt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing you in...");

        emailEditText.setText("moutamid@gmail.com");
        passwordEditText.setText("123456");

        setHintClickListener();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                checkStatusOfEditTexts();
            }
        });

        findViewById(R.id.login_hint1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_conditions_url)));
                startActivity(browserIntent);
            }
        });
    }

    private void checkStatusOfEditTexts() {

        // Getting strings from edit texts
        emailStr = emailEditText.getText().toString();
        passwordStr = passwordEditText.getText().toString();

        // Checking if Fields are empty or not
        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

            if (layout_register) {
                signUpUserWithNameAndPassword();
            } else {
                signInUserWithNameAndPassword();
            }

            // User Name is Empty
        } else if (TextUtils.isEmpty(emailStr)) {

            progressDialog.dismiss();
            emailEditText.setError("Please provide a emailStr");
            emailEditText.requestFocus();


            // Password is Empty
        } else if (TextUtils.isEmpty(passwordStr)) {

            progressDialog.dismiss();
            passwordEditText.setError("Please provide a passwordStr");
            passwordEditText.requestFocus();


            // Confirm Password is Empty
        }
    }

    private void signInUserWithNameAndPassword() {

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            //if Email Address is Invalid..

            progressDialog.dismiss();
            emailEditText.setError("Email is not valid. Make sure no spaces and special characters are included");
            emailEditText.requestFocus();
        } else {

            Constants.auth().signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        utils.storeString(context, Constants.USER_EMAIL, emailStr);
                        utils.storeString(context, Constants.USER_PASSWORD, passwordStr);

                        progressDialog.dismiss();

                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);

                    } else {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }

    private void signUpUserWithNameAndPassword() {

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            //if Email Address is Invalid..

            progressDialog.dismiss();
            emailEditText.setError("Please enter a valid email with no spaces and special characters included");
            emailEditText.requestFocus();
        } else {

            Constants.auth().createUserWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        utils.storeString(context, Constants.USER_EMAIL, emailStr);
                        utils.storeString(context, Constants.USER_PASSWORD, passwordStr);

                        progressDialog.dismiss();

                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);

                    } else {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setHintClickListener() {
        TextView hintText = findViewById(R.id.login_hint);
        TextView headerText = findViewById(R.id.headerTextPrimary);

        hintText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layout_register) {

                    hintText.setText(getString(R.string.login_signup_hint));
                    headerText.setText("Login");
                    loginButton.setText("LOGIN");

                    layout_register = false;
                } else {

                    hintText.setText(getString(R.string.signup_login_hint));
                    headerText.setText("Register");
                    loginButton.setText("REGISTER");

                    layout_register = true;
                }

            }
        });
    }

}