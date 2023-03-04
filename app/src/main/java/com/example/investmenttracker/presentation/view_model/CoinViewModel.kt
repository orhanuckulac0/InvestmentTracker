package com.example.investmenttracker.presentation.view_model

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.investmenttracker.data.model.UserData
import com.example.investmenttracker.data.util.Resource
import com.example.investmenttracker.domain.use_case.coin.GetAllCoinsUseCase
import com.example.investmenttracker.domain.use_case.coin.GetMultipleCoinsUseCase
import com.example.investmenttracker.domain.use_case.user.GetUserDataUseCase
import com.example.investmenttracker.domain.use_case.user.InsertUserDataUseCase
import com.example.investmenttracker.domain.use_case.user.UpdateUserDataUseCase
import com.example.investmenttracker.presentation.events.UiEventActions
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoinViewModel(
    private val app:Application,
    private val getAllCoinsUseCase: GetAllCoinsUseCase,
    private val insertUserDataUseCase: InsertUserDataUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val updateUserDataUseCase: UpdateUserDataUseCase,
    private val getMultipleCoinsUseCase: GetMultipleCoinsUseCase
): AndroidViewModel(app) {

    var userData: UserData? = null

    val coinsListResponse: MutableLiveData<Resource<JsonObject>> = MutableLiveData()

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.run {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
        return result
    }

    fun getTokensFromWallet() = liveData {
        getAllCoinsUseCase.execute().collect(){
            emit(it)
        }
    }

    fun getUserData(id: Int) = liveData {
        getUserDataUseCase.execute(id).collect(){
            emit(it)
            userData = it
        }
    }

    fun updateUserdata(){
        viewModelScope.launch {
            if (userData != null){
                updateUserDataUseCase.execute(userData!!)
            }
        }
    }

    fun insertUserData(data: UserData) {
        viewModelScope.launch {
            insertUserDataUseCase.execute(data)
        }
    }

    fun getMultipleCoinsBySlug(slugList: List<String>) = viewModelScope.launch(Dispatchers.IO) {
        coinsListResponse.postValue(Resource.Loading())
        try {
            if (isNetworkAvailable(app)){
                val slugListResponse = getMultipleCoinsUseCase.execute(slugList)
                coinsListResponse.postValue(Resource.Success(slugListResponse))
            }else {
                coinsListResponse.postValue(Resource.Error(UiEventActions.NO_INTERNET_CONNECTION))
            }
        }catch (e:java.lang.Exception){
            coinsListResponse.postValue(Resource.Error("No search results. Check your spelling."))
        }
    }
}