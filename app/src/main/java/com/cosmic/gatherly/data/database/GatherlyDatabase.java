package com.cosmic.gatherly.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.cosmic.gatherly.data.database.dao.MessageDao;
import com.cosmic.gatherly.data.database.dao.UserDao;
import com.cosmic.gatherly.data.database.entity.MessageEntity;
import com.cosmic.gatherly.data.database.entity.UserEntity;

@Database(
    entities = {UserEntity.class, MessageEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class GatherlyDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "gatherly_database";
    private static volatile GatherlyDatabase INSTANCE;
    
    public abstract UserDao userDao();
    public abstract MessageDao messageDao();
    
    public static GatherlyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GatherlyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            GatherlyDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    public static void destroyInstance() {
        INSTANCE = null;
    }
}