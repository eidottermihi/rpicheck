/**
 * Copyright (C) 2017  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.eidottermihi.rpicheck.test;

import org.junit.Test;

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
    public void wlan() throws RaspiQueryException {
        sessionMocker.withCommand("ls -1 /sys/class/net", new CommandMocker()
                .withResponse("lo\nwlan0").mock());
        sessionMocker.withCommand("cat /sys/class/net/wlan0/carrier",
                new CommandMocker().withResponse("1").mock());
        sessionMocker.withCommand("ip -f inet addr show dev wlan0 | sed -n 2p",
                new CommandMocker().withResponse("192.168.0.9").mock());
        sessionMocker.withCommand("cat /proc/net/wireless", new CommandMocker()
                .withResponse(
                        "Inter-| sta-|   Quality        |   Discarded packets               | Missed | WE"
                                + "\n"
                                + " face | tus | link level noise |  nwid  crypt   frag  retry   misc | beacon | 22"
                                + "\n"
                                + " wlan0: 0000 100. 95. 0. 0 0 0 0 0 0\n").mock());
        List<NetworkInterfaceInformation> interfaces = raspiQuery
                .queryNetworkInformation();
        assertEquals(1, interfaces.size());
        NetworkInterfaceInformation wlan0 = interfaces.get(0);
        assertEquals("wlan0", wlan0.getName());
        assertEquals(true, wlan0.isHasCarrier());
        assertEquals("192.168.0.9", wlan0.getIpAdress());
        WlanBean wlanInfo = wlan0.getWlanInfo();
        assertNotNull(wlanInfo);
        assertEquals(100, wlanInfo.getLinkQuality().intValue());
        assertEquals(95, wlanInfo.getSignalLevel().intValue());

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

}
