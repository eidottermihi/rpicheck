package de.eidottermihi.rpicheck.ssh;

import java.io.IOException;
import java.util.List;

import de.eidottermihi.rpicheck.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.beans.ProcessBean;
import de.eidottermihi.rpicheck.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public interface IQueryService {

	/**
	 * Queries information available via vcgencmd (temperature, frequencies,
	 * firmware version, ...).
	 * 
	 * @return a {@link VcgencmdBean} with the data
	 * @throws RaspiQueryException
	 *             when vcgencmd was not found on the machine
	 */
	public abstract VcgencmdBean queryVcgencmd() throws RaspiQueryException;

	/**
	 * Queries network information.
	 * 
	 * @return a List with informations to every interface found (loopback
	 *         excluded).
	 * @throws RaspiQueryException
	 *             if something goes wrong.
	 */
	public abstract List<NetworkInterfaceInformation> queryNetworkInformation()
			throws RaspiQueryException;

	/**
	 * Queries the current voltage.
	 * 
	 * @param vcgencmdPath
	 *            path to "vcgendcmd"
	 * @return the current voltage
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract Double queryVolts(String vcgencmdPath)
			throws RaspiQueryException;

	/**
	 * Queries uptime of the system (via cat /proc/uptime).
	 * 
	 * @return uptime in seconds
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract double queryUptime() throws RaspiQueryException;

	/**
	 * Queries the load average (via cat /proc/loadavg).
	 * 
	 * @param timePeriod
	 *            time period for calculation of the load average
	 * @return the load average (min 0, max 1)
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract double queryLoadAverage(LoadAveragePeriod timePeriod)
			throws RaspiQueryException;

	/**
	 * Queries the cpu serial.
	 * 
	 * @return the cpu serial
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract String queryCpuSerial() throws RaspiQueryException;

	/**
	 * Query memory information.
	 * 
	 * @return a {@link RaspiMemoryBean}
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract RaspiMemoryBean queryMemoryInformation()
			throws RaspiQueryException;

	/**
	 * Query the disk usage of the raspberry pi.
	 * 
	 * @return a List with information for every disk
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract List<DiskUsageBean> queryDiskUsage()
			throws RaspiQueryException;

	/**
	 * Queries the distribution name.
	 * 
	 * @return the distribution name
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract String queryDistributionName() throws RaspiQueryException;

	/**
	 * Queries the running processes.
	 * 
	 * @param showRootProcesses
	 *            if processes of root should be shown
	 * @return List with running processes
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 */
	public abstract List<ProcessBean> queryProcesses(boolean showRootProcesses)
			throws RaspiQueryException;

	/**
	 * Inits a reboot via command 'sudo /sbin/shutdown -r now'. For shutdown,
	 * root privileges are required.
	 * 
	 * @param sudoPassword
	 *            the sudo password
	 * @throws RaspiQueryException
	 */
	public abstract void sendRebootSignal(String sudoPassword)
			throws RaspiQueryException;

	/**
	 * Inits a system halt via command 'sudo /sbin/shutdown -h now'. For halt,
	 * root privileges are required.
	 * 
	 * @param sudoPassword
	 *            the sudo password
	 * @throws RaspiQueryException
	 */
	public abstract void sendHaltSignal(String sudoPassword)
			throws RaspiQueryException;

	/**
	 * Establishes a ssh connection to a raspberry pi via ssh.
	 * 
	 * @param password
	 *            the ssh password
	 * @throws RaspiQueryException
	 *             - if connection, authentication or transport fails
	 */
	public abstract void connect(String password) throws RaspiQueryException;

	/**
	 * Establishes a ssh connection with public key authentification.
	 * 
	 * @param keyfilePath
	 *            path to the private key file in PKCS11/OpenSSH format
	 * @throws RaspiQueryException
	 */
	public abstract void connectWithPubKeyAuth(String keyfilePath)
			throws RaspiQueryException;

	/**
	 * Establishes a ssh connection with public key authentification.
	 * 
	 * @param path
	 *            path to the private key file in PKCS11/OpenSSH format
	 * @throws RaspiQueryException
	 */
	public abstract void connectWithPubKeyAuthAndPassphrase(String path,
			String privateKeyPass) throws RaspiQueryException;

	/**
	 * Disconnects the current client.
	 * 
	 * @throws RaspiQueryException
	 *             if something goes wrong
	 * 
	 * @throws IOException
	 *             if something goes wrong
	 */
	public abstract void disconnect() throws RaspiQueryException;

	public abstract String getFile(String path);

	public abstract String getHostname();

	public abstract void setHostname(String hostname);

	/**
	 * Runs the specified command.
	 * 
	 * @param command
	 *            the command to run
	 * @throws RaspiQueryException
	 *             when something goes wrong
	 */
	public abstract String run(String command) throws RaspiQueryException;

}