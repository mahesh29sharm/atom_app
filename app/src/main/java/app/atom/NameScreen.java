package app.atom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.ArrayList;

public class NameScreen extends AppCompatActivity {

    ArrayList<User> userList = new ArrayList<>();
    User userData = new User();
    EditText user_name;
    Button next;
    SharedPreferences.Editor editor;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_screen);
        user_name = findViewById(R.id.user_name);
        next = findViewById(R.id.next);
        editor = getSharedPreferences("user",MODE_PRIVATE).edit();
        pref = getSharedPreferences("user",MODE_PRIVATE);

        user_name.setText(getIntent().getStringExtra("name"));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = user_name.getText().toString();
                userData.setUserEmail(getIntent().getStringExtra("email"));
                userData.setUserName(userName);
                userList.add(userData);
                if(pref.contains("users")){
                    editor.remove("users");
                }
                String users  = new Gson().toJson(userList);
                editor.putString("users",users);
                editor.apply();

                Intent intent = new Intent(NameScreen.this, MainActivity.class);
                intent.putExtra("email", getIntent().getStringExtra("email"));
                intent.putExtra("name", userName);
                startActivity(intent);

            }
        });

    }
}