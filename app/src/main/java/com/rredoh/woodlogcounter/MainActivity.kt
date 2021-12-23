package com.rredoh.woodlogcounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import org.opencv.android.OpenCVLoader
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var textView: TextView

    private val permissionId = 88
    private var tempUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.btn_choose_image)
        textView = findViewById(R.id.textView)

        if (OpenCVLoader.initDebug()) {
            Log.d("myTag", "OpenCV loaded")
        }

        button.setOnClickListener {
            if(checkAndRequestPermissions()){
                chooseImage();
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            permissionId -> when {
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        applicationContext,
                        "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT
                    )
                        .show()
                }
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        applicationContext,
                        "FlagUp Requires Access to Your Storage.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    chooseImage()
                }
            }
        }
    }

    fun checkAndRequestPermissions(): Boolean {
        val WExtstorePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, listPermissionsNeeded
                    .toTypedArray(),
                permissionId
            )
            return false
        }
        return true
    }

    private fun chooseImage() {
        val optionsMenu = arrayOf<CharSequence>(
            "Take Photo",
            "Choose from Gallery",
            "Exit"
        )
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setItems(optionsMenu) { dialogInterface, i ->
            when (i) {
                0 -> {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraResult.launch(takePicture)
                }
                1 -> {
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResult.launch(pickPhoto)
                }
                2 -> {
                    dialogInterface.dismiss()
                }
            }
        }
        builder.show()
    }

    private val cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.get("data")?.let {
                val selectedImage = it as Bitmap
                getImageUri(it)?.let { uri ->
                    cropImageResult.launch(
                        options(uri = uri) {
                            setGuidelines(CropImageView.Guidelines.ON)
                            setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        }
                    )
                }
            }
        }
    }

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { selectedImage ->
                cropImageResult.launch(
                    options(uri = selectedImage) {
                        setGuidelines(CropImageView.Guidelines.ON)
                        setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                    }
                )
            }
        }
    }

    private val cropImageResult = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriFilePath = result.getUriFilePath(this)

            val bitmap = BitmapFactory.decodeFile(uriFilePath)
            val objectDetectionResult = ObjectDetection().detectCircle(bitmap)

            imageView.setImageBitmap(objectDetectionResult.first)
            imageView.visibility = View.VISIBLE

            textView.text = "Number of circle : " + objectDetectionResult.second

            tempUri?.let {
                contentResolver.delete(it, null, null)
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri? {
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "logs-image",
            null
        )
        tempUri = Uri.parse(path)
        return tempUri
    }
}