/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
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

public class MemoryBean implements Serializable {
    private static final long serialVersionUID = -6431484424293280487L;

    private long bytes;

    private MemoryBean(long bytes) {
        this.setBytes(bytes);
    }

    public static MemoryBean from(Memory scale, long data) {
        final MemoryBean memoryBean = new MemoryBean(0);
        if (scale == Memory.B) {
            memoryBean.setBytes(data * Memory.B.getScale());
        } else if (scale == Memory.KB) {
            memoryBean.setBytes(data * Memory.KB.getScale());
        } else if (scale == Memory.MB) {
            memoryBean.setBytes(data * Memory.MB.getScale());
        } else if (scale == Memory.GB) {
            memoryBean.setBytes(data * Memory.GB.getScale());
        } else if (scale == Memory.TB) {
            memoryBean.setBytes(data * Memory.GB.getScale());
        }
        return memoryBean;
    }

    public String humanReadableByteCount(boolean si) {
        int unit = si ? 1000 : 1024;
        if (getBytes() < unit)
            return getBytes() + " B";
        int exp = (int) (Math.log(getBytes()) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (si ? "" : "i");
        return String.format("%.1f %sB", getBytes() / Math.pow(unit, exp), pre);
    }

    @Exported
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

}
