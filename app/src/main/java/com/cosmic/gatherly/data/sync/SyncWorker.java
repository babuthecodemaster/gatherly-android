package com.cosmic.gatherly.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cosmic.gatherly.data.database.GatherlyDatabase;
import com.cosmic.gatherly.data.database.dao.MessageDao;
import com.cosmic.gatherly.data.database.entity.MessageEntity;
import com.cosmic.gatherly.data.network.NetworkManager;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.data.storage.SecurePreferences;

import java.util.List;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    
    private AuthRepository authRepository;
    private MessageDao messageDao;
    private NetworkManager networkManager;
    private SecurePreferences securePrefs;
    
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        
        authRepository = new AuthRepository(context);
        messageDao = GatherlyDatabase.getInstance(context).messageDao();
        networkManager = NetworkManager.getInstance(context);
        securePrefs = new SecurePreferences(context);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting background sync");
        
        try {
            // Check if user is logged in
            if (!authRepository.isLoggedIn()) {
                Log.d(TAG, "User not logged in, skipping sync");
                return Result.success();
            }
            
            // Check network connectivity
            if (!networkManager.isConnected()) {
                Log.d(TAG, "No network connection, skipping sync");
                return Result.retry();
            }
            
            // Sync pending messages
            syncPendingMessages();
            
            // Update last sync time
            securePrefs.putLong(SecurePreferences.KEY_LAST_SYNC, System.currentTimeMillis());
            
            Log.d(TAG, "Background sync completed successfully");
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during background sync", e);
            return Result.retry();
        }
    }
    
    private void syncPendingMessages() {
        try {
            // Get pending messages from local database
            List<MessageEntity> pendingMessages = messageDao.getPendingMessages().blockingGet();
            
            Log.d(TAG, "Found " + pendingMessages.size() + " pending messages to sync");
            
            for (MessageEntity message : pendingMessages) {
                try {
                    // Here you would typically send the message to your API
                    // For now, we'll just mark it as sent
                    // sendMessageToServer(message);
                    
                    // Mark as sent in local database
                    messageDao.updateMessageSentStatus(message.getId(), true).blockingAwait();
                    
                    Log.d(TAG, "Message synced: " + message.getId());
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing message: " + message.getId(), e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting pending messages", e);
        }
    }
    
    // This method would implement the actual API call to send messages
    private void sendMessageToServer(MessageEntity message) {
        // Implementation would depend on your API structure
        // Example:
        // SendMessageRequest request = new SendMessageRequest(message.getContent(), message.getChannelId());
        // apiService.sendMessage(request).execute();
    }
}