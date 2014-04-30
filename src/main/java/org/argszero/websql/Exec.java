package org.argszero.websql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.regex.Pattern;

@RestController
@EnableAutoConfiguration
public class Exec {
    private static Log logger = LogFactory.getLog(Exec.class);

    @RequestMapping("/exec")
    @ResponseBody
    String exec(@RequestParam String sql) throws SQLException, JSONException {
        logger.info("exec sql:" + sql);
        System.out.println("sql:" + sql);
        JSONObject result = new JSONObject();
        Connection con = null;
        Statement stmt = null;
        ResultSet res = null;
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            con = DriverManager.getConnection(
                    "jdbc:hive2://10.161.0.15:10002/default", "dmp", "dmp");
            stmt = con.createStatement();
            sql = sql.trim();
            sql = sql.replaceAll(";", "");
            if (Pattern.matches("select \\* from \\S*", sql)) {
                sql = sql +" limit 200";
            }
            boolean success = stmt.execute(sql);
            if (success) {
                res = stmt.getResultSet();
                int updateCount = stmt.getUpdateCount();
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
                    while (res.next() && count++ < 200) {
                        JSONArray row = new JSONArray();
                        for (int i = 1; i < columnCount; i++) {
                            row.put(res.getObject(i));
                        }
                        data.put(row);
                    }
                    if (res.next()) {
                        result.put("message", "more data than 200 was discard!");
                    } else {
                        result.put("message", "all data showed below:");
                    }
                } else {
                    result.put("updateCount", updateCount);
                }
            }
            result.put("success", success);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Exec.class, args);
    }

}