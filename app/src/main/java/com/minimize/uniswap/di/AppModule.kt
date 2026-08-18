package com.minimize.uniswap.di

import android.content.Context
import androidx.room.Room
import com.minimize.uniswap.UniSwapApplication
import com.minimize.uniswap.data.local.UniSwapDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UniSwapDatabase {
        return Room.databaseBuilder(
            context,
            UniSwapDatabase::class.java,
            "uniswap_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideApplicationScope(@ApplicationContext context: Context): CoroutineScope {
        return (context as UniSwapApplication).applicationScope
    }
}
