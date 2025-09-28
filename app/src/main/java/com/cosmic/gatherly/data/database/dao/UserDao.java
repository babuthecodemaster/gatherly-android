package com.cosmic.gatherly.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.cosmic.gatherly.data.database.entity.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    Single<UserEntity> getUserById(String userId);
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    Single<UserEntity> getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    Single<UserEntity> getUserByEmail(String email);
    
    @Query("SELECT * FROM users ORDER BY username ASC")
    Flowable<List<UserEntity>> getAllUsers();
    
    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY username ASC")
    Flowable<List<UserEntity>> getOnlineUsers();
    
    @Query("SELECT * FROM users WHERE isOnline = 0 ORDER BY username ASC")
    Flowable<List<UserEntity>> getOfflineUsers();
    
    @Query("SELECT * FROM users WHERE username LIKE :query OR email LIKE :query ORDER BY username ASC")
    Single<List<UserEntity>> searchUsers(String query);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(UserEntity user);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUsers(List<UserEntity> users);
    
    @Update
    Completable updateUser(UserEntity user);
    
    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :userId")
    Completable updateUserOnlineStatus(String userId, boolean isOnline);
    
    @Query("UPDATE users SET status = :status WHERE id = :userId")
    Completable updateUserStatus(String userId, String status);
    
    @Query("UPDATE users SET avatar = :avatar WHERE id = :userId")
    Completable updateUserAvatar(String userId, String avatar);
    
    @Delete
    Completable deleteUser(UserEntity user);
    
    @Query("DELETE FROM users WHERE id = :userId")
    Completable deleteUserById(String userId);
    
    @Query("DELETE FROM users")
    Completable deleteAllUsers();
    
    @Query("SELECT COUNT(*) FROM users")
    Single<Integer> getUserCount();
    
    @Query("SELECT COUNT(*) FROM users WHERE isOnline = 1")
    Single<Integer> getOnlineUserCount();
}