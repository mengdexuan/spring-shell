package com.meng.shell;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;


@ShellComponent
public class TestCommands {


    @ShellMethod(value = "Add two integers together.",group = "Test Commands")
    public int add(@ShellOption(defaultValue="1",help = "这是第1个值")int a,
                   @ShellOption(help = "这是第2个值")int b) {

        System.out.println("a:"+a+",b:"+b);

        int c = a + b;

        return c;
    }



    @ShellMethod(value = "des",group = "Test Commands")
    public Object des(String accessKey,String secretKey) {

        DES des = SecureUtil.des(accessKey.getBytes());
        String desStr = des.encryptBase64(secretKey);

        return desStr;
    }




    public static void main(String[] args) {
        String accessKey = "123";
        String secretKey = "95a96197-b00eb08b-c7d985ff-fdce4";

        DES des = SecureUtil.des(accessKey.getBytes());
        String desStr = des.encryptBase64(secretKey);

        System.out.println(desStr);
    }



}






































