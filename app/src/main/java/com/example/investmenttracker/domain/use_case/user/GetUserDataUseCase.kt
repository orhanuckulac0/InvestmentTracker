package com.example.investmenttracker.domain.use_case.user

import com.example.investmenttracker.data.model.UserData
import com.example.investmenttracker.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow

class GetUserDataUseCase(private val userDataRepository: UserDataRepository) {

    fun execute(id: Int): Flow<UserData> = userDataRepository.getData(id)

}