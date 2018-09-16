package de.eidottermihi.rpicheck.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.eidottermihi.rpicheck.db.entity.Device;

import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DeviceDaoTest {

    private DeviceDatabase db;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DeviceDatabase.class).build();
    }

    @After
    public void teardown() {
        db.close();
    }

    @Test
    public void test() {
        final DeviceDao dao = db.deviceDao();
        dao.insert(new Device());
        final LiveData<List<Device>> all = dao.findAll();
        final List<Device> value = all.getValue();
        assertThat(value.size(), CoreMatchers.is(0));
    }


}
