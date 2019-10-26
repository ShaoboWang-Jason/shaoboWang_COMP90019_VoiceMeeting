//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.comp90025.util.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private Button btnLogin, btnReturn;
    public static String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        FirebaseUser usr = auth.getCurrentUser();
        if (usr != null) {
            auth.signOut();
            FirebaseUser usr1 = auth.getCurrentUser();
            if (usr1 == null){
                System.out.println("signout successful");
            }else{
                System.out.println("signout fail");
            }
        }else{
            System.out.println("No account exist");
        }
        setContentView(R.layout.activity_login);
        inputEmail =  findViewById(R.id.logid);
        inputPassword =  findViewById(R.id.logpsw);
        btnLogin =  findViewById(R.id.signin);
        btnReturn = findViewById(R.id.log_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            if(password.length()<6){
                                inputPassword.setError("Password too short,minimum 6 characters");
                            }else{
                                Toast.makeText(LoginActivity.this, "Authentication failed, check your email and password or sign up",Toast.LENGTH_LONG).show();

                            }
                        }else{
                            // if login successfully, send username and password to webserver
                            System.out.println("Login successful");
                            JSONArray userJson = new JSONArray();
                            userJson.put(email);
                            userJson.put(password);
                            String userName = userJson.toString();
                            Logger.e(userName);
                            upload(userName);
                            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    // use http post method to send json to webserver
    private void upload(String Json) {
        StrictMode.ThreadPolicy p = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(p);
        String path = "http://35.189.40.211:8889/getUser";
        HttpURLConnection c = null;
        try {
            URL url = new URL(path);
            c = (HttpURLConnection)url.openConnection();
            c.setDoOutput(true);
            c.setDoInput(true);
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setConnectTimeout(60 * 1000);
            c.setReadTimeout(60 * 1000);
            c.setRequestProperty("Charsert", "UTF-8");
            c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            c.setRequestProperty("Connection", "Keep-Alive");
            c.setRequestProperty("logType", "base");
            c.connect();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), "UTF-8"));
            writer.write(Json);
            System.out.println("Success");
            writer.close();
            int responseCode = c.getResponseCode();
            Logger.e("Transferred: " + responseCode + " *** " + c.getResponseMessage() + String.valueOf(Json));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }

    }

}
