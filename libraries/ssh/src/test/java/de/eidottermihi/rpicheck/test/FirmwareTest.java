package de.eidottermihi.rpicheck.test;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.IOException;

import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael
 */
public class FirmwareTest extends AbstractMockedQueryTest {
    
    @Test
    public void firmware_hash() throws IOException, RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/vcgencmd_version.txt"));
        sessionMocker.withCommand("vcgencmd version",
                new CommandMocker().withResponse(output).mock());
        String firmwareVersion = raspiQuery.queryFirmwareVersion("vcgencmd");
        assertEquals("7789db485409720b0e523a3d6b86b12ed56fd152 (clean) (release)", firmwareVersion);
    }
}
