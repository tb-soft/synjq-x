# RDBMSWriter 插件文档

---

## 1 快速介绍

RDBMSWriter 插件实现了写入数据到 RDBMS 主库的目的表的功能。在底层实现上， RDBMSWriter 通过 JDBC 连接远程 RDBMS 数据库，并执行相应的 insert into ... 的 sql 语句将数据写入 RDBMS。 RDBMSWriter 是一个通用的关系数据库写插件，您可以通过注册数据库驱动等方式增加任意多样的关系数据库写支持。

RDBMSWriter 面向 ETL 开发工程师，他们使用 RDBMSWriter 从数仓导入数据到 RDBMS。同时 RDBMSWriter 亦可以作为数据迁移工具为 DBA 等用户提供服务。

## 2 实现原理

RDBMSWriter 通过 DataX 框架获取 Reader 生成的协议数据，RDBMSWriter 通过 JDBC 连接远程 RDBMS 数据库，并执行相应的 insert into ... 的 sql 语句将数据写入 RDBMS。

## 3 功能说明

### 3.1 配置样例

- 配置一个写入 RDBMS 的作业。

```
{
    "job": {
        "setting": {
            "speed": {
                "channel": 1
            }
        },
        "content": [
            {
                "reader": {
                    "name": "streamreader",
                    "parameter": {
                        "column": [
                            {
                                "value": "DataX",
                                "type": "string"
                            },
                            {
                                "value": 19880808,
                                "type": "long"
                            },
                            {
                                "value": "1988-08-08 08:08:08",
                                "type": "date"
                            },
                            {
                                "value": true,
                                "type": "bool"
                            },
                            {
                                "value": "test",
                                "type": "bytes"
                            }
                        ],
                        "sliceRecordCount": 1000
                    }
                },
                "writer": {
                    "name": "rdbmswriter",
                    "parameter": {
                        "connection": [
                            {
                                "jdbcUrl": "jdbc:dm://ip:port/database",
                                "table": [
                                    "table"
                                ]
                            }
                        ],
                        "username": "username",
                        "password": "password",
                        "table": "table",
                        "column": [
                            "*"
                        ],
                        "preSql": [
                            "delete from XXX;"
                        ]
                    }
                }
            }
        ]
    }
}
```

### 3.2 参数说明

- **jdbcUrl**

  - 描述：描述的是到对端数据库的 JDBC 连接信息，jdbcUrl 按照 RDBMS 官方规范，并可以填写连接附件控制信息。请注意不同的数据库 jdbc 的格式是不同的，DataX 会根据具体 jdbc 的格式选择合适的数据库驱动完成数据读取。

  * 达梦 jdbc:dm://ip:port/database
  * db2 格式 jdbc:db2://ip:port/database
  * PPAS 格式 jdbc:edb://ip:port/database

  **rdbmswriter 如何增加新的数据库支持:**

  - 进入 rdbmswriter 对应目录，这里${DATAX_HOME}为 DataX 主目录，即: ${DATAX_HOME}/plugin/writer/rdbmswriter
  - 在 rdbmswriter 插件目录下有 plugin.json 配置文件，在此文件中注册您具体的数据库驱动，具体放在 drivers 数组中。rdbmswriter 插件在任务执行时会动态选择合适的数据库驱动连接数据库。

  ```json
  {
    "name": "rdbmswriter",
    "class": "net.tbsoft.datax.plugin.reader.rdbmswriter.RdbmsWriter",
    "description": "useScene: prod. mechanism: Jdbc connection using the database, execute select sql, retrieve data from the ResultSet. warn: The more you know about the database, the less problems you encounter.",
    "developer": "alibaba",
    "drivers": [
      "dm.jdbc.driver.DmDriver",
      "com.ibm.db2.jcc.DB2Driver",
      "com.sybase.jdbc3.jdbc.SybDriver",
      "com.edb.Driver"
    ]
  }
  ```

  - 在 rdbmswriter 插件目录下有 libs 子目录，您需要将您具体的数据库驱动放到 libs 目录下。

  ```
  $tree
  .
  |-- libs
  |   |-- Dm7JdbcDriver16.jar
  |   |-- commons-collections-3.0.jar
  |   |-- commons-io-2.4.jar
  |   |-- commons-lang3-3.3.2.jar
  |   |-- commons-math3-3.1.1.jar
  |   |-- datax-common-0.0.1-SNAPSHOT.jar
  |   |-- datax-service-face-1.0.23-20160120.024328-1.jar
  |   |-- db2jcc4.jar
  |   |-- druid-1.0.15.jar
  |   |-- edb-jdbc16.jar
  |   |-- fastjson-1.1.46.sec01.jar
  |   |-- guava-r05.jar
  |   |-- hamcrest-core-1.3.jar
  |   |-- jconn3-1.0.0-SNAPSHOT.jar
  |   |-- logback-classic-1.0.13.jar
  |   |-- logback-core-1.0.13.jar
  |   |-- plugin-rdbms-util-0.0.1-SNAPSHOT.jar
  |   `-- slf4j-api-1.7.10.jar
  |-- plugin.json
  |-- plugin_job_template.json
  `-- rdbmswriter-0.0.1-SNAPSHOT.jar
  ```

  - 必选：是 <br />

  - 默认值：无 <br />

- **username**

  - 描述：数据源的用户名 <br />
  - 必选：是 <br />
  - 默认值：无 <br />

- **password**

  - 描述：数据源指定用户名的密码 <br />
  - 必选：是 <br />
  - 默认值：无 <br />

- **table**

  - 描述：目标表名称，如果表的 schema 信息和上述配置 username 不一致，请使用 schema.table 的格式填写 table 信息。 <br />
  - 必选：是 <br />
  - 默认值：无 <br />

- **column**

  - 描述：所配置的表中需要同步的列名集合。以英文逗号（,）进行分隔。`我们强烈不推荐用户使用默认列情况` <br />

  - 必选：是 <br />
  - 默认值：无 <br />

- **preSql**

  - 描述：执行数据同步任务之前率先执行的 sql 语句，目前只允许执行一条 SQL 语句，例如清除旧数据。<br />
  - 必选：否 <br />
  - 默认值：无 <br />

- **postSql**

  - 描述：执行数据同步任务之后执行的 sql 语句，目前只允许执行一条 SQL 语句，例如加上某一个时间戳。 <br />
  - 必选：否 <br />
  - 默认值：无 <br />

- **batchSize**

  - 描述：一次性批量提交的记录数大小，该值可以极大减少 DataX 与 RDBMS 的网络交互次数，并提升整体吞吐量。但是该值设置过大可能会造成 DataX 运行进程 OOM 情况。<br />

  - 必选：否 <br />

  - 默认值：1024 <br />

### 3.3 类型转换

目前 RDBMSReader 支持大部分通用得关系数据库类型如数字、字符等，但也存在部分个别类型没有支持的情况，请注意检查你的类型，根据具体的数据库做选择。
