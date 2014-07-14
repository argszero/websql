package org.argszero.websql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argszero.websql.domain.ConnectionConfig;
import org.argszero.websql.repo.ConnectionConfigRepo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/jdbc")
public class Exec {
    private static Log logger = LogFactory.getLog(Exec.class);

    @Autowired
    private ConnectionConfigRepo connectionConfigRepo;

    private static class ThreadClassLoader extends URLClassLoader implements AutoCloseable {
        private final ClassLoader oldClassLoader;

        public ThreadClassLoader(String dir) throws IOException, URISyntaxException {
            super(getJarURLArray(dir), Thread.currentThread().getContextClassLoader());
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
        }

        @Override
        public void close() throws IOException {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            super.close();
        }

        private static URL[] getJarURLArray(String dirName) throws IOException, URISyntaxException {
            File drivers = Main.getDriverDir();
            File dir = new File(drivers, dirName);
            if (dir.exists() && dir.isDirectory()) {
                List<URL> jars = new ArrayList();
                for (File jar : dir.listFiles()) {
                    if (jar.getName().endsWith(".jar")) {
                        jars.add(jar.toURI().toURL());
                    }
                }
                return jars.toArray(new URL[jars.size()]);
            }
            return new URL[0];
        }
    }


    @RequestMapping("/init")
    @Transactional
    void init() {
        ConnectionConfig config;
//         config = new ConnectionConfig();
//        config.setDriver("hive-jdbc-0.11.0-shark-0.9.1");
//        config.setDriverClass("org.apache.hive.jdbc.HiveDriver");
//        config.setName("dmp@dmp001:hive-jdbc-0.11.0-shark-0.9.1");
//        config.setUrl("jdbc:hive2://10.161.0.15:10002/default");
//        config.setPwd("dmp");
//        config.setUsr("dmp");
//        config.setMonitorLink("http://10.161.0.15:4040/");
//        connectionConfigRepo.save(config);

        config = new ConnectionConfig();
        config.setDriver("hive-jdbc-0.12.0-cdh5.0.0");
        config.setName("dmp@dmp001:hive-jdbc-0.12.0-cdh5.0.0");
        config.setDriverClass("org.apache.hive.jdbc.HiveDriver");
        config.setUrl("jdbc:hive2://10.161.0.15:10000/default");
        config.setPwd("dmp");
        config.setUsr("dmp");
        config.setMonitorLink("http://10.161.0.15:8338/");
        connectionConfigRepo.save(config);

        config = new ConnectionConfig();
        config.setDriver("apache-spark-branch-1.0-jdbc");
        config.setName("dmp@dmp001:apache-spark-branch-1.0-jdbc");
        config.setDriverClass("org.apache.hive.jdbc.HiveDriver");
        config.setUrl("jdbc:hive2://dmp001:10003/default");
        config.setPwd("dmp");
        config.setUsr("dmp");
        config.setMonitorLink("http://dmp001:4040/");
        connectionConfigRepo.save(config);
    }

    @RequestMapping("/connection_config")
    @ResponseBody
    @Transactional
    List<ConnectionConfig> findAll() {
        List<ConnectionConfig> configs = new ArrayList<>();
        for (ConnectionConfig conf : connectionConfigRepo.findAll()) {
            configs.add(conf);
        }
        return configs;
    }


    @RequestMapping("/exec")
    @ResponseBody
    @Transactional
    String exec(@RequestParam String sql, @RequestParam Long connectionConfigId) throws SQLException, JSONException, IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        long limit = 100;
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        String[] sqls = sql.split(";");
        for (int i = 0; i < sqls.length; i++) {
            if (Pattern.matches("select \\* from \\S*", sqls[i])) {
                sqls[i] = sqls[i] + " limit " + limit;
            }
        }
        logger.info("exec sql:" + sql);
        JSONObject result = new JSONObject();
        ConnectionConfig config = connectionConfigRepo.findOne(connectionConfigId);
        try (ThreadClassLoader classLoader = new ThreadClassLoader(config.getDriver())) {
            Class driverClass = Class.forName(config.getDriverClass(), true, classLoader);
            logger.info("use driverClass from :" + driverClass.getResource("HiveDriver.class"));
            DelegatingDriver driver = new DelegatingDriver((Driver) driverClass.newInstance());
            try {
                DriverManager.registerDriver(driver); // register using the Delegating Driver
                exec(sqls, result, config, limit);
            } finally {
                DriverManager.deregisterDriver(driver);
            }
        }
        return result.toString();
    }

    private void exec(String[] sqls, JSONObject result, ConnectionConfig config, long limit) throws SQLException, JSONException {
        try (Connection con = DriverManager.getConnection(config.getUrl(), config.getUsr(), config.getPwd());
             Statement stmt = con.createStatement()) {
            for (String sql : sqls) {
                logger.info("exec sql:" + sql);
                boolean success = stmt.execute(sql);
                if (success) {
                    int updateCount = stmt.getUpdateCount();
                    try (ResultSet res = stmt.getResultSet()) {
                        if (res != null) {
                            JSONObject meta = new JSONObject();
                            result.put("meta", meta);
                            JSONArray columnNames = new JSONArray();
                            meta.put("columnNames", columnNames);
                            int columnCount = res.getMetaData().getColumnCount() + 1;
                            meta.put("columnCount", columnCount - 1);
                            for (int i = 1; i < columnCount; i++) {
                                columnNames.put(res.getMetaData().getColumnName(i));
                            }
                            JSONArray data = new JSONArray();
                            result.put("data", data);
                            int count = 0;
                            while (res.next() && count++ < limit) {
                                JSONArray row = new JSONArray();
                                for (int i = 1; i < columnCount; i++) {
                                    row.put(res.getObject(i));
                                }
                                data.put(row);
                            }
                            if (res.next()) {
                                result.put("message", "more data than " + limit + " was discard!");
                            } else {
                                result.put("message", "all data showed below:");
                            }
                        } else {
                            result.put("updateCount", updateCount);
                        }
                    }
                }
                result.put("success", success);
            }
        }
    }

    private static class DelegatingDriver implements Driver {
        private final Driver driver;

        public DelegatingDriver(Driver driver) {
            if (driver == null) {
                throw new IllegalArgumentException("Driver must not be null.");
            }
            this.driver = driver;
        }

        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }

        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }
    }


}