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
package de.eidottermihi.rpicheck.ssh.impl;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.ProcessBean;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;

/**
 * Simple API for interacting with a Raspberry Pi computer over SSH.
 */
public class RaspiQuery implements IQueryService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RaspiQuery.class);

    private static final int FREQ_ARM = 0;
    private static final int FREQ_CORE = 1;

    private static final Pattern CPU_PATTERN = Pattern.compile("[0-9.]{4,}");
    private static final String DISK_USAGE_CMD = "LC_ALL=C df -h";
    private static final String DF_COMMAND_HEADER_START = "Filesystem";
    private static final String DISTRIBUTION_CMD = "cat /etc/*-release | grep PRETTY_NAME";
    private static final String PROCESS_NO_ROOT_CMD = "ps -U root -u root -N";
    private static final String PROCESS_ALL = "ps -A";
    private static final String N_A = "n/a";

    private static final int DEFAULT_SSH_PORT = 22;
    private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";
    private SSHClient client;
    private String hostname;
    private String username;
    private int port = DEFAULT_SSH_PORT;

    static {
        Security.removeProvider(BOUNCY_CASTLE_PROVIDER_NAME);
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * Initialize a new RaspiQuery.
     *
     * @param host hostname or ip adress of a running raspberry pi
     * @param user username for ssh login
     * @param port ssh port to use (if null, default will be used)
     */
    public RaspiQuery(final String host, final String user, final Integer port) {
        final Provider[] providers = Security.getProviders();
        LOGGER.debug("+++ Registered JCE providers +++");
        for (Provider prov : providers) {
            LOGGER.debug("Provider: {} - {}", prov.getName(), prov.getInfo());
        }
        final Set<String> signatures = Security.getAlgorithms("signature");
        LOGGER.debug("+++ Availabe signatures +++");
        for (String sig : signatures) {
            LOGGER.debug("Signature: {}", sig);
        }
        if (Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("hostname should not be blank.");
        } else if (Strings.isNullOrEmpty(user)) {
            throw new IllegalArgumentException("username should not be blank.");
        } else {
            LOGGER.info("Initialiazed new RaspiQuery for host {} on port {}", host, port);
            this.hostname = host;
            this.username = user;
            if (port != null) {
                this.port = port;
            }
        }
    }

    /**
     * Queries the current CPU temperature.
     *
     * @param vcgencmdPath the path to vcgencmd
     * @return the temperature in Celsius
     * @throws RaspiQueryException if something goes wrong
     */
    private Double queryCpuTemp(String vcgencmdPath)
            throws RaspiQueryException {
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    final String cmdString = vcgencmdPath + " measure_temp";
                    final Command cmd = session.exec(cmdString);
                    cmd.join(30, TimeUnit.SECONDS);
                    String output = IOUtils.readFully(cmd.getInputStream()).toString();
                    return this.parseTemperature(output);
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }
    }

    /**
     * Queries the current cpu frequency.
     *
     * @param unit         cpu or arm
     * @param vcgencmdPath the path of the vcgendcmd tool
     * @return the frequency in hz
     * @throws RaspiQueryException if something goes wrong
     */
    private long queryFreq(final int unit, final String vcgencmdPath)
            throws RaspiQueryException {
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    String cmdString = vcgencmdPath + " measure_clock";
                    if (unit == FREQ_ARM) {
                        cmdString += " arm";
                    } else if (unit == FREQ_CORE) {
                        cmdString += " core";
                    } else {
                        return 0;
                    }
                    final Command cmd = session.exec(cmdString);
                    cmd.join(30, TimeUnit.SECONDS);
                    String output = IOUtils.readFully(cmd.getInputStream()).toString();
                    return this.parseFrequency(output);
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryVcgencmd()
     */
    @Override
    public final VcgencmdBean queryVcgencmd() throws RaspiQueryException {
        LOGGER.debug("Querying vcgencmd...");
        // first, find the location of vcgencmd
        final String vcgencmdPath = findVcgencmd().orNull();
        if (vcgencmdPath == null) {
            throw RaspiQueryException.createVcgencmdNotFound();
        }
        // create Bean
        final VcgencmdBean bean = new VcgencmdBean();
        bean.setArmFrequency(this.queryFreq(FREQ_ARM, vcgencmdPath));
        bean.setCoreFrequency(this.queryFreq(FREQ_CORE, vcgencmdPath));
        bean.setCoreVolts(this.queryVolts(vcgencmdPath));
        bean.setCpuTemperature(this.queryCpuTemp(vcgencmdPath));
        bean.setVersion(this.queryFirmwareVersion(vcgencmdPath));
        return bean;
    }

    /**
     * Queries the firmware version.
     *
     * @param vcgencmdPath path to vcgencmd
     * @return the firmware Version
     * @throws RaspiQueryException if something goes wrong
     */
    public String queryFirmwareVersion(String vcgencmdPath)
            throws RaspiQueryException {
        return QueryFactory.makeFirmwareQuery(client, vcgencmdPath).run();
    }

    /**
     * Checks the known paths to the vcgencmd executable and return the first
     * path found.
     *
     * @return the path or <code>null</code>, when vcgencmd was not found
     * @throws RaspiQueryException if something goes wrong
     */
    private Optional<String> findVcgencmd() throws RaspiQueryException {
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                try {
                    final String[] pathsToCheck = new String[]{"vcgencmd", "/usr/bin/vcgencmd", "/opt/vc/bin/vcgencmd"};
                    String foundPath = null;
                    for (int i = 0; i < pathsToCheck.length; i++) {
                        String guessedPath = pathsToCheck[i];
                        if (isValidVcgencmdPath(guessedPath, client)) {
                            foundPath = guessedPath;
                            break;
                        }
                    }
                    if (foundPath != null) {
                        LOGGER.info("Found vcgencmd in path: {}.", foundPath);
                        return Optional.of(foundPath);
                    } else {
                        LOGGER.error("vcgencmd was not found. Verify that vcgencmd is available in /usr/bin or /opt/vc/bin and make sure your user is in group 'video'.");
                        return Optional.absent();
                    }
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname, e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }
    }

    /**
     * Checks if the path is a correct path to vcgencmd.
     *
     * @param path   the path to check
     * @param client authenticated and open client
     * @return true, if correct, false if not
     * @throws IOException if something ssh related goes wrong
     */
    private boolean isValidVcgencmdPath(String path, SSHClient client) throws IOException {
        final Session session = client.startSession();
        session.allocateDefaultPTY();
        LOGGER.debug("Checking vcgencmd location: {}", path);
        final Command cmd = session.exec(path);
        cmd.join(30, TimeUnit.SECONDS);
        session.close();
        final Integer exitStatus = cmd.getExitStatus();
        final String output = IOUtils.readFully(cmd.getInputStream()).toString().toLowerCase();
        LOGGER.debug("Path check output: {}", output);
        return exitStatus != null && exitStatus.equals(0)
                && !output.contains("not found") && !output.contains("no such file or directory");
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryNetworkInformation()
     */
    @Override
    public List<NetworkInterfaceInformation> queryNetworkInformation()
            throws RaspiQueryException {
        return QueryFactory.makeNetworkInformationQuery(client).run();
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#queryVolts(java.lang.String)
     */
    @Override
    public final Double queryVolts(String vcgencmdPath)
            throws RaspiQueryException {
        LOGGER.info("Querying core volts...");
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    final String cmdString = vcgencmdPath
                            + " measure_volts core";
                    final Command cmd = session.exec(cmdString);
                    cmd.join(30, TimeUnit.SECONDS);
                    final String output = IOUtils.readFully(
                            cmd.getInputStream()).toString();
                    return this.formatVolts(output);
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryUptime()
     */
    @Override
    public final double queryUptime() throws RaspiQueryException {
        return QueryFactory.makeUptimeQuery(client).run();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryCpuSerial()
     */
    @Override
    public final String queryCpuSerial() throws RaspiQueryException {
        return QueryFactory.makeSerialNoQuery(client).run();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryMemoryInformation()
     */
    @Override
    public final RaspiMemoryBean queryMemoryInformation()
            throws RaspiQueryException {
        return QueryFactory.makeMemoryQuery(client).run();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryDiskUsage()
     */
    @Override
    public final List<DiskUsageBean> queryDiskUsage()
            throws RaspiQueryException {
        LOGGER.info("Querying disk usage...");
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    final Command cmd = session.exec(DISK_USAGE_CMD);
                    cmd.join(30, TimeUnit.SECONDS);
                    return this.parseDiskUsage(IOUtils
                            .readFully(cmd.getInputStream()).toString().trim());
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryDistributionName()
     */
    @Override
    public final String queryDistributionName() throws RaspiQueryException {
        LOGGER.info("Querying distribution name...");
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    final Command cmd = session.exec(DISTRIBUTION_CMD);
                    cmd.join(30, TimeUnit.SECONDS);
                    return this.parseDistribution(IOUtils.readFully(cmd.getInputStream()).toString().trim());
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException(
                        "You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
    }

    @Override
    public final String querySystemtime() throws RaspiQueryException {
        return QueryFactory.makeSystemTimeQuery(client).run();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#queryProcesses(boolean)
     */
    @Override
    public final List<ProcessBean> queryProcesses(boolean showRootProcesses)
            throws RaspiQueryException {
        LOGGER.info("Querying running processes...");
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    final Command cmd = session.exec(showRootProcesses ? PROCESS_ALL : PROCESS_NO_ROOT_CMD);
                    cmd.join(30, TimeUnit.SECONDS);
                    return this.parseProcesses(IOUtils.readFully(cmd.getInputStream()).toString().trim());
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException(
                        "You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#sendRebootSignal(java.lang
     * .String)
     */
    @Override
    public final void sendRebootSignal(String sudoPassword)
            throws RaspiQueryException {
        if (sudoPassword == null) {
            LOGGER.info("No sudo password for reboot specified. Using empty password instead.");
            sudoPassword = "";
        }
        final StringBuilder sb = new StringBuilder();
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    session.allocateDefaultPTY();
                    final String command = sb.append("echo ").append("\"")
                            .append(sudoPassword).append("\"")
                            .append(" | sudo -S /sbin/shutdown -r now")
                            .toString();
                    final String rebootCmdLogger = "echo \"??SUDO_PW??\" | sudo -S /sbin/shutdown -r now";
                    LOGGER.info("Sending reboot command: {}", rebootCmdLogger);
                    Command cmd = session.exec(command);
                    try {
                        cmd.join();
                        session.join();
                    } catch (ConnectionException e) {
                        LOGGER.debug("ConnectException while sending reboot command. Probably system is going down...", e);
                        return;
                    }
                    if (cmd.getExitStatus() != null && cmd.getExitStatus() != 0) {
                        LOGGER.warn("Sudo unknown: Trying \"reboot\"...");
                        // openelec running
                        session = client.startSession();
                        session.allocateDefaultPTY();
                        cmd = session.exec("reboot");
                        try {
                            cmd.join();
                            LOGGER.debug("join successful after 'reboot'.");
                        } catch (ConnectionException e) {
                            // system went down
                            LOGGER.debug("ConnectException while sending reboot command. Probably system is going down...", e);
                        }
                    }
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException(
                        "You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#sendHaltSignal(java.lang.String
     * )
     */
    @Override
    public final void sendHaltSignal(String sudoPassword)
            throws RaspiQueryException {
        if (sudoPassword == null) {
            LOGGER.info("No sudo password for halt specified. Using empty password instead.");
            sudoPassword = "";
        }
        final StringBuilder sb = new StringBuilder();
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    session.allocateDefaultPTY();
                    final String command = sb.append("echo ").append("\"")
                            .append(sudoPassword).append("\"")
                            .append(" | sudo -S /sbin/shutdown -h now")
                            .toString();
                    final String haltCmdLogger = "echo \"??SUDO_PW??\" | sudo -S /sbin/shutdown -h now";
                    LOGGER.info("Sending halt command: {}", haltCmdLogger);
                    Command cmd = session.exec(command);
                    try {
                        cmd.join();
                        session.join();
                    } catch (ConnectionException e) {
                        LOGGER.debug("ConnectException while sending halt command. Probably system is going down...", e);
                        return;
                    }
                    if (cmd.getExitStatus() != null && cmd.getExitStatus() != 0) {
                        // openelec running
                        session = client.startSession();
                        session.allocateDefaultPTY();
                        LOGGER.warn("Sudo unknown: Trying \"halt\"...");
                        cmd = session.exec("halt");
                        try {
                            cmd.join();
                            LOGGER.debug("join successful after 'halt'.");
                        } catch (ConnectionException e) {
                            // system went down
                            LOGGER.debug("ConnectException while sending halt command. Probably system is going down...", e);
                        }
                    }
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException(
                        "You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
    }

    /**
     * Parses the output of the ps command.
     *
     * @param output
     * @return List with processes
     */
    private List<ProcessBean> parseProcesses(String output) {
        final List<String> lines = Splitter.on("\n").trimResults()
                .splitToList(output);
        final List<ProcessBean> processes = new LinkedList<ProcessBean>();
        int count = 0;
        for (String line : lines) {
            if (count == 0) {
                // first line
                count++;
                continue;
            }
            // split line at whitespaces
            final List<String> cols = Splitter.on(CharMatcher.WHITESPACE)
                    .omitEmptyStrings().trimResults().splitToList(line);
            if (cols.size() >= 4) {
                try {
                    // command may contain whitespace, so join again
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 3; i < cols.size(); i++) {
                        sb.append(cols.get(i)).append(' ');
                    }
                    processes.add(new ProcessBean(
                            Integer.parseInt(cols.get(0)), cols.get(1), cols
                            .get(2), sb.toString()));
                } catch (NumberFormatException e) {
                    LOGGER.error("Could not parse processes.");
                    LOGGER.error("Error occured on following line: {}", line);
                }
            } else {
                LOGGER.error("Line[] length: {}", cols.size());
                LOGGER.error("Expcected another output of ps. Skipping line: {}", line);
            }
        }
        return processes;
    }

    private String parseDistribution(String output) {
        final String[] split = output.trim().split("=");
        if (split.length >= 2) {
            final String distriWithApostroph = split[1];
            return distriWithApostroph.replace("\"", "");
        } else {
            LOGGER.error("Could not parse distribution. Make sure 'cat /etc/*-release' works on your distribution.");
            LOGGER.error("Output of {}: \n{}", DISTRIBUTION_CMD, output);
            return N_A;
        }
    }

    /**
     * Parses the output of the disk usage commando.
     *
     * @param output the output
     * @return a List with {@link DiskUsageBean}
     */
    private List<DiskUsageBean> parseDiskUsage(String output) {
        final String[] lines = output.split("\n");
        final List<DiskUsageBean> disks = new LinkedList<DiskUsageBean>();
        for (String line : lines) {
            if (line.startsWith(DF_COMMAND_HEADER_START)) {
                continue;
            }
            // split string at whitespaces
            final String[] linesSplitted = line.split("\\s+");
            if (linesSplitted.length >= 6) {
                String filesystem = linesSplitted[0];
                String size = linesSplitted[1];
                String used = linesSplitted[2];
                String free = linesSplitted[3];
                String usedPercentage = linesSplitted[4];
                String mountpoint = linesSplitted[5];
                if (linesSplitted.length > 6) {
                    // whitespace in mountpoint path
                    StringBuilder sb = new StringBuilder();
                    for (int i = 5; i < linesSplitted.length; i++) {
                        sb.append(linesSplitted[i]);
                        if (i != linesSplitted.length - 1) {
                            sb.append(" ");
                        }
                    }
                    mountpoint = sb.toString();
                }
                if (filesystem.length() > 20) {
                    // shorten filesystem
                    LOGGER.debug("Shorten filesystem: {}", filesystem);
                    filesystem = ".." + filesystem.substring(filesystem.length() - 21, filesystem.length());
                }
                disks.add(new DiskUsageBean(filesystem, size, used, free, usedPercentage, mountpoint));
            } else {
                LOGGER.warn("Expected another output of df -h. Skipping line: {}", line);
            }
        }
        LOGGER.debug("Disks: {}", disks);
        return disks;
    }

    private Double formatVolts(String output) {
        final String[] splitted = output.trim().split("=");
        if (splitted.length >= 2) {
            final String voltsWithUnit = splitted[1];
            final String volts = voltsWithUnit.substring(0, voltsWithUnit.length() - 1);
            return Double.parseDouble(volts);
        } else {
            LOGGER.error("Could not parse cpu voltage.");
            LOGGER.error("Output of 'vcgencmd measure_volts core': \n{}", output);
            return 0D;
        }
    }

    /**
     * Formats the output of the vcgencmd measure_temp command.
     *
     * @param output output of vcgencmd
     * @return formatted temperature string
     */
    private Double parseTemperature(final String output) {
        final Matcher m = CPU_PATTERN.matcher(output);
        if (m.find()) {
            final String formatted = m.group().trim();
            return Double.parseDouble(formatted);
        } else {
            LOGGER.error("Could not parse cpu temperature.");
            LOGGER.error("Output of 'vcgencmd measure_temp': \n{}", output);
            return 0D;
        }
    }

    /**
     * Parse the output of the command "vcgencmd measure_clock [core/arm]".
     *
     * @param output
     * @return the frequency in Hz
     */
    private long parseFrequency(final String output) {
        final String[] splitted = output.trim().split("=");
        long formatted = 0;
        if (splitted.length >= 2) {
            try {
                formatted = Long.parseLong(splitted[1]);
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse frequency.");
                LOGGER.error("Output of 'vcgencmd measure_clock [core/arm]': \n{}",
                        output);
            }
        } else {
            LOGGER.error("Could not parse frequency.");
            LOGGER.error("Output of 'vcgencmd measure_clock [core/arm]': \n{}",
                    output);
        }
        return formatted;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#connect(java.lang.String)
     */
    @Override
    public final void connect(String password) throws RaspiQueryException {
        LOGGER.info("Connecting to host '{}' on port '{}'.", hostname, port);
        client = newAndroidSSHClient();
        LOGGER.info("Using no host key verification.");
        client.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            client.connect(hostname, port);
            client.authPassword(username, password);
        } catch (UserAuthException e) {
            throw RaspiQueryException.createAuthenticationFailure(hostname,
                    username, e);
        } catch (TransportException e) {
            throw RaspiQueryException.createTransportFailure(hostname, e);
        } catch (IOException e) {
            throw RaspiQueryException.createConnectionFailure(hostname, port, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#connectWithPubKeyAuth(java
     * .lang.String)
     */
    @Override
    public final void connectWithPubKeyAuth(final String keyfilePath)
            throws RaspiQueryException {
        LOGGER.info("Connecting to host '{}' on port '{}'.", hostname, port);
        client = newAndroidSSHClient();
        LOGGER.info("Using no host key verification.");
        client.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            client.connect(hostname, port);
            LOGGER.info("Using private/public key authentication, keyfile: '{}'", keyfilePath);
            KeyProvider keyProvider = client.loadKeys(keyfilePath);
            client.authPublickey(username, keyProvider);
        } catch (UserAuthException e) {
            LOGGER.info("Authentication failed.", e);
            throw RaspiQueryException.createAuthenticationFailure(hostname,
                    username, e);
        } catch (TransportException e) {
            throw RaspiQueryException.createTransportFailure(hostname, e);
        } catch (IOException e) {
            throw RaspiQueryException
                    .createConnectionFailure(hostname, port, e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#connectWithPubKeyAuthAndPassphrase
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void connectWithPubKeyAuthAndPassphrase(String path,
                                                   String privateKeyPass) throws RaspiQueryException {
        LOGGER.info("Connecting to host '{}' on port '{}'.", hostname, port);
        client = newAndroidSSHClient();
        LOGGER.info("Using no host key verification.");
        client.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            client.connect(hostname, port);
        } catch (IOException e) {
            throw RaspiQueryException.createConnectionFailure(hostname, port, e);
        }
        try {
            LOGGER.info("Using private/public key authentification with encrypted privatekey, keyfile: '{}'", path);
            final KeyProvider keyProvider = client.loadKeys(path, privateKeyPass.toCharArray());
            client.authPublickey(username, keyProvider);
        } catch (UserAuthException e) {
            LOGGER.info("Authentification failed.", e);
            throw RaspiQueryException.createAuthenticationFailure(hostname, username, e);
        } catch (TransportException e) {
            throw RaspiQueryException.createTransportFailure(hostname, e);
        } catch (IOException e) {
            throw RaspiQueryException.createIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#disconnect()
     */
    @Override
    public final void disconnect() throws RaspiQueryException {
        if (client != null) {
            if (client.isConnected()) {
                try {
                    LOGGER.info("Disconnecting from host {}.",
                            this.getHostname());
                    client.disconnect();
                } catch (IOException e) {
                    // dont throw, just log
                    LOGGER.warn("Caught exception during disconnect: {}", e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#getHostname()
     */
    @Override
    public String getHostname() {
        return hostname;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.eidottermihi.rpicheck.ssh.IQueryService#setHostname(java.lang.String)
     */
    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.eidottermihi.rpicheck.ssh.IQueryService#run(java.lang.String)
     */
    @Override
    public String run(String command, int timeout) throws RaspiQueryException {
        LOGGER.info("Running custom command: {}", command);
        if (client != null) {
            if (client.isConnected() && client.isAuthenticated()) {
                Session session;
                try {
                    session = client.startSession();
                    session.allocateDefaultPTY();
                    final Command cmd = session.exec(command);
                    cmd.join(timeout, TimeUnit.SECONDS);
                    cmd.close();
                    final String output = IOUtils.readFully(cmd.getInputStream()).toString();
                    final String error = IOUtils.readFully(cmd.getErrorStream()).toString();
                    final StringBuilder sb = new StringBuilder();
                    final String out = sb.append(output).append(error).toString();
                    LOGGER.debug("Output of '{}': {}", command, out);
                    session.close();
                    return out;
                } catch (IOException e) {
                    throw RaspiQueryException.createTransportFailure(hostname,
                            e);
                }
            } else {
                throw new IllegalStateException("You must establish a connection first.");
            }
        } else {
            throw new IllegalStateException("You must establish a connection first.");
        }

    }

    /**
     * @return SSHClient with Android Configuration
     */

    public SSHClient newAndroidSSHClient() {
        return new SSHClient(new AndroidConfig());
    }

    @Override
    public double queryLoadAverage(LoadAveragePeriod timePeriod)
            throws RaspiQueryException {
        return QueryFactory.makeLoadAvgQuery(client, timePeriod).run();
    }

}
