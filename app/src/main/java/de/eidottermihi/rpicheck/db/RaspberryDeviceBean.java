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
package de.eidottermihi.rpicheck.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

import de.eidottermihi.rpicheck.beans.QueryBean;
import de.eidottermihi.rpicheck.ssh.beans.Exported;

public class RaspberryDeviceBean implements Serializable, Parcelable {
    public static final Parcelable.Creator<RaspberryDeviceBean> CREATOR = new Parcelable.Creator<RaspberryDeviceBean>() {
        public RaspberryDeviceBean createFromParcel(Parcel in) {
            return new RaspberryDeviceBean(in);
        }

        public RaspberryDeviceBean[] newArray(int size) {
            return new RaspberryDeviceBean[size];
        }
    };

    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_PUBLIC_KEY = "keys";
    public static final String AUTH_PUBLIC_KEY_WITH_PASSWORD = "keysWithPassword";
    public static final String[] SPINNER_AUTH_METHODS = {AUTH_PASSWORD, AUTH_PUBLIC_KEY, AUTH_PUBLIC_KEY_WITH_PASSWORD};

    private static final long serialVersionUID = -7054070923663258253L;
    private int id;
    private String name;
    private String host;
    private String user;
    private String pass;
    private int port;
    private String description;
    private String serial;
    private Date createdAt;
    private Date modifiedAt;
    private String sudoPass;
    private String authMethod;
    private String keyfilePath;
    private String keyfilePass;
    private int spinnerPosition;
    private QueryBean lastQueryData;

    public RaspberryDeviceBean(Parcel source) {
        /*
         * Reconstruct from the Parcel. Keep same order as in writeToParcel()
		 */
        id = source.readInt();
        name = source.readString();
        host = source.readString();
        user = source.readString();
        pass = source.readString();
        port = source.readInt();
        description = source.readString();
        serial = source.readString();
        createdAt = (Date) source.readSerializable();
        modifiedAt = (Date) source.readSerializable();
        sudoPass = source.readString();
        authMethod = source.readString();
        keyfilePath = source.readString();
        keyfilePass = source.readString();
        spinnerPosition = source.readInt();
        lastQueryData = (QueryBean) source.readSerializable();
    }

    public RaspberryDeviceBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(host);
        dest.writeString(user);
        dest.writeString(pass);
        dest.writeInt(port);
        dest.writeString(description);
        dest.writeString(serial);
        dest.writeSerializable(createdAt);
        dest.writeSerializable(modifiedAt);
        dest.writeString(sudoPass);
        dest.writeString(authMethod);
        dest.writeString(keyfilePath);
        dest.writeString(keyfilePass);
        dest.writeInt(spinnerPosition);
        dest.writeSerializable(lastQueryData);
    }

    @Exported
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Exported
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Exported
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Exported
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Exported
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Exported
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Exported
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Exported
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Exported
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exported
    public String getSudoPass() {
        return sudoPass;
    }

    public void setSudoPass(String sudoPass) {
        this.sudoPass = sudoPass;
    }

    public int getSpinnerPosition() {
        return spinnerPosition;
    }

    public void setSpinnerPosition(int spinnerPosition) {
        this.spinnerPosition = spinnerPosition;
    }

    public QueryBean getLastQueryData() {
        return lastQueryData;
    }

    public void setLastQueryData(QueryBean lastQueryData) {
        this.lastQueryData = lastQueryData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getKeyfilePath() {
        return keyfilePath;
    }

    public void setKeyfilePath(String keyfilePath) {
        this.keyfilePath = keyfilePath;
    }

    public String getKeyfilePass() {
        return keyfilePass;
    }

    public void setKeyfilePass(String keyfilePass) {
        this.keyfilePass = keyfilePass;
    }

    @Override
    public String toString() {
        return "RaspberryDeviceBean [id=" + id + ", createdAt=" + createdAt
                + ", modifiedAt=" + modifiedAt + ", name=" + name + ", host="
                + host + ", user=" + user + ", port=" + port + ", description="
                + description + ", serial=" + serial + ", sudoPass=" + sudoPass
                + ", authMethod=" + authMethod + ", pass=" + pass
                + ", keyfilePath=" + keyfilePath + ", keyfilePass="
                + keyfilePass + ", spinnerPosition=" + spinnerPosition
                + ", lastQueryData=" + lastQueryData + "]";
    }

    /**
     * @param authMethod AUTH_PASSWORD, AUTH_PUBLIC_KEY or AUTH_PUBLIC_KEY_WITH_PASSWORD
     * @return if this devices uses this method
     */
    public boolean usesAuthentificationMethod(String authMethod) {
        return getAuthMethod().equals(authMethod);
    }

}
