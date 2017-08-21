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

public enum Memory {
    B("Byte", "B", 1), KB("KiloByte", "KB", 1024), MB("MegaByte", "MB",
    1024 * 1024), GB("GigaByte", "GB", 1024 * 1024 * 1024), TB(
    "TeraByte", "TB", 1024 * 1024 * 1024 * 1024);

    private String longName;
    private String shortName;
    private long scale;

    private Memory(String name, String shortName, long scale) {
        this.longName = name;
        this.shortName = shortName;
        this.scale = scale;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public long getScale() {
        return scale;
    }

}
