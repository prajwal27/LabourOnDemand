package com.example.labourondemand;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class FormActivity extends CustomerMainActivity {

    private Services services = new Services();

    private EditText description, addressLine1, addressLine2, landmark, city;
    private Button submitButton;
    private ViewPager viewPager;
    private Uri filePath;
    private FloatingActionButton floatingActionButton;
    private FirebaseStorage storage;
    private Uri mainImageURI;
    private ArrayList<Uri> pictures = new ArrayList<>();
    private Customer customer;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private Slide slide;
    private Bitmap compressedImageFile;
    private EditText amount;
    private Button submit;
    private String TAG = FormActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_main_fl);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_form, frameLayout, false);
        drawerLayout.addView(view, 1);*/
        //frameLayout.addView(view);
        setContentView(R.layout.activity_form);

        services.setSkill(getIntent().getExtras().getString("skill"));
        customer = (Customer) getIntent().getExtras().get("customer");
       /* viewPager = view.findViewById(R.id.activity_form_vp);
        floatingActionButton = view.findViewById(R.id.activity_form_fab);
        description = view.findViewById(R.id.activity_form_et_description);
        addressLine1 = view.findViewById(R.id.activity_form_et_address1);
        addressLine2 = view.findViewById(R.id.activity_form_et_address2);
        landmark = view.findViewById(R.id.activity_form_et_landmark);
        city = view.findViewById(R.id.activity_form_et_city);*/
        viewPager = findViewById(R.id.activity_form_vp);
        floatingActionButton = findViewById(R.id.activity_form_fab);
        description = findViewById(R.id.activity_form_et_description);
        addressLine1 = findViewById(R.id.activity_form_et_address1);
        addressLine2 = findViewById(R.id.activity_form_et_address2);
        landmark = findViewById(R.id.activity_form_et_landmark);
        city = findViewById(R.id.activity_form_et_city);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        submit = findViewById(R.id.activity_form_btn_submit);
        amount = findViewById(R.id.activity_form_et_amount);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        slide = new Slide(this, new ArrayList<String>() );
        viewPager.setAdapter(slide);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_description(v);
            }
        });
    }

    public void save_description(View view) {

        String problem_description = description.getText().toString();
        String address_line_1_string = addressLine1.getText().toString();
        String address_line_2_string = addressLine2.getText().toString();
        String landmark_string = landmark.getText().toString();
        String city_string = city.getText().toString();
        String amonut_string = amount.getText().toString();

        if (problem_description.length() == 0 || address_line_1_string.length() == 0 || address_line_2_string.length() == 0 || landmark_string.length() == 0 || city_string.length() == 0 || amonut_string.length() == 0) {
            if (problem_description.length() == 0) {
                description.setError("Please enter a description before submitting");
            }
            if (address_line_1_string.length() == 0) {
                addressLine1.setError("Please enter an address before submitting");
            }
            if (address_line_2_string.length() == 0) {
                addressLine2.setError("Please enter an address before submitting");
            }
            if (landmark_string.length() == 0) {
                landmark.setError("Please enter a landmark before submitting");
            }
            if (city_string.length() == 0) {
                city.setError("Please enter a city before submitting");
            }
            if (amonut_string.length() == 0) {
                city.setError("Please enter amount before submitting");
            }
        } else {
            services.setServiceID(firebaseAuth.getUid()+"+"+String.valueOf(System.currentTimeMillis()));
            services.setSkill(getIntent().getExtras().getString("skill"));
            services.setA1(address_line_1_string);
            services.setA2(address_line_2_string);
            services.setDescription(problem_description);
            services.setCity(city_string);
            services.setLandmark(landmark_string);
            services.setCustomerAmount(Long.valueOf(amonut_string));
            sendToFirebase();
        }

    }

    private void sendToFirebase() {

        final ArrayList<String> uris = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        if(pictures.size()>0){
            for(final Uri uri : pictures){
                File newImageFile = new File(uri.getPath());
                try {

                    compressedImageFile = new Compressor(FormActivity.this)
                            .setMaxHeight(200)
                            .setMaxWidth(400)
                            .setQuality(50)
                            .compressToBitmap(newImageFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbData = baos.toByteArray();

                final UploadTask image_path = storageReference.child("services").child(pictures.indexOf(uri) + services.getServiceID() + ".jpg").putBytes(thumbData);

                image_path.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d("inside fi", taskSnapshot.toString());
                        storageReference.child("services").child(pictures.indexOf(uri) + services.getServiceID() + ".jpg").getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        uris.add(uri.toString());
                                        Log.d("uri",uri.toString()+"!"+uris.size());

                                        if(uris.size() == pictures.size()){
                                            HashMap<String, Object> images = new HashMap<>();
                                            images.put("images",uris);
                                            firebaseFirestore.collection("services").document(services.getServiceID()).set(images, SetOptions.merge())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("done",uris.toString());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("failure 1", e.toString());
                                                        }
                                                    });
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("failure 2", e.toString());

                                        Toast.makeText(FormActivity.this, "(IMAGE Error uri) : " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("failure 3", e.toString());
                        Toast.makeText(FormActivity.this ,"(IMAGE Error) : " + e, Toast.LENGTH_LONG).show();
                    }
                });

            }


        }else {
            map.put("images", new ArrayList<String>());
        }


        map.put("labourUID","");
        map.put("customerUID",firebaseAuth.getUid());
        map.put("customerAmount",services.getCustomerAmount());
        map.put("description",services.getDescription());
        map.put("feedback","");
        map.put("skill",services.getSkill());
        //map.put("images", pictures);
        map.put("labourResponses", new HashMap<>());
        map.put("a1",services.getA1());
        map.put("a2",services.getA2());
        map.put("city",services.getCity());
        map.put("landmark",services.getLandmark());

        firebaseFirestore.collection("services").document(services.getServiceID()).set(map,SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        HashMap<String , String> m = new HashMap<>();
                        m.put("currentService",services.getServiceID());
                        firebaseFirestore.collection("customer").document(firebaseAuth.getUid()).set(m,SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("success","!");
                                        Intent intent = new Intent(FormActivity.this,CustomerMainActivity.class);
                                        intent.putExtra("currentService",services.getServiceID());
                                        intent.putExtra("customer",customer);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("error",e.toString());
                                    }
                                });



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"error : "+e.toString());
                    }
                });

    }

    private void chooseImage() {

       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(FormActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(FormActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {

                BringImagePicker();

            }

        } else {

            BringImagePicker();

        }
    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(400, 200)
                .start(FormActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                pictures.add(mainImageURI);
                slide.added(mainImageURI.toString());
                viewPager.setCurrentItem(pictures.size()-1);
                //viewPager.setAdapter(new Slide(getApplicationContext(), pictures));
                //photo.setImageURI(mainImageURI);

                //isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

}
