package com.example.cletaeatsapp.di

import android.content.Context
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideCletaEatsRepository(@ApplicationContext context: Context): CletaEatsRepository {
        return CletaEatsRepository(context)
    }
}
