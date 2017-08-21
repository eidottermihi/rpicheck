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
package de.eidottermihi.rpicheck.activity;

import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.ProcessBean;
import de.eidottermihi.rpicheck.beans.QueryBean;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.beans.UptimeBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * @author Michael
 */
public class SSHQueryTask extends AsyncTask<String, Integer, QueryBean> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SSHQueryTask.class);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat
            .getPercentInstance();

    private final AsyncQueryDataUpdate delegate;

    private IQueryService queryService;
    private LoadAveragePeriod loadAveragePeriod;

    public SSHQueryTask(AsyncQueryDataUpdate delegate,
                        LoadAveragePeriod loadAvgPeriod) {
        super();
        this.delegate = delegate;
        this.loadAveragePeriod = loadAvgPeriod;
    }

    public SSHQueryTask(AsyncQueryDataUpdate delegate) {
        super();
        this.delegate = delegate;
        this.loadAveragePeriod = LoadAveragePeriod.FIVE_MINUTES;
    }

    @Override
    protected QueryBean doInBackground(String... params) {
        // create and do query
        queryService = new RaspiQuery((String) params[0], (String) params[1],
                Integer.parseInt(params[3]));
        final String pass = params[2];
        boolean hideRootProcesses = Boolean.parseBoolean(params[4]);
        final String privateKeyPath = params[5];
        final String privateKeyPass = params[6];
        QueryBean bean = new QueryBean();
        final long msStart = new Date().getTime();
        bean.setErrorMessages(new ArrayList<String>());
        try {
            publishProgress(5);
            if (privateKeyPath != null) {
                File f = new File(privateKeyPath);
                if (privateKeyPass == null) {
                    // connect with private key only
                    queryService.connectWithPubKeyAuth(f.getPath());
                } else {
                    // connect with key and passphrase
                    queryService.connectWithPubKeyAuthAndPassphrase(
                            f.getPath(), privateKeyPass);
                }
            } else {
                queryService.connect(pass);
            }
            publishProgress(20);
            final VcgencmdBean vcgencmdBean = queryService.queryVcgencmd();
            publishProgress(40);
            final Double loadAvg = queryService
                    .queryLoadAverage(this.loadAveragePeriod);
            publishProgress(50);
            final Double uptime = queryService.queryUptime();
            publishProgress(60);
            RaspiMemoryBean memory = queryService.queryMemoryInformation();
            publishProgress(70);
            String serialNo = queryService.queryCpuSerial();
            publishProgress(72);
            List<ProcessBean> processes = queryService
                    .queryProcesses(!hideRootProcesses);
            publishProgress(80);
            final List<NetworkInterfaceInformation> networkInformation = queryService
                    .queryNetworkInformation();
            publishProgress(90);
            bean.setDisks(queryService.queryDiskUsage());
            publishProgress(95);
            bean.setDistri(queryService.queryDistributionName());
            queryService.disconnect();
            publishProgress(100);
            bean.setVcgencmdInfo(vcgencmdBean);
            bean.setLastUpdate(Calendar.getInstance().getTime());
            bean.setStartup(new UptimeBean(uptime).getRunningPretty());
            bean.setAvgLoad(NUMBER_FORMAT.format(loadAvg));
            if (memory.getErrorMessage() != null) {
                bean.getErrorMessages().add(memory.getErrorMessage());
            } else {
                bean.setFreeMem(memory.getTotalFree());
                bean.setTotalMem(memory.getTotalMemory());
            }
            bean.setSerialNo(serialNo);
            bean.setNetworkInfo(networkInformation);
            bean.setProcesses(processes);
            for (String error : bean.getErrorMessages()) {
                LOGGER.error(error);
            }
        } catch (RaspiQueryException e) {
            LOGGER.error(e.getMessage(), e);
            bean.setException(e);
        } finally {
            try {
                queryService.disconnect();
            } catch (RaspiQueryException e) {
                LOGGER.debug("Error closing the ssh client", e);
            }
        }
        final long msFinish = new Date().getTime();
        final long durationInMs = msFinish - msStart;
        LOGGER.debug("Query time: {} ms.", durationInMs);
        return bean;
    }

    @Override
    protected void onPostExecute(QueryBean result) {
        // update query data
        delegate.onQueryFinished(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        final Integer totalProgress = values[0];
        delegate.onQueryProgress(totalProgress);
        super.onProgressUpdate(values);
    }

}