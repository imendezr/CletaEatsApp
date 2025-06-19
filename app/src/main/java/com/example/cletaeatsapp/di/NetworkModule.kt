package com.example.cletaeatsapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.cletaeatsapp.data.network.CletaEatsApiService
import com.example.cletaeatsapp.data.network.CletaEatsNetwork
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCletaEatsApiService(): CletaEatsApiService = CletaEatsNetwork.apiService

    @Provides
    @Singleton
    fun provideCletaEatsRepository(
        @ApplicationContext context: Context,
        apiService: CletaEatsApiService
    ): CletaEatsRepository = CletaEatsRepository(context, apiService)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("cletaeats_prefs") }
        )
    }
}
