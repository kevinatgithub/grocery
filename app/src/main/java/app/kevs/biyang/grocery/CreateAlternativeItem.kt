package app.kevs.biyang.grocery

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.models.ItemAlternative
import app.kevs.biyang.grocery.libs.data.DataManager
import com.bakhtiyor.gradients.Gradients
import kotlinx.android.synthetic.main.activity_alternative_items.*
import kotlinx.android.synthetic.main.activity_create_alternative_item.*
import kotlinx.android.synthetic.main.activity_create_alternative_item.fab_back
import kotlinx.android.synthetic.main.activity_create_alternative_item.imageView

class CreateAlternativeItem : AppCompatActivity() {

    var ITEM_ID : String? = null
    var IMG_CHANGED = false
    var dataManager : DataManager? = null
    val GALLERY_REQUEST = 101
    val CAMERA_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_alternative_item)
        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        create_alt_container.background = Gradients.rainyAshville()

        val itemId = intent.getStringExtra("ITEM_ID")
        if (itemId.isNullOrEmpty()){
            finish()
            return
        }

        dataManager = DataManager(this)

        ITEM_ID = itemId

        imageView.setOnClickListener {
            confirmImageSource()
        }

        fab_back.setOnClickListener {
            finish()
        }

        fab_save.setOnClickListener {
            attemptSave()
        }
    }

    override fun onPause() {
        super.onPause()
        dataManager?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST){
            imageView.setImageURI(data?.data)
            IMG_CHANGED = true
        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            IMG_CHANGED = true
        }
    }

    private fun confirmImageSource() {
        val b = AlertDialog.Builder(this)
        b.setMessage("Where to get image?")
        b.setPositiveButton("Camera", DialogInterface.OnClickListener { dialog, which -> openCamera() })
        b.setNegativeButton("Gallery", DialogInterface.OnClickListener { dialog, which -> openGallery() })
        b.setNeutralButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        val a = b.create()
        a.setOnShowListener {
            a.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            a.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            a.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK)
        }
        a.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST)
            }
        }
    }

    private fun attemptSave() {
        l_alt_description.error = ""
        if (txt_alt_description.text.isNullOrEmpty()){
            l_alt_description.error = "This is required"
        }else{
            Helper.toast(this, "saving..")

            var imgString : String? = if (IMG_CHANGED){
                var img : Bitmap? = (imageView.drawable as BitmapDrawable).bitmap
                img = Helper.getResizedBitmap(img!!, 200)
                Helper.encodeImageToBase64(img!!)
            }else{
                null
            }

            var item = ItemAlternative(
                null, ITEM_ID,
                txt_alt_description.text.toString().trim(),
                imgString, 0
            )

            dataManager?.addAlternativeItem(item,
                onSuccess = {
                    Helper.toast(this, "Alternative Item saved..")
                    setResult(0)
                    finish()
                },
                onError = {handlerError(it)})
        }
    }

    private fun handlerError(error: Throwable) {
        Helper.toast(this, error.message!!)
    }
}
