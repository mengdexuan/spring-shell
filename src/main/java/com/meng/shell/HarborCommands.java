package com.meng.shell;

import com.meng.harbor.HarborClient;
import com.meng.harbor.model.Project;

import java.util.List;

/**
 * @author mengdexuan on 2021/7/3 17:35.
 */
public class HarborCommands {


	public static void main(String[] args) throws Exception{

		String baseUrl = "https://182.92.155.16:8888";
		String username = "admin";
		String password = "Harbor12345";

		HarborClient harborClient = new HarborClient(baseUrl,true);

		int result = harborClient.login(username, password);

		System.out.println(result);

		List<Project> projectList = harborClient.getProjects("library", "true");

		System.out.println(projectList);
	}







}
