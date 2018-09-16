package de.eidottermihi.rpicheck.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.eidottermihi.rpicheck.db.entity.Device;

@Dao
public interface DeviceDao {

    @Insert
    Long insert(Device device);

    @Update
    void update(Device device);

    @Query("SELECT * FROM DEVICES ORDER BY _id ASC ")
    LiveData<List<Device>> findAll();

    @Query("SELECT * FROM DEVICES WHERE _id = :id")
    LiveData<Device> find(long id);

}
