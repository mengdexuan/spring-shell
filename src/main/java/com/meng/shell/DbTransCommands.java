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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * mysql 数据传输
 *
 * 从 A 数据库，将表结构及数据复制到 B 数据库中
 * 需要分别设置 A，B两个数据库的连接信息
 *
 * @author mengdexuan on 2021/7/21 11:00.
 */
@ShellComponent
public class DbTransCommands {

	private String fromUrl;
	private String toUrl;
	private String fromUserAndPass;
	private String toUserAndPass;

	String connParam = "?characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&tinyInt1isBit=false&autoReconnect=true&failOverReadOnly=false";

	@ShellMethod(value = "1.set from db info",group = "Db Trans Commands")
	public Object fromInfo(@ShellOption(help = "ip和port，eg: localhost:3306")String ipAndPort,
						   @ShellOption(help = "user和pass，eg: root:123456")String userAndPass,
						   @ShellOption(help = "数据库名称，eg: baas_rancher")String fromDb) {

		fromUrl = "jdbc:mysql://"+ipAndPort+"/"+fromDb+connParam;
		this.fromUserAndPass = userAndPass;

		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(fromUrl);
		ds.setUsername(userAndPass.split(":")[0]);
		ds.setPassword(userAndPass.split(":")[1]);

		try {
			DruidPooledConnection conn = ds.getConnection(3000);
			conn.close();
			ds.close();
		} catch (SQLException e) {
			System.out.println("获取数据库连接失败！");
		}
		System.out.println("数据库连接成功！");

		return fromUrl;
	}


	@ShellMethod(value = "2.set to db info",group = "Db Trans Commands")
	public Object toInfo(@ShellOption(help = "ip和port，eg: 192.168.2.17:3306")String ipAndPort,
						   @ShellOption(help = "user和pass，eg: root:123456")String userAndPass,
						   @ShellOption(help = "数据库名称，eg: baas_rancher")String toDb) {

		toUrl = "jdbc:mysql://"+ipAndPort+"/"+toDb+connParam;
		this.toUserAndPass = userAndPass;

		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(toUrl);
		ds.setUsername(userAndPass.split(":")[0]);
		ds.setPassword(userAndPass.split(":")[1]);

		try {
			DruidPooledConnection conn = ds.getConnection(3000);
			conn.close();
			ds.close();
		} catch (SQLException e) {
			System.out.println("获取数据库连接失败！");
		}
		System.out.println("数据库连接成功！");

		return toUrl;
	}



	@ShellMethod(value = "3.trans db from A to B",group = "Db Trans Commands")
	public Object trans(@ShellOption(defaultValue = "true", help = "是否复制表中数据") String flag) throws Exception{

		if (StrUtil.isEmpty(fromUrl)){
			return "from db info is null,stop!";
		}
		if (StrUtil.isEmpty(toUrl)){
			return "to db info is null,stop!";
		}
		String transInfo = fromUrl+"    -->   "+toUrl;
		System.out.println(transInfo);


		DruidDataSource fromDs = new DruidDataSource();
		fromDs.setUrl(fromUrl);
		fromDs.setUsername(fromUserAndPass.split(":")[0]);
		fromDs.setPassword(fromUserAndPass.split(":")[1]);

		DruidDataSource toDs = new DruidDataSource();
		toDs.setUrl(toUrl);
		toDs.setUsername(toUserAndPass.split(":")[0]);
		toDs.setPassword(toUserAndPass.split(":")[1]);

		DruidPooledConnection fromConn = fromDs.getConnection();
		DruidPooledConnection toConn = toDs.getConnection();

		List<String> tables = MySqlUtils.showTables(fromConn);

		for (String table:tables){
			try {
				System.out.println();
				System.out.println("--- begin trans "+table);
				String ddl = MySqlUtils.getTableDDL(fromConn, Lists.newArrayList(table)).get(0);

				System.out.println(ddl);
				JdbcUtils.execute(toConn,ddl);

				if ("true".equals(flag)){
					List<String> insertList = sqlInsertData(fromConn, table);
					for (String sql:insertList){
						try {
							JdbcUtils.execute(toConn,sql);
							System.out.println(sql);
						}catch (Exception e){
							System.out.println("error while exec insert sql:"+sql);
							System.out.println(e.getMessage());
						}
					}
				}

				System.out.println("--- end trans "+table);
				System.out.println();
				ThreadUtil.safeSleep(1000);
			}catch (Exception e){
				String msg = "error while trans "+table+"!";
				System.out.println(msg);
				System.out.println(e.getMessage());
			}
		}


		fromConn.close();
		toConn.close();
		fromDs.close();
		toDs.close();

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



}
