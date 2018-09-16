package de.eidottermihi.rpicheck.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.rpicheck.db.entity.Device;

@Database(entities = {Device.class}, version = 11)
public abstract class DeviceDatabase extends RoomDatabase {

    public abstract DeviceDao deviceDao();

    private static final Logger LOG = LoggerFactory.getLogger(DeviceDatabase.class);

    private static volatile DeviceDatabase instance;

    static DeviceDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (DeviceDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context,
                            DeviceDatabase.class, "RASPIQUERY")
                            .addMigrations(MIGRATION_10_11)
                            .build();
                }
            }
        }
        return instance;
    }

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            LOG.info("Migrating from 10 to 11 (Room Database)...");
        }
    };


}
