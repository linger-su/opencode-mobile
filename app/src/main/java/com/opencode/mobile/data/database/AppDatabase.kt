package com.opencode.mobile.data.database

import android.content.Context
import androidx.room.*
import com.opencode.mobile.data.database.dao.MessageDao
import com.opencode.mobile.data.database.dao.ConversationDao
import com.opencode.mobile.data.database.entity.MessageEntity
import com.opencode.mobile.data.database.entity.ConversationEntity

@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "opencode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromMap(value: Map<String, Any>): String {
        return value.toString()
    }

    @TypeConverter
    fun toMap(value: String): Map<String, Any> {
        return emptyMap()
    }
}
