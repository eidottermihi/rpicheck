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
package de.eidottermihi.rpicheck.ssh.beans;

import java.io.Serializable;

/**
 * Bean for storing process information.
 *
 * @author Michael
 */
public class ProcessBean implements Serializable {
    private static final long serialVersionUID = 3421726611868018755L;
    private int pId;
    private String tty;
    private String cpuTime;
    private String command;

    public ProcessBean(int pId, String tty, String cpuTime, String command) {
        this.pId = pId;
        this.tty = tty;
        this.cpuTime = cpuTime;
        this.command = command;
    }

    @Exported
    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    public String getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(String cpuTime) {
        this.cpuTime = cpuTime;
    }

    @Exported
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "ProcessBean [pId=" + pId + ", tty=" + tty + ", cpuTime="
                + cpuTime + ", command=" + command + "]";
    }

}
