package org.argszero.websql.domain;

import javax.persistence.*;

/**
 * Created by shaoaq on 14-1-8.
 */
@Entity(name = "websql_connection_config")
public class ConnectionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "driver")
    private String driver;
    @Column(name = "driver_class")
    private String driverClass;
    @Column(name = "url")
    private String url;
    @Column(name = "usr")
    private String usr;
    @Column(name = "pwd")
    private String pwd;
    @Column(name = "monitor_link")
    private String monitorLink;


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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getMonitorLink() {
        return monitorLink;
    }

    public void setMonitorLink(String monitorLink) {
        this.monitorLink = monitorLink;
    }
}
