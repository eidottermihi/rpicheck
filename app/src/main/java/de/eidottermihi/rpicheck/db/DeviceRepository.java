package de.eidottermihi.rpicheck.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import de.eidottermihi.rpicheck.db.entity.Device;

public class DeviceRepository {

    private DeviceDao deviceDao;
    private LiveData<List<Device>> allDevices;

    public DeviceRepository(Application application) {
        DeviceDatabase deviceDatabase = DeviceDatabase.getDatabase(application);
        this.deviceDao = deviceDatabase.deviceDao();
        this.allDevices = this.deviceDao.findAll();
    }

    public LiveData<List<Device>> findAll() {
        return this.allDevices;
    }

    public void insert(Device device) {
        new InsertAsyncTask(this.deviceDao).execute(device);
    }

    private static class InsertAsyncTask extends AsyncTask<Device, Void, Void> {

        private DeviceDao deviceDao;

        InsertAsyncTask(DeviceDao deviceDao) {
            this.deviceDao = deviceDao;
        }

        @Override
        protected Void doInBackground(final Device... devices) {
            this.deviceDao.insert(devices[0]);
            return null;
        }
    }
}
