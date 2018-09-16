package de.eidottermihi.rpicheck.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "DEVICES")
public class Device {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Long id;
    @ColumnInfo
    private String name;
    @ColumnInfo
    private String description;
    @ColumnInfo(name = "host")
    private String hostname;
    @ColumnInfo(name = "user")
    private String username;
    @ColumnInfo(name = "passwd")
    private String password;
    @ColumnInfo(name = "sudo_passwd")
    private String sudoPassword;
    @ColumnInfo(name = "ssh_port")
    private Integer sshPort;
    @ColumnInfo(name = "auth_method")
    private String authenticationMethod;
    @ColumnInfo(name = "keyfile_path")
    private String keyfilePath;
    @ColumnInfo(name = "keyfile_pass")
    private String keyfilePassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSudoPassword() {
        return sudoPassword;
    }

    public void setSudoPassword(String sudoPassword) {
        this.sudoPassword = sudoPassword;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getKeyfilePath() {
        return keyfilePath;
    }

    public void setKeyfilePath(String keyfilePath) {
        this.keyfilePath = keyfilePath;
    }

    public String getKeyfilePassword() {
        return keyfilePassword;
    }

    public void setKeyfilePassword(String keyfilePassword) {
        this.keyfilePassword = keyfilePassword;
    }
}
