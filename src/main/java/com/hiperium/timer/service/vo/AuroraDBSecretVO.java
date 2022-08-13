package com.hiperium.timer.service.vo;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author Andres Solorzano
 */
@RegisterForReflection
public class AuroraDBSecretVO {

    private String host;
    private String port;
    private String dbname;
    private String username;
    private String password;
    private String engine;
    private String dbClusterIdentifier;

    public AuroraDBSecretVO() {
        // Nothing to implement
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
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

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    @Override
    public String toString() {
        return "AuroraDBSecretVO{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", dbname='" + dbname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", engine='" + engine + '\'' +
                ", dbClusterIdentifier='" + dbClusterIdentifier + '\'' +
                '}';
    }
}
