package amit.example.lilichatians;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends Activity {

    EditText email;
    EditText password;
    Button login;
    Button signup_login;
    String emailData;
    String passwordData;
    TextView forgotPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

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
        login = findViewById(R.id.login_button);
        signup_login = findViewById(R.id.signup_login);
        progressBar = findViewById(R.id.progressbar_toLogin);
        forgotPassword  = findViewById(R.id.forgotpasswordlink);
        progressBar.setAlpha(0);
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailData = email.getText().toString().trim();
                passwordData = password.getText().toString().trim();
                check_login_status();
            }
        });

        //change activity to main
        signup_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changetoSignup();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser()!= null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(LoginActivity.this, indexActivity.class));
                finish();
            }
        }
    }

    public void check_login_status()
    {
        if(emailData.length()>5 && passwordData.length()>5) {
            progressBar.setAlpha(1);
            mAuth.signInWithEmailAndPassword(emailData, passwordData)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
                                    mRef.child(mAuth.getUid()).child("email").setValue(emailData);
                                    startActivity(new Intent(LoginActivity.this,indexActivity.class));
                                    finish();

                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Wrong Email Or Password Or Verify Your Email", Toast.LENGTH_LONG).show();
                                progressBar.setAlpha(0);
                            }
                        }
                    });
        }
        else{
            Toast.makeText(LoginActivity.this, "No Account Exists..", Toast.LENGTH_SHORT).show();
        }
    }

    public void changetoSignup()
    {
        startActivity(new Intent(LoginActivity.this,SignupActivity.class));
        finish();
    }
}
