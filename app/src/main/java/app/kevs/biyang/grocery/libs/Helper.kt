package app.kevs.biyang.grocery.libs

import android.R.attr
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import app.kevs.biyang.grocery.R
import co.metalab.asyncawait.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


object Helper {
    fun encodeImageToBase64(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private const val IMG_WIDTH = 640
    private const val IMG_HEIGHT = 480

    private fun resizeBase64Image(base64image: String): String {
        val encodeByte: ByteArray = Base64.decode(base64image.toByteArray(), Base64.DEFAULT)
        val options = BitmapFactory.Options()
        var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
        if (image == null){
            return base64image
        }
        if (image.height <= 400 && image.width <= 400) {
            return base64image
        }
        image = Bitmap.createScaledBitmap(image, IMG_WIDTH, IMG_HEIGHT, false)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        System.gc()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    private fun convertString64ToImage(base64String: String): Bitmap? {
        if (base64String.isNullOrEmpty()){
            return null
        }
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return convertString64ToImage(resizeBase64Image(base64String))
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun showAlert(ctx : Context,
                    message : String,
                    onPositive : () -> Unit) {
        val builder = AlertDialog.Builder(ctx)
        builder.setMessage(message)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which -> onPositive() })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        val alert = builder.create()
        alert.setOnShowListener {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
        alert.show()
    }

    fun toast(ctx : Context, message: String, isLong : Boolean = false){
        val len = if (isLong){
            Toast.LENGTH_LONG
        }else{
            Toast.LENGTH_SHORT
        }
        Toast.makeText(ctx, message, len).show()
    }

    fun makeid(length : Int) : String{
        val characters : String = "ABCDEFGHIJKLMNOPQRSTWXYZabcdefghijklmnopqrstwxyz0123456789"
        var id = ""
        var i = 0
        while (i < length){
            val index = (0..length).random()
            id += characters[index]
            i++
        }
        return id
    }

    fun showDialog(ctx : Context,
                           obj : Any?,
                           options : ArrayList<Pair<String, (Any?) -> Unit>>
    ) {
        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_options)

        val btns = arrayListOf<Button>()
        btns.add(dialog.findViewById(R.id.btn_opt1) as Button)
        btns.add(dialog.findViewById(R.id.btn_opt2) as Button)
        btns.add(dialog.findViewById(R.id.btn_opt3) as Button)
        btns.add(dialog.findViewById(R.id.btn_opt4) as Button)
        btns.forEach{
            it.visibility = View.GONE
        }

        var i = 0
        while (i < options.size){
            val option = options.get(i)
            val btn = btns.get(i)
            if (option != null){
                btn.visibility = View.VISIBLE
                val (msg, onclick) = option
                btn.apply {
                    text = msg
                }
                btn.setOnClickListener {
                    onclick(obj)
                    dialog.dismiss()
                }
            }
            i++
        }
        dialog.show()

    }

    fun prompt(ctx : Context,
               title : String,
               message : String,
               onSubmit : (input : String) -> Unit){
        val inputAlert = AlertDialog.Builder(ctx)
        inputAlert.setTitle(title)
        inputAlert.setMessage(message)
        val userInput = EditText(ctx)
        inputAlert.setView(userInput)
        inputAlert.setPositiveButton(
            "Submit"
        ) { dialog, which -> onSubmit(userInput.text.toString()) }
        inputAlert.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.dismiss() }
        val alertDialog = inputAlert.create()
        alertDialog.show()
    }

    fun getBitmapFromUrl(src : String) : Bitmap?{
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) { // Log exception
            null
        }
    }

    fun getRelatedImagesUrlFromWeb(keyword : String, resultCount : Int = 10) : ArrayList<String>{
        var url = "https://ph.images.search.yahoo.com/search/images;_ylt=AwrxhZU7hLZerxMAKr60Rwx.;_ylc=X1MDMjExNDczNDAwNARfcgMyBGZyAwRncHJpZANUM1AwS1ZEbFR2ZUxvVzBwMzRyQl9BBG5fc3VnZwMxMARvcmlnaW4DcGguaW1hZ2VzLnNlYXJjaC55YWhvby5jb20EcG9zAzAEcHFzdHIDBHBxc3RybAMEcXN0cmwDNgRxdWVyeQN0YWxvbmcEdF9zdG1wAzE1ODkwMTk3NTM-?fr2=sb-top-ph.images.search&p=KEYWORD&ei=UTF-8&iscqry=&fr=sfp"
        url = url.replace("KEYWORD", keyword)

        val result = ArrayList<String>()
        val document: Document = Jsoup.connect(url).validateTLSCertificates(false).get()
        val imgs : List<Element> = document.select("img").take(10)
        imgs.map {
            val imgBase64 = it.attr("data-src")
            if (imgBase64 != null){
                result.add(imgBase64)
            }
        }
        return result
    }
}