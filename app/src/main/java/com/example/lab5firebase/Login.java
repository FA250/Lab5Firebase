package com.example.lab5firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth firebaseAuntentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuntentication = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuntentication.getCurrentUser();
        if(currentUser!= null)
            mostrarListaProductos();
    }

    private void mostrarListaProductos() {
        startActivity(new Intent(this,MainActivity.class));
    }

    public void btnIngreso(View view) {
        EditText textCorreo= findViewById(R.id.textCorreoUsuario);
        EditText textContrasenna= findViewById(R.id.textPass);

        String email=textCorreo.getText().toString();
        String password=textContrasenna.getText().toString();

        if(email.isEmpty() || password.isEmpty())
            Toast.makeText(this,"Debe digitar el correo y contraseña", Toast.LENGTH_LONG).show();
        else {
            firebaseAuntentication.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Authentication", "signInWithEmail:success");
                                FirebaseUser user = firebaseAuntentication.getCurrentUser();
                                mostrarListaProductos();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Authentication", "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Correo o contraseña inválidos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
