package de.eidottermihi.rpicheck.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.eidottermihi.rpicheck.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

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
		Assert.assertEquals(1, interfaces.size());
		NetworkInterfaceInformation eth0 = interfaces.get(0);
		Assert.assertEquals("eth0", eth0.getName());
		Assert.assertEquals(true, eth0.isHasCarrier());
		Assert.assertEquals("192.168.0.9", eth0.getIpAdress());
		Assert.assertNull(eth0.getWlanInfo());
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
		Assert.assertEquals(1, interfaces.size());
		NetworkInterfaceInformation eth0 = interfaces.get(0);
		Assert.assertEquals("eth0", eth0.getName());
		Assert.assertEquals(true, eth0.isHasCarrier());
		Assert.assertEquals("192.168.0.9", eth0.getIpAdress());
		Assert.assertNull(eth0.getWlanInfo());
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
		Assert.assertEquals(1, interfaces.size());
		NetworkInterfaceInformation eth0 = interfaces.get(0);
		Assert.assertEquals("eth0", eth0.getName());
		Assert.assertEquals(true, eth0.isHasCarrier());
		Assert.assertNull(eth0.getIpAdress());
		Assert.assertNull(eth0.getWlanInfo());
	}

}
