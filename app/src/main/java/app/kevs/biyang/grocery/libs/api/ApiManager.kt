package app.kevs.biyang.grocery.libs.api

import app.kevs.biyang.grocery.libs.api.endpoints.GroceryApiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiManager {

    companion object {

        val API_BASE_URL = "https://grocerylist.getsandbox.com/"
        val SEARCH_IMAGE_API_BASE_URL = "https://serpapi.com/"

        fun create(): GroceryApiService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create())
                .baseUrl(API_BASE_URL)
                .build()

            return retrofit.create(GroceryApiService::class.java)
        }
    }
}