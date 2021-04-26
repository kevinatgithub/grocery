package app.kevs.biyang.grocery.libs.api

import android.graphics.Bitmap

object SearchImageResult {
    data class ImageResult(var bitmap : Bitmap?,
                           var url : String?,
                           var base64Converter : (bitmap : Bitmap) -> String?,
                           var resizer : (bitmap : Bitmap) -> Bitmap?){

        var base64 : String? = null
        get() {
            if (bitmap != null){
                return base64Converter(bitmap!!)
            }
            return null
        }

        var resizedBitmap : Bitmap? = null
        get() {
            if (bitmap != null){
                return resizer(bitmap!!)
            }
            return null
        }


    }
}