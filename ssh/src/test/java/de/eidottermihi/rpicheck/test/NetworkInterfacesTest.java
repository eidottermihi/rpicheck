/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.eidottermihi.rpicheck.test;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.WlanBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class NetworkInterfacesTest extends AbstractMockedQueryTest {

    @Test
    public void eth0() throws RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\neth0").mock());
        sessionMocker.withCommand("cat /sys/class/net/eth0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("ip -f inet addr show dev eth0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation eth0 = interfaces.get(0);
        assertEquals("eth0", eth0.getName());
        assertEquals(true, eth0.isHasCarrier());
        assertEquals("192.168.0.9", eth0.getIpAdress());
        assertNull(eth0.getWlanInfo());
    }

    @Test
    public void eth0_no_ip_cmd_available() throws RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\neth0").mock());
        sessionMocker.withCommand("cat /sys/class/net/eth0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("ip -f inet addr show dev eth0 | sed -n 2p",
                new CommandMocker().withResponse("ip: command not found")
                        .withExitStatus(1).mock());
        sessionMocker
                .withCommand(
                        "/sbin/ifconfig eth0 | grep \"inet addr\"",
                        new CommandMocker()
                                .withResponse(
                                        "          inet addr:192.168.0.9  Bcast:192.168.0.255  Mask:255.255.255.0")
                                .withExitStatus(0).mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation eth0 = interfaces.get(0);
        assertEquals("eth0", eth0.getName());
        assertEquals(true, eth0.isHasCarrier());
        assertEquals("192.168.0.9", eth0.getIpAdress());
        assertNull(eth0.getWlanInfo());
    }

    @Test
    public void eth0_no_ip_and_ifconfig_available() throws RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\neth0").mock());
        sessionMocker.withCommand("cat /sys/class/net/eth0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("ip -f inet addr show dev eth0 | sed -n 2p",
                new CommandMocker().withResponse("ip: command not found")
                        .withExitStatus(1).mock());
        sessionMocker.withCommand("/sbin/ifconfig eth0 | grep \"inet addr\"",
                new CommandMocker().withResponse("ifconfig: command not found")
                        .withExitStatus(1).mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation eth0 = interfaces.get(0);
        assertEquals("eth0", eth0.getName());
        assertEquals(true, eth0.isHasCarrier());
        assertNull(eth0.getIpAdress());
        assertNull(eth0.getWlanInfo());
    }

    @Test
    public void wlan_signal_percentage() throws IOException, RaspiQueryException {
        String iwconfigOutput = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/iwconfig_percentage.txt"));
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\nwlp2s0").mock());
        sessionMocker.withCommand("cat /sys/class/net/wlp2s0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("ip -f inet addr show dev wlp2s0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        sessionMocker.withCommand("LC_ALL=C /usr/bin/whereis iwconfig",
                new CommandMocker().withResponse("vcgencmd: /sbin/iwconfig").mock());
        sessionMocker.withCommand("LC_ALL=C /sbin/iwconfig wlp2s0", new CommandMocker()
                .withResponse(iwconfigOutput).mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation wlan0 = interfaces.get(0);
        assertEquals("wlp2s0", wlan0.getName());
        assertEquals(true, wlan0.isHasCarrier());
        assertEquals("192.168.0.9", wlan0.getIpAdress());
        WlanBean wlanInfo = wlan0.getWlanInfo();
        assertNotNull(wlanInfo);
        assertEquals(66, wlanInfo.getLinkQuality().intValue());
        assertEquals(70, wlanInfo.getSignalLevel().intValue());
    }

    @Test
    public void wlan_signal_dbm() throws IOException, RaspiQueryException {
        String iwconfigOutput = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/iwconfig_dbm.txt"));
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\nwlp2s0").mock());
        sessionMocker.withCommand("cat /sys/class/net/wlp2s0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("LC_ALL=C /usr/bin/whereis iwconfig",
                new CommandMocker().withResponse("vcgencmd: /sbin/iwconfig").mock());
        sessionMocker.withCommand("ip -f inet addr show dev wlp2s0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        sessionMocker.withCommand("LC_ALL=C /sbin/iwconfig wlp2s0", new CommandMocker()
                .withResponse(iwconfigOutput).mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation wlan0 = interfaces.get(0);
        assertEquals("wlp2s0", wlan0.getName());
        assertEquals(true, wlan0.isHasCarrier());
        assertEquals("192.168.0.9", wlan0.getIpAdress());
        WlanBean wlanInfo = wlan0.getWlanInfo();
        assertNotNull(wlanInfo);
        assertEquals(94, wlanInfo.getLinkQuality().intValue());
        assertEquals(100, wlanInfo.getSignalLevel().intValue());
    }


    @Test
    public void wlan_no_iwconfig() throws IOException, RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\nwlp2s0").mock());
        sessionMocker.withCommand("cat /sys/class/net/wlp2s0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("LC_ALL=C /usr/bin/whereis iwconfig",
                new CommandMocker().withResponse("whereis: not found").withExitStatus(127).mock());
        sessionMocker.withCommand("ip -f inet addr show dev wlp2s0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        sessionMocker.withCommand("cat /proc/net/wireless", new CommandMocker()
                .withResponse(
                        "Inter-| sta-|   Quality        |   Discarded packets               | Missed | WE"
                                + "\n"
                                + " face | tus | link level noise |  nwid  crypt   frag  retry   misc | beacon | 22"
                                + "\n"
                                + " wlp2s0: 0000 100. 95. 0. 0 0 0 0 0 0\n").mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation wlan0 = interfaces.get(0);
        assertEquals("wlp2s0", wlan0.getName());
        assertEquals(true, wlan0.isHasCarrier());
        assertEquals("192.168.0.9", wlan0.getIpAdress());
        WlanBean wlanInfo = wlan0.getWlanInfo();
        assertNotNull(wlanInfo);
        assertEquals(100, wlanInfo.getLinkQuality().intValue());
        assertEquals(95, wlanInfo.getSignalLevel().intValue());
    }

    @Test
    public void wlan_no_iwconfig_dbm_values() throws IOException, RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\nwlp2s0").mock());
        sessionMocker.withCommand("cat /sys/class/net/wlp2s0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("LC_ALL=C /usr/bin/whereis iwconfig",
                new CommandMocker().withResponse("whereis: not found").withExitStatus(127).mock());
        sessionMocker.withCommand("ip -f inet addr show dev wlp2s0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        sessionMocker.withCommand("cat /proc/net/wireless", new CommandMocker()
                .withResponse(
                        "Inter-| sta-|   Quality        |   Discarded packets               | Missed | WE"
                                + "\n"
                                + " face | tus | link level noise |  nwid  crypt   frag  retry   misc | beacon | 22"
                                + "\n"
                                + " wlp2s0: 0000 70. -45. 0. 0 0 0 0 0 0\n").mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation wlan0 = interfaces.get(0);
        assertEquals("wlp2s0", wlan0.getName());
        assertEquals(true, wlan0.isHasCarrier());
        assertEquals("192.168.0.9", wlan0.getIpAdress());
        WlanBean wlanInfo = wlan0.getWlanInfo();
        assertNotNull(wlanInfo);
        assertEquals(100, wlanInfo.getLinkQuality().intValue());
        assertEquals(100, wlanInfo.getSignalLevel().intValue());
    }


}
