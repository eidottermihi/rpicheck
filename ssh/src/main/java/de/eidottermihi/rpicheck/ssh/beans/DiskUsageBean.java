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
 * Wraps the fields of a "df -h" output. Example: <br/>
 * Filesystem Size Used Avail Use% Mounted on <br/>
 * rootfs 3.6G 606M 2.9G 17% / <br/>
 * /dev/root 3.6G 606M 2.9G 17% / <br/>
 * devtmpfs 201M 0 201M 0% /dev <br/>
 * tmpfs 41M 536K 40M 2% /run <br/>
 * tmpfs 5.0M 0 5.0M 0% /run/lock <br/>
 * tmpfs 81M 0 81M 0% /run/shm <br/>
 * /dev/mmcblk0p1 34M 14M 21M 41% /boot
 *
 * @author Michael
 */
public class DiskUsageBean implements Serializable {
    private static final long serialVersionUID = 7826381063640779093L;

    private String fileSystem;
    private String size;
    private String used;
    private String available;
    private String usedPercent;
    private String mountedOn;

    public DiskUsageBean(String fileSystem, String size, String used,
                         String available, String usedPercent, String mountedOn) {
        super();
        this.fileSystem = fileSystem;
        this.size = size;
        this.used = used;
        this.available = available;
        this.usedPercent = usedPercent;
        this.mountedOn = mountedOn;
    }

    @Exported("filesystem")
    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Exported
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Exported
    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    @Exported
    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    @Exported
    public String getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(String usedPercent) {
        this.usedPercent = usedPercent;
    }

    @Exported
    public String getMountedOn() {
        return mountedOn;
    }

    public void setMountedOn(String mountedOn) {
        this.mountedOn = mountedOn;
    }

    @Override
    public String toString() {
        return "DiskUsageBean [fileSystem=" + fileSystem + ", size=" + size
                + ", used=" + used + ", available=" + available
                + ", usedPercent=" + usedPercent + ", mountedOn=" + mountedOn
                + "]";
    }

}
