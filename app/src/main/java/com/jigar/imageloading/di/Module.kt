package com.jigar.imageloading.di

import android.content.Context
import com.jigar.imageloading.constant.Constant
import com.jigar.imageloading.data.api.PhotosApi
import com.jigar.imageloading.data.domain.repository.PhotoRepository
import com.jigar.imageloading.data.domain.repository.PhotoRepositoryImpl
import com.jigar.imageloading.imageloader.ImageCache
import com.jigar.imageloading.imageloader.ImageLoader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {
    @Singleton
    @Provides
    fun provideImageLoader(@ApplicationContext context: Context): ImageCache {
        return ImageLoader(context)
    }

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    HttpLoggingInterceptor.Level.BODY
                )
            )
        }.build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Singleton
    @Provides
    fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi)

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient, moshiConverterFactory: MoshiConverterFactory
    ): Retrofit {
        return Retrofit.Builder().addConverterFactory(moshiConverterFactory)
            .baseUrl(Constant.BASE_URL).client(okHttpClient).build()
    }

    @Singleton
    @Provides
    fun providePhotoApi(retrofit: Retrofit): PhotosApi = retrofit.create(PhotosApi::class.java)

    @Singleton
    @Provides
    fun providePhotoRepository(photosApi: PhotosApi): PhotoRepository {
        return PhotoRepositoryImpl(photosApi)
    }

}