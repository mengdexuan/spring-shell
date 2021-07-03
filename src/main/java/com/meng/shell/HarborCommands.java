package com.meng.shell;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 生成迁移 harbor 镜像的脚本命令文件，实际迁移通过执行生成的脚本文件进行
 * @author mengdexuan on 2021/7/3 17:35.
 */
@ShellComponent
public class HarborCommands {

	//校验地址中是否存在 “ip:端口号”  （例如rtsp://admin:admin@192.168.30.98:554/media/video1 ）
	private Pattern ipAndPortPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");
	private String firstRowCmd = "#!/bin/bash";

	private String baseUrl = "";
	private String username = "";
	private String ipAndPort = "";
	private String pwd = "";

	private static final String harborCmdGroup = "Harbor Commands";

	@ShellMethod(value = "set harbor base url", group = harborCmdGroup)
	public String setUrl(String ipAndPort, String username, String pwd,
						 @ShellOption(defaultValue = "false", help = "是否为 https 连接") String isHttps) {

		String temp = "http://";
		if (isHttps.equals("true")) {
			temp = "https://";
		}
		temp += ipAndPort;
		temp += "/api/v2.0";

		baseUrl = temp;
		this.ipAndPort = ipAndPort;
		this.username = username;
		this.pwd = pwd;

		return showUrl();
	}

	@ShellMethod(value = "print the basic info to connect harbor", group = harborCmdGroup)
	public String showUrl() {
		Tuple tuple = new Tuple(baseUrl, username, pwd);
		System.out.println("connection info:");
		return tuple.toString();
	}


	@ShellMethod(value = "query all projects in the harbor", group = harborCmdGroup)
	public Object projects() {

		HttpRequest request = HttpUtil.createGet(baseUrl + "/projects?page=1&page_size=100");
		request.basicAuth(username, pwd);

		HttpResponse response = request.execute();

		Object result = "";

		if (response.isOk()) {
			String json = JSONUtil.toJsonPrettyStr(response.body());

			JSONArray arr = JSONUtil.parseArray(response.body());

			List<Map<String, Object>> tempList = new ArrayList<>();
			for (Object obj : arr) {
				JSONObject item = (JSONObject) obj;

				int repoCount = item.getInt("repo_count");
				String projectId = item.getStr("project_id");
				String name = item.getStr("name");

				Map<String, Object> map = new HashMap<>();
				map.put("project_id", projectId);
				map.put("name", name);
				map.put("repoCount", repoCount);
				tempList.add(map);
			}

			result = tempList;

		} else {
			result = response.toString();
		}

		return result;
	}


	@ShellMethod(value = "在当前目录下生成：指定 project 下所有镜像的拉取命令文件", group = harborCmdGroup)
	public Object genPullSh(String name) {

		HttpRequest request = HttpUtil.createGet(baseUrl + "/projects/"+name+"/repositories?page=1&page_size=100");
		request.basicAuth(username, pwd);

		HttpResponse response = request.execute();

		if (response.isOk()){

			File file = new File(name+"-pull.sh");
			FileUtil.appendUtf8Lines(Lists.newArrayList(firstRowCmd),file);

			JSONArray arr = JSONUtil.parseArray(response.body());

			List<Map<String, Object>> tempList = new ArrayList<>();
			for (Object obj : arr) {
				JSONObject item = (JSONObject) obj;
				String repoName = item.getStr("name");

				String pullCmd = "docker pull "+ipAndPort+"/"+repoName+":";

				String artifact = StrUtil.removePrefix(repoName, name+"/");

//				If it contains slash, encode it with URL encoding. e.g. a/b -> a%252Fb
				artifact = StrUtil.replace(artifact,"/","%252F");


				request = HttpUtil.createGet(baseUrl + "/projects/"+name+"/repositories/"+artifact+"/artifacts?page=1&page_size=10&with_tag=true&with_label=false&with_scan_overview=false&with_signature=false&with_immutable_status=false");
				request.basicAuth(username, pwd);

				response = request.execute();

				if (response.isOk()){
					//提取 tag 信息
					JSONArray arr2 = JSONUtil.parseArray(response.body());
					for (Object obj2 : arr2) {
						JSONObject item2 = (JSONObject) obj2;

						JSONArray tags = item2.getJSONArray("tags");
						if (CollUtil.isEmpty(tags)){
							System.out.println(repoName + " tags is null");
						}else {
							JSONObject tag = (JSONObject)tags.get(0);
							String tagName = tag.getStr("name");
							pullCmd += tagName;
							System.out.println(pullCmd);

							FileUtil.appendUtf8Lines(Lists.newArrayList(pullCmd),file);
						}
					}
				}else {
					return response.body();
				}
			}

			return "项目名称为 "+name +" 下的所有镜像，拉取命令已生成："+file.getName();
		}else {
			return response.body();
		}

	}


	@ShellMethod(value = "根据 gen-pull-sh 命令生成的文件，重新打 tag ，指定到不同的 harbor 仓库", group = harborCmdGroup)
	public Object genTagSh(String name,String ipAndPort) {

		File file = new File(name+"-pull.sh");
		File tagFile = new File(name+"-tag.sh");

		List<String> list = FileUtil.readUtf8Lines(file);

		for (int i=0;i<list.size();i++){
			String item = list.get(i);
			if (i==0){
				FileUtil.appendUtf8Lines(Lists.newArrayList(firstRowCmd),tagFile);
			}else {
//				docker pull 182.92.155.16:8888/database/es:7.6.2
//				docker tag 182.92.155.16:8888/database/redis:6.2.4 192.168.1.2:8888/database/redis:6.2.4

				String content = item.split(" ")[2];

				String tagCmd = "docker tag " + content +" ";

				String temp = ReUtil.replaceFirst(ipAndPortPattern, content, ipAndPort);

				tagCmd += temp;

				System.out.println(tagCmd);

				FileUtil.appendUtf8Lines(Lists.newArrayList(tagCmd),tagFile);
			}
		}

		return "生成文件："+tagFile.getName();
	}



	@ShellMethod(value = "根据 gen-tag-sh 命令生成的文件，修改为 push 命令 ，以 push 到不同的 harbor 仓库", group = harborCmdGroup)
	public Object genPushSh(String name) {
		File tagFile = new File(name+"-tag.sh");
		File pushFile = new File(name+"-push.sh");

		List<String> list = FileUtil.readUtf8Lines(tagFile);

		for (int i=0;i<list.size();i++) {
			String item = list.get(i);
			if (i == 0) {
				FileUtil.appendUtf8Lines(Lists.newArrayList(firstRowCmd), pushFile);
			} else {
				String tempArr[] = item.split(" ");
				String last = tempArr[tempArr.length-1];
				String pushCmd = "docker push "+last;

				System.out.println(pushCmd);

				FileUtil.appendUtf8Lines(Lists.newArrayList(pushCmd), pushFile);
			}
		}

		return "生成文件："+pushFile.getName();
	}

	@ShellMethod(value = "生成删除本地镜像脚本", group = harborCmdGroup)
	public Object genRmiSh(String name) {

		File pullFile = new File(name+"-pull.sh");
		File pushFile = new File(name+"-push.sh");
		File pullRmiFile = new File(name+"-pull-rmi.sh");
		File pushRmiFile = new File(name+"-push-rmi.sh");

		List<String> list = FileUtil.readUtf8Lines(pullFile);

		for (int i=0;i<list.size();i++) {
			String item = list.get(i);
			if (i == 0) {
				FileUtil.appendUtf8Lines(Lists.newArrayList(firstRowCmd), pullRmiFile);
			} else {
				String pullRmiCmd = StrUtil.replace(item, "pull", "rmi");
				System.out.println(pullRmiCmd);
				FileUtil.appendUtf8Lines(Lists.newArrayList(pullRmiCmd), pullRmiFile);
			}
		}


		System.out.println("生成文件："+pullRmiFile.getName());

		list = FileUtil.readUtf8Lines(pushFile);

		for (int i=0;i<list.size();i++) {
			String item = list.get(i);
			if (i == 0) {
				FileUtil.appendUtf8Lines(Lists.newArrayList(firstRowCmd), pushRmiFile);
			} else {
				String pushRmiCmd = StrUtil.replace(item, "push", "rmi");
				System.out.println(pushRmiCmd);
				FileUtil.appendUtf8Lines(Lists.newArrayList(pushRmiCmd), pushRmiFile);
			}
		}

		System.out.println("生成文件："+pushRmiFile.getName());

		return "ok";
	}








}
