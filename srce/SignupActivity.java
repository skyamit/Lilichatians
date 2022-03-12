package amit.example.lilichatians;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignupActivity extends Activity {

    EditText email;
    EditText password;
    Button signup;
    Button signuptoLogin;
    String emailData;
    String passwordData;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        AdView adView = findViewById(R.id.BannerAds1);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener(){

        });

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.signup_button);
        signuptoLogin = findViewById(R.id.signup_login);
        progressBar = findViewById(R.id.progressbar_Signup);
        progressBar.setAlpha(0);

        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailData = email.getText().toString().trim();
                passwordData = password.getText().toString().trim();
                Tosignup();
            }
        });

        signuptoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changetoLogin();
            }
        });
    }
    public void Tosignup()
    {
        if(emailData.length()>5 && passwordData.length()>5) {
            progressBar.setAlpha(1);
            mAuth.createUserWithEmailAndPassword(emailData, passwordData)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification();
                                mAuth.getInstance().signOut();
                                Toast.makeText(SignupActivity.this, "Account Created... verify your Account and Enjoy", Toast.LENGTH_LONG).show();
                                progressBar.setAlpha(0);

                            } else {
                                Toast.makeText(SignupActivity.this, "Your Account Exists..", Toast.LENGTH_LONG).show();
                                progressBar.setAlpha(0);

                            }
                        }
                    });
        }
        else{
            Toast.makeText(SignupActivity.this, "Password must be of 6 or More digits..", Toast.LENGTH_SHORT).show();
            progressBar.setAlpha(0);
        }
    }
    public void changetoLogin()
    {
        startActivity(new Intent(SignupActivity.this,LoginActivity.class));
        finish();
    }

}
