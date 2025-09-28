package com.cosmic.gatherly.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.cosmic.gatherly.data.database.entity.MessageEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    Single<MessageEntity> getMessageById(String messageId);
    
    @Query("SELECT * FROM messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    Flowable<List<MessageEntity>> getMessagesByChannel(String channelId);
    
    @Query("SELECT * FROM messages WHERE channelId = :channelId ORDER BY timestamp ASC LIMIT :limit")
    Single<List<MessageEntity>> getRecentMessagesByChannel(String channelId, int limit);
    
    @Query("SELECT * FROM messages WHERE channelId = :channelId AND timestamp < :beforeTimestamp ORDER BY timestamp DESC LIMIT :limit")
    Single<List<MessageEntity>> getMessagesBefore(String channelId, long beforeTimestamp, int limit);
    
    @Query("SELECT * FROM messages WHERE userId = :userId ORDER BY timestamp DESC")
    Single<List<MessageEntity>> getMessagesByUser(String userId);
    
    @Query("SELECT * FROM messages WHERE content LIKE :query ORDER BY timestamp DESC")
    Single<List<MessageEntity>> searchMessages(String query);
    
    @Query("SELECT * FROM messages WHERE isSent = 0 ORDER BY timestamp ASC")
    Single<List<MessageEntity>> getPendingMessages();
    
    @Query("SELECT * FROM messages WHERE isDelivered = 0 AND isSent = 1 ORDER BY timestamp ASC")
    Single<List<MessageEntity>> getUndeliveredMessages();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessage(MessageEntity message);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessages(List<MessageEntity> messages);
    
    @Update
    Completable updateMessage(MessageEntity message);
    
    @Query("UPDATE messages SET isSent = :isSent WHERE id = :messageId")
    Completable updateMessageSentStatus(String messageId, boolean isSent);
    
    @Query("UPDATE messages SET isDelivered = :isDelivered WHERE id = :messageId")
    Completable updateMessageDeliveredStatus(String messageId, boolean isDelivered);
    
    @Query("UPDATE messages SET isRead = :isRead WHERE id = :messageId")
    Completable updateMessageReadStatus(String messageId, boolean isRead);
    
    @Query("UPDATE messages SET isRead = 1 WHERE channelId = :channelId AND userId != :currentUserId")
    Completable markChannelMessagesAsRead(String channelId, String currentUserId);
    
    @Delete
    Completable deleteMessage(MessageEntity message);
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    Completable deleteMessageById(String messageId);
    
    @Query("DELETE FROM messages WHERE channelId = :channelId")
    Completable deleteMessagesByChannel(String channelId);
    
    @Query("DELETE FROM messages WHERE timestamp < :beforeTimestamp")
    Completable deleteOldMessages(long beforeTimestamp);
    
    @Query("DELETE FROM messages")
    Completable deleteAllMessages();
    
    @Query("SELECT COUNT(*) FROM messages WHERE channelId = :channelId")
    Single<Integer> getMessageCountByChannel(String channelId);
    
    @Query("SELECT COUNT(*) FROM messages WHERE channelId = :channelId AND isRead = 0 AND userId != :currentUserId")
    Single<Integer> getUnreadMessageCount(String channelId, String currentUserId);
}