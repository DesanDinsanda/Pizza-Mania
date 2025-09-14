package com.example.pizza_mania;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedbackActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText feedbackDescription;
    ImageView feedbackImage;
    Button btnSelectImage, btnSubmitFeedback;
    Uri imageUri = null;

    FirebaseFirestore db;
    StorageReference storageRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        feedbackDescription = findViewById(R.id.feedbackDescription);
        feedbackImage = findViewById(R.id.feedbackImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // Firebase init
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("feedback_images");

        // Ask camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        }

        // Select or capture image
        btnSelectImage.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooser = Intent.createChooser(pickIntent, "Select or Capture an Image");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{captureIntent});
            startActivityForResult(chooser, PICK_IMAGE_REQUEST);
        });

        // Submit feedback
        btnSubmitFeedback.setOnClickListener(v -> {
            String feedbackText = feedbackDescription.getText().toString().trim();

            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImageAndSaveFeedback(feedbackText);
            } else {
                saveFeedbackToFirestore(feedbackText, null);
            }
        });
    }

    private void uploadImageAndSaveFeedback(String feedbackText) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imgRef = storageRef.child(fileName);

        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveFeedbackToFirestore(feedbackText, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveFeedbackToFirestore(String feedbackText, @Nullable String imageUrl) {
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("description", feedbackText);
        feedback.put("imageUrl", imageUrl);
        feedback.put("timestamp", System.currentTimeMillis());

        db.collection("Feedback")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Feedback submitted!", Toast.LENGTH_LONG).show();
                    feedbackDescription.setText("");
                    feedbackImage.setImageResource(R.drawable.ic_launcher_background);
                    imageUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        feedbackImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    feedbackImage.setImageBitmap(photo);
                }
            }
        }
    }
}
