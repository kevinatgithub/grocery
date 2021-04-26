package app.kevs.biyang.grocery

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.kevs.biyang.grocery.libs.models.Category
import app.kevs.biyang.grocery.libs.models.GroceryItem
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.data.DataManager
import app.kevs.biyang.grocery.libs.models.AppPreference
import co.metalab.asyncawait.async
import com.bakhtiyor.gradients.Gradients
import com.chibatching.kotpref.Kotpref
import kotlinx.android.synthetic.main.activity_alternative_items.*
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_create.fab_back


class CreateGroceryItem : AppCompatActivity() {

    var dataManager : DataManager? = null
    var ITEM : GroceryItem? = null
    val GALLERY_REQUEST = 101
    val CAMERA_REQUEST = 102
    val SEARCH_IMAGE_REQUEST = 103
    var IMG_CHANGED = false
    var IMG_URL : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        Kotpref.init(this)

        supportActionBar?.setBackgroundDrawable(AppConstants.actionBarBackgrounDrawable)
        actionBar?.setBackgroundDrawable(AppConstants.actionBarBackgrounDrawable)
        create_container.background = AppConstants.containerBackgroundDrawable

        dataManager = DataManager(this)

        val itemId = intent.getStringExtra("ITEM_ID")

        if (!itemId.isNullOrEmpty()){
            dataManager?.getItem(itemId,
                onSuccess = {
                    ITEM = it
                    populateFields()
                },
                onError = {handleError(it)})
        }

        img_image.setOnClickListener {
            confirmImageSource()
        }

        fab_save.setOnClickListener {
            attemptSave()
        }

        fab_back.setOnClickListener {
            finish()
        }

        getCategories()
    }

    private fun handleError(error: Throwable) {
        Helper.toast(this, error.message!!, true)
    }

    private fun populateFields() {
        if (ITEM != null){
            if(!ITEM?.imgUrl.isNullOrEmpty()){
                val url = ITEM?.imgUrl!!
                async {
                    val bitmap = await { Helper.getBitmapFromUrl(url) }
                    if (bitmap != null)
                        img_image.setImageBitmap(bitmap)
                }
            }else if (!ITEM?.img.isNullOrEmpty() && ITEM?.img != "null") {
                val bmp = Helper.decodeBase64ToBitmap(ITEM?.img!!)
                img_image.setImageBitmap(bmp)
            }else{
                img_image.setImageDrawable(resources.getDrawable(R.drawable.ic_info))
            }

            txt_name.setText(ITEM?.name)
            txt_quantity.setText(ITEM?.quantity.toString())
            txt_quantity_type.setText(ITEM?.quantityType)
            txt_remarks.setText(ITEM?.remarks)
            val index = CATEGORIES.indexOf(
                Category(
                    ITEM?.category
                )
            )
            spinner_category.setSelection(index)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST){
            img_image.setImageURI(data?.data)
            IMG_CHANGED = true
        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            img_image.setImageBitmap(imageBitmap)
            IMG_CHANGED = true
        }
        if (requestCode == SEARCH_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            img_image.setImageBitmap(Helper.decodeBase64ToBitmap(AppPreference.itemImgBase64))
            IMG_URL = data?.getStringExtra("IMG_URL")
            IMG_CHANGED = true
        }
    }

    private fun confirmImageSource() {
        val options = ArrayList<Pair<String, (Any?) -> Unit>>()

        val openCameraClick : (Any?) -> Unit = {param : Any? -> openCamera()}
        options.add(Pair("OPEN CAMERA", openCameraClick))

        val openGallery : (Any?) -> Unit = {param : Any? -> openGallery()}
        options.add(Pair("OPEN GALLERY", openGallery))

        val openSearch : (Any?) -> Unit = {param : Any? -> openSearchImage()}
        options.add(Pair("SEARCH WEB", openSearch))

        val setBlank : (Any?) -> Unit = {param : Any? -> clearCurrentImage()}
        options.add(Pair("CLEAR CURRENT IMAGE", setBlank))

        Helper.showDialog(this,
            null,
            options)

    }

    private fun clearCurrentImage() {
        ITEM?.img = null
        dataManager?.updateItem(ITEM!!,
            onSuccess = {
                populateFields()
            }){handleError(it)}
    }

    private fun openSearchImage() {
        val intent = Intent(this, SearchImage::class.java)
        intent.putExtra("KEYWORD", txt_name.text.toString().trim())
        startActivityForResult(intent, SEARCH_IMAGE_REQUEST)
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

    var CATEGORIES = ArrayList<Category>()

    private fun getCategories() {
        dataManager?.getCategories {
            val catergories = mutableListOf<String>()
            for(cat in it){
                catergories.add(cat.category.toString())
            }
            CATEGORIES.clear()
            CATEGORIES.addAll(it)
            populateCategories(catergories)
        }
    }

    private fun populateCategories(catergories: MutableList<String>) {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, catergories)
        spinner_category.adapter = adapter
    }

    private fun attemptSave() {
        setState("LOADING")
        if (formValid()){
            val name = txt_name.text.toString().trim()
            val quantity = txt_quantity.text.toString().trim()
            val quantityType = txt_quantity_type.text.toString().trim()
            val category = spinner_category.selectedItem?.toString() ?: "General"
            val remarks = txt_remarks.text.toString().trim()
            var imgBase64 : String? = ITEM?.img
            if (IMG_CHANGED){
                var img : Bitmap? = (img_image.drawable as BitmapDrawable).bitmap
                if (img != null){
                    img = Helper.getResizedBitmap(img, 200)
                    imgBase64 = Helper.encodeImageToBase64(img!!)
                }
            }
            val order : Int = ITEM?.order ?: 0
            val itemId : String? = ITEM?._id ?: Helper.makeid(20)
            val item = GroceryItem(
                itemId,
                name,
                Integer.parseInt(quantity),
                quantityType,
                category,
                remarks,
                null,
                IMG_URL,
                false,
                order
            )
            if (ITEM == null)
                dataManager?.addItem(item,
                    onSuccess = {finish()},
                    onError = {handleError(it)})
            else
                dataManager?.updateItem(item,
                    onSuccess = {finish()},
                    onError = {handleError(it)})
        }
        setState("IDLE")
    }

    private fun formValid(): Boolean {
        l_name.error = null
        l_quantity_type.error = null

        var valid = true
        if (txt_name.text.toString().isNullOrEmpty()){
            l_name.error = "The name is required"
            valid = false
        }

        if (txt_quantity.text.toString().isNullOrEmpty()){
            l_quantity_type.error = "Provide quantity"
            valid = false
        }

        return valid
    }

    override fun onPause() {
        super.onPause()
        dataManager?.dispose()
    }

    fun setState(state : String){
        when(state){
            "LOADING" -> fab_save.apply {
                    isEnabled = false
                }
            "IDLE" -> fab_save.apply {
                isEnabled = true
            }
        }
    }
}
