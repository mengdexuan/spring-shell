package com.meng.shell;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.util.MySqlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.meng.util.HelpMe;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 执行数据库脚本命令
 * @author mengdexuan on 2021/7/21 11:00.
 */
@ShellComponent
public class DbCommands {

	private String url;
	private String userAndPass;

	private String connParam = "?characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&tinyInt1isBit=false&autoReconnect=true&failOverReadOnly=false";

	@ShellMethod(value = "1.set db info",group = "Db Commands")
	public Object setDbInfo(@ShellOption(help = "ip和port，eg: localhost:3306")String ipAndPort,
						   @ShellOption(help = "user和pass，eg: root:123456")String userAndPass,
						   @ShellOption(help = "数据库名称，eg: baas_rancher")String db) {

		url = "jdbc:mysql://"+ipAndPort+"/"+db+connParam;
		this.userAndPass = userAndPass;

		DruidDataSource ds = getDs(url,userAndPass);

		checkDs(ds);

		return url;
	}


	@ShellMethod(value = "2.exec db script",group = "Db Commands")
	public Object exe(@ShellOption(help = "脚本文件路径，eg:/home/tq/queryNewCode.sql") String scriptPath) throws Exception{

		DruidDataSource ds = getDs(url,userAndPass);
		DruidPooledConnection conn = ds.getConnection();

		String sql = FileUtil.readUtf8String(scriptPath);

		JdbcUtils.execute(conn,sql);

		JdbcUtils.close(conn);
		JdbcUtils.close(ds);

		return "complete!";
	}






	/**
	 * 获取 表 中数据，并生成 insert 语句形式
	 * @param conn
	 * @param tableName
	 * @return
	 */
	private List<String> sqlInsertData(Connection conn, String tableName) {

		List<Map<String, Object>> mapList = null;
		try {
			mapList = JdbcUtils.executeQuery(conn, "select * from " + tableName, Lists.newArrayList());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<String> sqlData = Lists.newArrayList();

		for (Map<String, Object> map : mapList) {
			Map<String, Object> mapTemp = Maps.newHashMap();

			for (Map.Entry<String, Object> entry:map.entrySet()){
				mapTemp.put("`"+entry.getKey()+"`",entry.getValue());
			}

			String tempSql = JdbcUtils.makeInsertToTableSql(tableName, mapTemp.keySet());
			List<Object> parameters = new ArrayList(mapTemp.values());
			tempSql = tempSql.replaceAll("\\?", "{}");
			List<String> paramList = parameters.stream().map(item -> {
				if (item == null) {
					return null;
				} else {
					return "'" + item.toString() + "'";
				}
			}).collect(Collectors.toList());

			tempSql = StrUtil.format(tempSql, HelpMe.easyList2Arr(paramList));

			sqlData.add(tempSql);
		}

		return sqlData;
	}


	private DruidDataSource getDs(String url,String userAndPass){
		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(url);
		ds.setUsername(userAndPass.split(":")[0]);
		ds.setPassword(userAndPass.split(":")[1]);
		return ds;
	}

	/**
	 * 检测数据库连接是否可用
	 * @param ds
	 */
	private void checkDs(DruidDataSource ds){
		try {
			DruidPooledConnection conn = ds.getConnection(3000);
			conn.close();
			ds.close();
		} catch (SQLException e) {
			System.err.println("获取数据库连接失败！");
		}
		System.out.println("数据库连接成功！");
	}


}
