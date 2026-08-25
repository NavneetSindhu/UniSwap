package com.minimize.uniswap.di

import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.repository.ReportRepository
import com.minimize.uniswap.data.repository.firebase.FirebaseAuthRepository
import com.minimize.uniswap.data.repository.firebase.FirestoreChatRepository
import com.minimize.uniswap.data.repository.firebase.FirestoreItemRepository
import com.minimize.uniswap.data.repository.firebase.FirebaseReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        firestoreRepository: FirestoreItemRepository
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        firestoreChatRepository: FirestoreChatRepository
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        firebaseReportRepository: FirebaseReportRepository
    ): ReportRepository
}
