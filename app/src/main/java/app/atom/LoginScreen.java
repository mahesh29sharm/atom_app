package app.atom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LoginScreen extends AppCompatActivity {
    int RC_SIGN_IN = 0;
    GoogleSignInClient signInClient;
    SignInButton button;
    String base_url;
    private FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
    ProgressBar progressBar;
    Button google_sign_in, guest_sign_in;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    ArrayList<User> userList = new ArrayList<>();
    User userData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        progressBar = findViewById(R.id.progress_bar);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        google_sign_in = findViewById(R.id.google_sign_in);
        guest_sign_in = findViewById(R.id.guest_sign_in);


        google_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        guest_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        editor = getSharedPreferences("user",MODE_PRIVATE).edit();
        pref = getSharedPreferences("user",MODE_PRIVATE);




    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

                Log.w(TAG, "Google sign in failed", e);

                updateUI(null);

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }


                        hideProgressBar();

                    }
                });
    }
    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {

            if(pref.contains("users")) {
                int index = 0;
                boolean matched = false;
                userList = new Gson().fromJson(pref.getString("users",""),  new TypeToken<ArrayList<User>>()
                {}.getType());
                for(int i = 0; i< Objects.requireNonNull(userList).size(); i++){

                User user_data = userList.get(i);
                if(user_data.getUserEmail().equals(user.getEmail())){
                    matched = true;
                    index = i;
                }


                }
                if(matched) {
                    Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                    intent.putExtra("email", userList.get(index).getUserEmail());
                    intent.putExtra("name", userList.get(index).getUserName());
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(LoginScreen.this, NameScreen.class);
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("name", user.getDisplayName());
                    startActivity(intent);
                }

            }
            else {
                Intent intent = new Intent(LoginScreen.this, NameScreen.class);
                intent.putExtra("email", user.getEmail());
                intent.putExtra("name", user.getDisplayName());
                startActivity(intent);
            }



        } else {
            Log.d("status","logged out");
        }
    }
    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

}