package com.rredoh.woodlogcounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var button: Button

    private val permissionId = 88

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.btn_choose_image)

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
                imageView.setImageBitmap(selectedImage);
                imageView.visibility = View.VISIBLE
            }
        }
    }

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { selectedImage ->
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                if (selectedImage != null) {
                    val cursor: Cursor? = contentResolver.query(
                        selectedImage,
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                        val picturePath: String = cursor.getString(columnIndex)
                        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                        imageView.visibility = View.VISIBLE
                        cursor.close()
                    }
                }
            }
        }
    }
}