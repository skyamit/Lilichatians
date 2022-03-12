package amit.example.lilichatians;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends Activity {

    Button submit;
    EditText email;
    Button login_forgot;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    String emailData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

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

        submit = findViewById(R.id.submit);
        email = findViewById(R.id.email);
        login_forgot = findViewById(R.id.login_forgot);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setAlpha(0);

        mAuth = FirebaseAuth.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailData = email.getText().toString().trim();
                if(emailData.length()>5)
                {
                    progressBar.setAlpha(1);
                    mAuth.sendPasswordResetEmail(emailData);
                    Toast.makeText(ForgotPasswordActivity.this, "Check your mail to Reset Password!!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setAlpha(0);
            }
        });

        login_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
                finish();
            }
        });

    }
}
