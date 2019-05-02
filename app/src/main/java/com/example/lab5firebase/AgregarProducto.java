package com.example.lab5firebase;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AgregarProducto extends AppCompatActivity {

    FirebaseUser usuarioActual;

    final int PERMISSION_REQUEST_CODE = 4565;
    final int SELECT_PICTURE = 7895;

    String path_imagen;
    boolean subirFoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        usuarioActual = firebaseAuth.getCurrentUser();
    }

    public void btgAgregarImagen(View view) {
        ElegirImagen();
    }

    // ------------------ Elegir imagen de usuario para mostrar ----------
    public void ElegirImagen() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            } else {
                requestPermission();
            }
        } else {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                Uri filePath = data.getData();
                if (null != filePath) {
                    try {
                        ImageView imgUsuario = findViewById(R.id.imgProducto);
                        imgUsuario.setBackground(null);
                        imgUsuario.setImageURI(filePath);
                        path_imagen = getFilePath(this, filePath);
                        subirFoto = true;
                        Log.d("PATH", filePath.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void btnAgregarProducto(View view) {
        EditText textNombre = findViewById(R.id.textNombreProducto);
        EditText textPrecio = findViewById(R.id.textPrecio);
        EditText textDescripcion = findViewById(R.id.textDescripcion);

        String nombreProducto = textNombre.getText().toString();
        String descripcionProducto = textDescripcion.getText().toString();

        if (nombreProducto.isEmpty() || textPrecio.getText().toString().isEmpty() || descripcionProducto.isEmpty())
            Toast.makeText(AgregarProducto.this, "Debe completar todos los campos", Toast.LENGTH_LONG).show();
        else {
            int precioProducto = Integer.parseInt(textPrecio.getText().toString());
            subirDatos(nombreProducto, precioProducto, descripcionProducto);
        }
    }

    private void subirDatos(final String nombreProducto, int precioProducto, String descripcionProducto) {
        final Map<String, Object> producto = new HashMap<>();
        producto.put("Nombre", nombreProducto);
        producto.put("Precio", precioProducto);
        producto.put("Descripcion", descripcionProducto);


        nombreRepetidoBD(nombreProducto).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.contains("Nombre")) {
                    new AlertDialog.Builder(AgregarProducto.this)
                            .setTitle("Producto repetido")
                            .setMessage("Ya existe un producto con el nombre " + nombreProducto + ", ¿Desea modificarlo con los datos actuales?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    subirDatosAux(producto);
                                }
                            })
                            .setNegativeButton("No",null)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();//*/
                }
                else
                    subirDatosAux(producto);
            }
        });
    }

    private void subirDatosAux(Map<String, Object> producto) {
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        if (subirFoto) {
            String pathImagenDB = "usuarios/" + usuarioActual.getEmail() + "/imagenesProductos/" + producto.get("Nombre").toString() + ".jpg";
            Uri uri = Uri.fromFile(new File(path_imagen));
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference imagenProductoRef = storageReference.child(pathImagenDB);

            imagenProductoRef.putFile(uri);

            producto.put("Imagen", pathImagenDB);
        } else
            producto.put("Imagen", null);

        firebaseDB.collection("Usuarios")
                .document(usuarioActual.getEmail())
                .collection("Productos")
                .document(producto.get("Nombre").toString()).set(producto)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Se ha creado el producto correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al agregar el nuevo producto", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private Task<DocumentSnapshot> nombreRepetidoBD(final String nombreProducto) {
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

       return firebaseDB.collection("Usuarios")
                        .document(usuarioActual.getEmail())
                        .collection("Productos").document(nombreProducto).get();
    }
}
