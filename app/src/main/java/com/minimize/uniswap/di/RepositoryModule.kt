package com.minimize.uniswap.di

import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.repository.firebase.FirestoreItemRepository
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
}
