package com.opencode.mobile.di

import android.content.Context
import com.opencode.mobile.data.database.AppDatabase
import com.opencode.mobile.data.database.dao.MessageDao
import com.opencode.mobile.data.database.dao.ConversationDao
import com.opencode.mobile.data.repository.ChatRepository
import com.opencode.mobile.data.repository.SettingsRepository
import com.opencode.mobile.domain.controller.DeviceController
import com.opencode.mobile.domain.controller.AccessibilityDeviceController
import com.opencode.mobile.domain.parser.CommandParser
import com.opencode.mobile.domain.parser.DefaultCommandParser
import com.opencode.mobile.domain.provider.LLMProvider
import com.opencode.mobile.domain.provider.OpenAIProvider
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        messageDao: MessageDao,
        conversationDao: ConversationDao
    ): ChatRepository {
        return ChatRepository(messageDao, conversationDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideCommandParser(): CommandParser {
        return DefaultCommandParser()
    }

    @Provides
    @Singleton
    fun provideDeviceController(
        @ApplicationContext context: Context
    ): DeviceController {
        return AccessibilityDeviceController(context)
    }

    @Provides
    @Singleton
    fun provideLLMProvider(): LLMProvider {
        return OpenAIProvider()
    }
}
