package com.meng.shell;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.util.JdbcUtils;
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

	//set-db-info --ip-and-port 10.10.1.69:3306 --user-and-pass root:V9ftr3SNqwGoQt0eli --db wyt_csf
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



	@ShellMethod(value = "3.dealTable",group = "Db Commands")
	public Object dealTable(@ShellOption(help = "数据格式:表名,字段名... ：signature_parameter,signature_path") String table) throws Exception{

		DruidDataSource ds = getDs(url,userAndPass);
		DruidPooledConnection conn = ds.getConnection();

		List<String> strList = HelpMe.easySplit(table);

		String tableName = strList.get(0);

		String sql = "select id,";

		for (int i=0;i<strList.size();i++){
			if (i==0){
			}else {
				sql += strList.get(i);
				sql += ",";
			}
		}

		sql = StrUtil.removeSuffix(sql,",");
		sql += " from " + strList.get(0);

		System.out.println("执行的sql: "+sql);

		List<Map<String, Object>> mapList = null;
		try {
			mapList = JdbcUtils.executeQuery(conn, sql, Lists.newArrayList());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		int size = strList.size();

		for (Map<String, Object> item:mapList){
			for (int i=1;i<strList.size();i++){
				String ossUrl = MapUtil.getStr(item,strList.get(i));
				if (StrUtil.isNotEmpty(ossUrl)&&(ossUrl.startsWith("http://") || ossUrl.startsWith("https://"))){
					dealColumn(tableName,strList.get(i),MapUtil.getLong(item,"id"),ossUrl,conn);
				}
			}
		}

//		System.out.println(JSONUtil.toJsonPrettyStr(mapList));
		System.out.println("数据条数："+mapList.size());

		JdbcUtils.close(conn);
		JdbcUtils.close(ds);

		return "complete!";
	}


	private void dealColumn(String tableName,String columnName,Long idVal,String ossUrl,DruidPooledConnection conn){
		System.out.println("原字段 ossUrl 值：" +columnName+" : "+ossUrl);

		String baseDir = "C:\\Users\\18514\\Desktop\\test4";

		String extName = FileUtil.extName(ossUrl);

		String finalName = baseDir + "\\" + IdUtil.fastSimpleUUID()+"."+extName;

		HttpUtil.downloadFile(ossUrl,finalName);

		String url = "http://test.wyt.ticket.iciyun.net/user/outter/fore/upload/common.do";

		File file = new File(finalName);

		Map<String,Object> param = Maps.newHashMap();
		param.put("file",file);

		String body = HttpUtil.post(url, param);

		JSONObject json = JSONUtil.parseObj(body);

		String columnVal = ossUrl;

		String success = json.getStr("success");
		if ("true".equals(success)){
			String obj = json.getStr("obj");
			if (obj.startsWith("http") || obj.startsWith("https")){
				columnVal = obj;
			}
		}

		String sql = "update "+tableName + " set "+columnName + " = '"+columnVal+"' where id = " + idVal;

		try {
			JdbcUtils.execute(conn,sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("执行更新 sql : "+sql);

		FileUtil.del(file);

		ThreadUtil.safeSleep(500);
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
