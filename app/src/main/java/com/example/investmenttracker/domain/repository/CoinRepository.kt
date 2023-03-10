package com.example.investmenttracker.domain.repository

import com.example.investmenttracker.data.model.CoinModel
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    // functions related to api calls
    suspend fun getCoinBySlug(slug: String): JsonObject
    suspend fun getCoinBySymbol(symbol: String): JsonObject
    suspend fun getMultipleCoinsBySlug(slugList: List<String>): JsonObject

    // functions related to db
    suspend fun insertCoinToDB(coinModel: CoinModel)
    suspend fun updateCoinInvestmentDetails(id: Int, totalTokenHeldAmount: Double, totalInvestmentAmount: Double, totalInvestmentWorth: Double)
    suspend fun updateCoinDetails(coins: List<CoinModel>)
    suspend fun deleteCoinFromDB(coin: CoinModel)

    fun getSingleCoinById(id: Int): Flow<CoinModel>
    fun getAllCoinsFromDB(): Flow<List<CoinModel>>

}