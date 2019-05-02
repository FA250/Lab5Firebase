package com.example.lab5firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CustomListProductos extends ArrayAdapter {
    private final Activity context;
    List nombreProductos,precioProductos,descripcionProductos,pathImgProductos;
    StorageReference firebaseStorage;



    public CustomListProductos(Activity context, List<String> nombreProductos, List<String> precioProductos, List<String> descripcionProductos, List<String> pathImgProductos) {
        super(context, R.layout.custom_item_producto, nombreProductos);
        this.context = context;
        this.nombreProductos = nombreProductos;
        this.precioProductos = precioProductos;
        this.descripcionProductos = descripcionProductos;
        this.pathImgProductos = pathImgProductos;
        firebaseStorage= FirebaseStorage.getInstance().getReference();
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.custom_item_producto, null, true);

        final TextView lbNombre = (TextView) rowView.findViewById(R.id.lbNombreProducto);
        TextView lbPrecio = (TextView) rowView.findViewById(R.id.lbPrecio);
        TextView lbDescripcion = (TextView) rowView.findViewById(R.id.lbDescripcion);
        final ImageView imgProducto = (ImageView) rowView.findViewById(R.id.imgProducto);

        lbNombre.setText(nombreProductos.get(position).toString());
        lbPrecio.setText("Precio:"+precioProductos.get(position).toString());
        lbDescripcion.setText(descripcionProductos.get(position).toString());


        if(!pathImgProductos.get(position).toString().isEmpty()) {
            StorageReference imgProductoRef = firebaseStorage.child(pathImgProductos.get(position).toString());

            try {
                final File fileImgProducto = File.createTempFile("images", "jpg");
                imgProductoRef.getFile(fileImgProducto)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Uri uri = Uri.fromFile(fileImgProducto);
                                imgProducto.setBackground(null);
                                imgProducto.setImageURI(uri);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Eliminar producto")
                        .setMessage("Desea eliminar el producto "+lbNombre.getText().toString()+"?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                eliminarProducto(lbNombre.getText().toString());
                            }
                        })
                        .setNegativeButton("No",null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();//*/
                return true;
            }
        });

        return rowView;
    }

    private void eliminarProducto(String nombreProducto) {
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        firebaseDB.collection("Usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("Productos").document(nombreProducto).delete();

        getContext().startActivity(new Intent(context,MainActivity.class));
        context.finish();
    }


}
