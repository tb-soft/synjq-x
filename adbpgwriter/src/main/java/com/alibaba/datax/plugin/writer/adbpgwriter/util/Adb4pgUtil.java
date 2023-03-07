package net.tbsoft.datax.plugin.writer.adbpgwriter.util;

import net.tbsoft.cloud.analyticdb.adb4pgclient.Adb4pgClient;
import net.tbsoft.cloud.analyticdb.adb4pgclient.Adb4pgClientException;
import net.tbsoft.cloud.analyticdb.adb4pgclient.DatabaseConfig;
import net.tbsoft.datax.common.element.Column;
import net.tbsoft.datax.common.exception.DataXException;
import net.tbsoft.datax.common.spi.ErrorCode;
import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.plugin.rdbms.util.DBUtil;
import net.tbsoft.datax.plugin.rdbms.util.DBUtilErrorCode;
import net.tbsoft.datax.plugin.rdbms.util.DataBaseType;
import net.tbsoft.datax.plugin.rdbms.writer.Constant;
import net.tbsoft.datax.plugin.rdbms.writer.Key;
import net.tbsoft.datax.plugin.rdbms.writer.util.WriterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

import static net.tbsoft.datax.plugin.rdbms.util.DBUtilErrorCode.COLUMN_SPLIT_ERROR;

/**
 * @author yuncheng
 */
public class Adb4pgUtil {

    private static final Logger LOG = LoggerFactory.getLogger(Adb4pgUtil.class);
    private static final DataBaseType DATABASE_TYPE = DataBaseType.PostgreSQL;
    public static void checkConfig(Configuration originalConfig) {
        try {

            DatabaseConfig databaseConfig = convertConfiguration(originalConfig);

            Adb4pgClient testConfigClient = new Adb4pgClient(databaseConfig);
        } catch (Exception e) {
            throw new Adb4pgClientException(Adb4pgClientException.CONFIG_ERROR, "Check config exception: " + e.getMessage(), null);
        }
    }

    public static DatabaseConfig convertConfiguration(Configuration originalConfig) {
        originalConfig.getNecessaryValue(Key.USERNAME, COLUMN_SPLIT_ERROR);
        originalConfig.getNecessaryValue(Key.PASSWORD, COLUMN_SPLIT_ERROR);


        String userName = originalConfig.getString(Key.USERNAME);
        String passWord = originalConfig.getString(Key.PASSWORD);
        String tableName = originalConfig.getString(Key.TABLE);
        String schemaName = originalConfig.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.SCHEMA);
        String host = originalConfig.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.HOST);
        String port = originalConfig.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.PORT);
        String databseName = originalConfig.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.DATABASE);

        List<String> columns = originalConfig.getList(Key.COLUMN, String.class);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setHost(host);
        databaseConfig.setPort(Integer.valueOf(port));
        databaseConfig.setDatabase(databseName);

        databaseConfig.setUser(userName);

        databaseConfig.setPassword(passWord);
        databaseConfig.setLogger(LOG);

        databaseConfig.setInsertIgnore(originalConfig.getBool(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.IS_INSERTINGORE, true));
        databaseConfig.addTable(Collections.singletonList(tableName), schemaName);
        databaseConfig.setColumns(columns, tableName, schemaName);

        return databaseConfig;
    }

    private static Map<String, List<String>> splitBySchemaName(List<String> tables) {
        HashMap<String, List<String>> res = new HashMap<String, List<String>>(16);

        for (String schemaNameTableName: tables) {
            String[] s = schemaNameTableName.split("\\.");
            if (!res.containsKey(s[0])) {
                res.put(s[0], new ArrayList<String>());
            }
            res.get(s[0]).add(s[1]);

        }

        return res;
    }

    public static Connection getAdbpgConnect(Configuration conf) {
        String userName = conf.getString(Key.USERNAME);
        String passWord = conf.getString(Key.PASSWORD);

        return DBUtil.getConnection(DataBaseType.PostgreSQL, generateJdbcUrl(conf), userName, passWord);

    }

    private static String generateJdbcUrl(Configuration configuration) {
        String host = configuration.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.HOST);
        String port = configuration.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.PORT);
        String databseName = configuration.getString(net.tbsoft.datax.plugin.writer.adbpgwriter.util.Key.DATABASE);
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + databseName;
        return jdbcUrl;

    }
    public static void prepare(Configuration originalConfig) {
        List<String> preSqls = originalConfig.getList(Key.PRE_SQL,
                String.class);

        String tableName = originalConfig.getString(Key.TABLE);
        List<String> renderedPreSqls = WriterUtil.renderPreOrPostSqls(
                preSqls, tableName);

        if (renderedPreSqls.size() == 0) {
            return;
        }

        originalConfig.remove(Key.PRE_SQL);

        Connection conn = getAdbpgConnect(originalConfig);
        WriterUtil.executeSqls(conn, renderedPreSqls, generateJdbcUrl(originalConfig), DATABASE_TYPE);
        DBUtil.closeDBResources(null, null, conn);


    }

    public static void post(Configuration configuration) {
        List<String> postSqls = configuration.getList(Key.POST_SQL,
                String.class);
        String tableName = configuration.getString(Key.TABLE);
        List<String> renderedPostSqls = WriterUtil.renderPreOrPostSqls(
                postSqls, tableName);

        if (renderedPostSqls.size() == 0) {
            return;
        }

        configuration.remove(Key.POST_SQL);

        Connection conn =  getAdbpgConnect(configuration);

        WriterUtil.executeSqls(conn, renderedPostSqls, generateJdbcUrl(configuration), DATABASE_TYPE);
        DBUtil.closeDBResources(null, null, conn);
    }


}
