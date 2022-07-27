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

	//临时文件存储目录
	String baseDir = "C:\\Users\\18514\\Desktop\\test4";

	/**
	 * 设置数据库连接信息，参数示例：
	 * 	set-db-info --ip-and-port 10.10.1.69:3306 --user-and-pass root:V9ftr3SNqwGoQt0eli --db wyt_csf
	 * @param ipAndPort
	 * @param userAndPass
	 * @param db
	 * @return
	 */
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
	 * 处理表中字段，参数示例：
	 *
	 * deal-table tang_tenant_config,img_login_logo,img_login_background,browser_icon,voucher_proof_img,voucher_logo,index_background,login_background
	 * deal-table sys_account_record,offline_payment_url
	 * deal-table signature_parameter,signature_path
	 * deal-table per_front_user,idcard_front_url,idcard_reverse_url
	 * deal-table attachment,file_url
	 * deal-table axq_contract,file_id
	 * deal-table axq_seal,localhost_path
	 * deal-table pay_detail,pay_param
	 * deal-table enterprise_expand,business_license_url,idcard_front_side_url,idcard_reverse_side_url,author_certificate
	 *
	 * @param table
	 * @return
	 * @throws Exception
	 */
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


	/**
	 * 处理 json 中的 url ，参数示例：
	 *
	 * deal-table-json --table enterprise_expand --column person --json-key credentFrontUrl
	 * deal-table-json --table enterprise_expand --column person --json-key credentReverseUrl
	 *
	 * @param table
	 * @param column
	 * @param jsonKey
	 * @return
	 * @throws Exception
	 */
	@ShellMethod(value = "4.dealTableJson",group = "Db Commands")
	public Object dealTableJson(@ShellOption(help = "表名：enterprise_expand") String table,
								@ShellOption(help = "字段名：enterprise_info") String column,
								@ShellOption(help = "json key：businessLicenseUrl") String jsonKey) throws Exception{

		DruidDataSource ds = getDs(url,userAndPass);
		DruidPooledConnection conn = ds.getConnection();

		String sql = "select id,"+column+" from "+table+" where "+column +" is not null";

		System.out.println("执行的sql: "+sql);

		List<Map<String, Object>> mapList = null;
		try {
			mapList = JdbcUtils.executeQuery(conn, sql, Lists.newArrayList());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (Map<String, Object> item:mapList){
			dealColumnJson(item,table,column,jsonKey,conn);
		}

		System.out.println("数据条数："+mapList.size());

		JdbcUtils.close(conn);
		JdbcUtils.close(ds);

		return "complete!";
	}





	private void dealColumn(String tableName,String columnName,Long idVal,String ossUrl,DruidPooledConnection conn){
		System.out.println("原字段 ossUrl 值：" +columnName+" : "+ossUrl);


		String extName = FileUtil.extName(ossUrl);

		String finalName = baseDir + "\\" + IdUtil.fastSimpleUUID()+"."+extName;

		HttpUtil.downloadFile(ossUrl,finalName);

//		万易通测试环境地址
//		String url = "http://test.wyt.ticket.iciyun.net/user/outter/fore/upload/common.do";

//		万易通正式环境地址
		String url = "https://wanyitong.51tangpiao.com/user/outter/fore/upload/common.do";

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

		ThreadUtil.safeSleep(300);
	}


	private void dealColumnJson(Map<String,Object> map,String table,String column,String jsonKey,DruidPooledConnection conn){
		Long id = MapUtil.getLong(map,"id");
		String columnVal = MapUtil.getStr(map,column);

		System.out.println("原字段值：" +columnVal);

		JSONObject json2 = JSONUtil.parseObj(columnVal);
		String jsonVal = json2.getStr(jsonKey);

		if (StrUtil.isNotEmpty(jsonVal)&&(jsonVal.startsWith("http://") || jsonVal.startsWith("https://"))){
		}else {
			return;
		}

		String extName = FileUtil.extName(jsonVal);

		String finalName = baseDir + "\\" + IdUtil.fastSimpleUUID()+"."+extName;

		HttpUtil.downloadFile(jsonVal,finalName);

//		万易通测试环境地址
//		String url = "http://test.wyt.ticket.iciyun.net/user/outter/fore/upload/common.do";

//		万易通正式环境地址
		String url = "https://wanyitong.51tangpiao.com/user/outter/fore/upload/common.do";

		File file = new File(finalName);

		Map<String,Object> param = Maps.newHashMap();
		param.put("file",file);

		String body = HttpUtil.post(url, param);

		JSONObject json = JSONUtil.parseObj(body);

		String newUrl = jsonVal;

		String success = json.getStr("success");
		if ("true".equals(success)){
			String obj = json.getStr("obj");
			if (obj.startsWith("http") || obj.startsWith("https")){
				newUrl = obj;
			}
		}

		json2.putOpt(jsonKey,newUrl);

		String newColumnVal = JSONUtil.toJsonStr(json2);

		String sql = "update "+table + " set "+column + " = '"+newColumnVal+"' where id = " + id;

		try {
			JdbcUtils.execute(conn,sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("执行更新 sql : "+sql);

		FileUtil.del(file);

		ThreadUtil.safeSleep(300);
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
