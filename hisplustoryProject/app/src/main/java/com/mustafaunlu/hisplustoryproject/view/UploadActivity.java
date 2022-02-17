package com.mustafaunlu.hisplustoryproject.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mustafaunlu.hisplustoryproject.R;
import com.mustafaunlu.hisplustoryproject.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ActivityUploadBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissonLauncher;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    Uri imageData;
    Bitmap selectedImage;
    Uri deneme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUploadBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        registerLauncher();

        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();

        storageReference=firebaseStorage.getReference();

        deneme=Uri.parse("R.drawable.file");


    }

    private void registerLauncher() {

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if(intentFromResult !=null){
                        imageData=intentFromResult.getData();

                        try{
                            if(Build.VERSION.SDK_INT>28){
                                ImageDecoder.Source source=ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.selectedImageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage= MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.selectedImageView.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissonLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted -> activityResultLauncher
                    Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                }
                else{
                    //permission denied
                    Toast.makeText(getApplicationContext(), "Permission needed for gallery", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void seal(View view){
        if(imageData !=null){
            UUID uuid=UUID.randomUUID();
            String imageName="images/"+uuid+".jpg";
            System.out.println("deneme: "+imageData);


            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageReference newReference=firebaseStorage.getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl=uri.toString();
                            String comment=binding.commentEditText.getText().toString();
                            FirebaseUser user= auth.getCurrentUser();;
                            String email=user.getEmail();

                            HashMap<String,Object> postData=new HashMap<>();
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent=new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Toast.makeText(UploadActivity.this,"son"+ e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    System.out.println(e.getLocalizedMessage());
                                }
                            });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this,"ilk"+ e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                }
            });

        }

    }

    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //permission launcher
                        permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }
            else{
            //permission launcher
            permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else{
            //go to gallery
            Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }

}