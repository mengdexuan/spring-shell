package com.meng.shell;

import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 *
 * Jasypt 加密 解密
 *
 * @author mengdexuan on 2021/9/6 15:52.
 */
@ShellComponent
public class JasyptCommands {


	@Autowired
	private StringEncryptor stringEncryptor;


	@Value("${jasypt.encryptor.password}")
	private String pwd;


	private static final String JasyptCmdGroup = "Jasypt Commands";


	/**
	 * 加密
	 * @param val	待加密字符串
	 * @return
	 */
	@ShellMethod(value = "加密给定的字符串", group = JasyptCmdGroup)
	public String encrypt(@ShellOption(help = "待加密字符串")String val) {

		String result = stringEncryptor.encrypt(val);

		System.out.println("使用密钥："+pwd+" 加密字符串："+val+" 结果是："+result);

		return result;
	}




	/**
	 * 解密
	 * @param val	待解密字符串
	 * @return
	 */
	@ShellMethod(value = "解密给定的字符串", group = JasyptCmdGroup)
	public String decrypt(@ShellOption(help = "待解密字符串")String val) {

		String result = stringEncryptor.decrypt(val);

		System.out.println("使用密钥："+pwd+" 解密字符串："+val+" 结果是："+result);

		return result;
	}



























}
