package com.imsoft.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
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
import com.imsoft.kotlinartbook.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap: Bitmap? = null

    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = this.openOrCreateDatabase("ArtBookDB", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val art = intent.getStringExtra("art")

        if (art.equals("newArt")) {

            binding.imageView.setImageResource(R.drawable.select)
            binding.titleText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")

            binding.saveButton.visibility = View.VISIBLE

        } else {

            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)

            val cursor = db.rawQuery("SELECT * FROM artbook WHERE id = ?", arrayOf(selectedId.toString()))

            val nameIndex = cursor.getColumnIndex("artname")
            val artistNameIndex = cursor.getColumnIndex("artistname")
            val yearIndex = cursor.getColumnIndex("year")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {

                binding.titleText.setText(cursor.getString(nameIndex))
                binding.artistNameText.setText(cursor.getString(artistNameIndex))
                binding.yearText.setText(cursor.getString(yearIndex))

                val imageByteArray = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }

            cursor.close()

        }
    }

    fun saveBtnClick(view: View) {

        val artName = binding.titleText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if (selectedBitmap != null) {
            val minimizeBitmap = makeMinimizeBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            minimizeBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val imageByteArray = outputStream.toByteArray()

            try {

//                val db = this.openOrCreateDatabase("ArtBookDB", MODE_PRIVATE, null)
                db.execSQL("CREATE TABLE IF NOT EXISTS artbook (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO artbook (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = db.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, imageByteArray)
                statement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this@DetailsActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    fun selectImageClick(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this@DetailsActivity,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@DetailsActivity,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Snackbar.make(view, "Permission Needed", Snackbar.LENGTH_INDEFINITE).setAction("OK", View.OnClickListener {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                } else {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {

                val intentGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGalery)

            }

        } else {
            if (ContextCompat.checkSelfPermission(
                    this@DetailsActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@DetailsActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(view, "Permission Needed", Snackbar.LENGTH_INDEFINITE).setAction("OK", View.OnClickListener {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                } else {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {

                val intentGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGalery)

            }
        }



    }


    private fun makeMinimizeBitmap (image: Bitmap, maxSize: Int): Bitmap {

        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }


        return Bitmap.createScaledBitmap(image, width, height, true)
    }


    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data

                if (intentFromResult != null) {
                    val imageUri = intentFromResult.data

                    if (imageUri != null) {
                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver, imageUri)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@DetailsActivity.contentResolver, imageUri)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }

            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

            if (result) {
                // premission granted
                val intentGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGalery)
            } else {
                // premission denied
                Toast.makeText(this@DetailsActivity, "Permission Needed", Toast.LENGTH_LONG).show()
            }

        }

    }
}