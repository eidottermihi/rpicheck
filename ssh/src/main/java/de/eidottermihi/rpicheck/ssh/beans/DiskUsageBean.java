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
