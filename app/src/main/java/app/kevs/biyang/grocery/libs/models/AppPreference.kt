package app.kevs.biyang.grocery.libs.models

import com.chibatching.kotpref.KotprefModel

object AppPreference : KotprefModel() {
    var itemImgBase64 by stringPref()
}