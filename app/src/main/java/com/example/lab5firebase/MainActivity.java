package com.example.lab5firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usuario= firebaseAuth.getCurrentUser();

        if(usuario!=null)
            Snackbar.make(findViewById(R.id.actividad_principal), "Ha ingresado como "+usuario.getEmail() , Snackbar.LENGTH_LONG).show();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AgregarProducto.class));
            }
        });

        cargarProductos();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        cargarProductos();
    }

    private void cargarProductos() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final TextView lbNoProductos=findViewById(R.id.lbNoProductos);
        firebaseFirestore.collection("Usuarios").document(firebaseAuth.getCurrentUser().getEmail()).collection("Productos").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            lbNoProductos.setVisibility(View.VISIBLE);
                        } else {
                            lbNoProductos.setVisibility(View.INVISIBLE);

                            List<String> nombreProductos = new ArrayList();
                            List<String> precioProductos = new ArrayList();
                            List<String> descripcionProductos = new ArrayList();
                            List<String> pathImgProductos = new ArrayList();

                            for(QueryDocumentSnapshot documentSnapshot : documentSnapshots){
                                nombreProductos.add(documentSnapshot.getString("Nombre"));
                                precioProductos.add(String.valueOf(documentSnapshot.get("Precio")));
                                descripcionProductos.add(documentSnapshot.getString("Descripcion"));
                                if(documentSnapshot.get("Imagen")!=null)
                                    pathImgProductos.add(documentSnapshot.getString("Imagen"));
                                else
                                    pathImgProductos.add("");

                            }

                            ListView listViewProductos = findViewById(R.id.lstProductos);

                            CustomListProductos customListProductos= new CustomListProductos(MainActivity.this, nombreProductos, precioProductos,descripcionProductos,pathImgProductos);

                            listViewProductos.setAdapter(customListProductos);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        lbNoProductos.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this,"Error: no se pudo obtener la lista de productos",Toast.LENGTH_LONG).show();
                    }
                });
    }



    //------- Agregar menu de opciones --------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logOut: {
                cerrarSesion();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void cerrarSesion() {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            finish();
                        }
                    });

    }
}
