package com.qbra.kotlininstaclone.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.qbra.kotlininstaclone.databinding.ActivityPostBinding
import java.util.UUID

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private var selectedImage: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        auth = Firebase.auth
        storage = Firebase.storage
        firestore = Firebase.firestore
    }

    fun share(view: View) {

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("images/$imageName")
        if(selectedImage != null)
        {
            imageReference.putFile(selectedImage!!).addOnSuccessListener {
                val uploadPictureReference = storage.reference.child("images/$imageName")
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()

                    val postHashMap = hashMapOf<String, Any>()
                    postHashMap["downloadUrl"] = downloadUrl
                    postHashMap["userEmail"] = auth.currentUser!!.email.toString()
                    postHashMap["comment"] = binding.commentText.text.toString()
                    postHashMap["date"] = com.google.firebase.Timestamp.now()

                    firestore.collection("Posts").add(postHashMap).addOnSuccessListener {
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@PostActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }

                }.addOnFailureListener {
                    Toast.makeText(this@PostActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@PostActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun selectImage(view: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES))
                {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionResultLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else
                {
                    permissionResultLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
        else
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else
                {
                    permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedImage = intentFromResult.data
                    if(selectedImage != null)
                    {
                        binding.imageView2.setImageURI(selectedImage)
                    }
                }
            }
        }

        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if(result)
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else
            {
                Toast.makeText(this@PostActivity, "Permission needed", Toast.LENGTH_LONG).show()
            }
        }
    }

}