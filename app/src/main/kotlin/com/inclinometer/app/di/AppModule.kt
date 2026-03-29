package com.inclinometer.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.inclinometer.app.data.local.AppDatabase
import com.inclinometer.app.data.local.MeasurementDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideMeasurementDao(db: AppDatabase): MeasurementDao = db.measurementDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
