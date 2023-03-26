package ni.edu.uca.mein_leben_2

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.DrawableContainer
import android.icu.util.Output
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy.request
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.contentValuesOf
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import ni.edu.uca.mein_leben_2.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val url = "https://meme-api.com/gimme"
    private lateinit var  imageView: ImageView
    private lateinit var builder : AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageView = binding.imgView
        builder = AlertDialog.Builder(this)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        binding.download.setOnClickListener {
            val bitmap = getImageOfView(imageView)
            if(bitmap!=null){
                saveToStorage(bitmap)
            }
            Log.d("Hello","Hello")
            builder.setTitle("The image has been saved in photos")
                .setPositiveButton("Ok"){dialogInterface, it ->
                    dialogInterface.cancel()
                }.show()

        }
        loading()
    }

    private fun saveToStorage(bitmap: Bitmap) {
        val imageName = "noob_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            this.contentResolver?.also { resolver ->
                val contenteValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME,imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
                }
                val imgUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contenteValues)
                fos = imgUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        }
        else{
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory,imageName)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(applicationContext, "Successfully capture view", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageOfView(imageView: ImageView): Bitmap? {
        var image : Bitmap? = null
        try{
            image = Bitmap.createBitmap(imageView.measuredWidth,imageView.measuredHeight,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            imageView.draw(canvas)
        }catch (e: Exception){
            Log.e("XD","Cannot Capture")
        }
        return image
    }

    private fun loading(){
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, this.url, null,
            {response ->

                Picasso.get().load(response.get("url").toString()).placeholder(R.drawable.loading).into(imageView)
            },
            {
                Log.e("error", it.toString())
                Toast.makeText(applicationContext, "loading error", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    fun nextMeme(view: View){
        Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show()
        this.loading()
    }

    fun shareMeme(view: View){
        val bitmapDrawable = imageView.drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver,bitmap,"Tittle", null)

        val bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        startActivity(Intent.createChooser(intent, "Share to: "))
    }

}