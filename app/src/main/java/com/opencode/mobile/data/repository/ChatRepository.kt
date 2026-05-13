package com.opencode.mobile.data.repository

import com.opencode.mobile.data.database.dao.MessageDao
import com.opencode.mobile.data.database.dao.ConversationDao
import com.opencode.mobile.data.database.entity.MessageEntity
import com.opencode.mobile.data.database.entity.ConversationEntity
import com.opencode.mobile.domain.model.Message
import com.opencode.mobile.domain.model.MessageRole
import com.opencode.mobile.utils.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val DEFAULT_CONVERSATION_ID = "default_conversation"
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private var currentConversationId: String = DEFAULT_CONVERSATION_ID
    private var isInitialized = false

    init {
        LogManager.i(TAG, "ChatRepository 初始化")
    }

    private suspend fun ensureConversationExists() {
        if (!isInitialized) {
            try {
                // 检查默认对话是否存在
                val existing = conversationDao.getConversationById(DEFAULT_CONVERSATION_ID)
                if (existing == null) {
                    // 创建默认对话
                    val conversation = ConversationEntity(
                        id = DEFAULT_CONVERSATION_ID,
                        title = "默认对话",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    conversationDao.insertConversation(conversation)
                    LogManager.d(TAG, "创建默认对话")
                }
                isInitialized = true
            } catch (e: Exception) {
                LogManager.e(TAG, "初始化对话失败", e)
            }
        }
    }

    fun getMessages(): Flow<List<Message>> {
        return messageDao.getMessagesByConversation(currentConversationId).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id,
                    role = MessageRole.valueOf(entity.role),
                    content = entity.content,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun addMessage(message: Message) {
        ensureConversationExists()
        
        val entity = MessageEntity(
            id = message.id,
            conversationId = currentConversationId,
            role = message.role.name,
            content = message.content,
            timestamp = message.timestamp
        )
        messageDao.insertMessage(entity)
        LogManager.d(TAG, "消息已保存: ${message.role} - ${message.content.take(30)}...")

        // 更新对话时间
        try {
            conversationDao.getConversationById(currentConversationId)?.let { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(updatedAt = System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "更新对话时间失败", e)
        }
    }

    suspend fun clearMessages() {
        messageDao.deleteMessagesByConversation(currentConversationId)
        LogManager.i(TAG, "消息已清除")
    }

    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    suspend fun switchConversation(conversationId: String) {
        currentConversationId = conversationId
        LogManager.d(TAG, "切换对话: $conversationId")
    }

    suspend fun deleteConversation(conversationId: String) {
        conversationDao.getConversationById(conversationId)?.let {
            conversationDao.deleteConversation(it)
            messageDao.deleteMessagesByConversation(conversationId)
            LogManager.d(TAG, "删除对话: $conversationId")
        }
    }
}
