package com.mustafaunlu.hisplustoryproject.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mustafaunlu.hisplustoryproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user= auth.getCurrentUser();
        if(user !=null){
            Intent intent=new Intent(MainActivity.this,FeedActivity.class);
            startActivity(intent);

        }

    }

    public void signIn(View view) {
        String email = binding.usernameEditText.getText().toString();
        String sifre = binding.passwordEditText.getText().toString();

        auth.signInWithEmailAndPassword(email, sifre).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void signUp(View view) {
        String email = binding.usernameEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_LONG).show();
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                    startActivity(intent);
                    //bu activity i kapatmak için finish yazdık boşuna yer kaplamasın diye
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}