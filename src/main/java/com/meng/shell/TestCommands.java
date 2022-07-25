package com.meng.shell;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import com.google.common.collect.Lists;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.List;


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
//        String accessKey = "123";
//        String secretKey = "95a96197-b00eb08b-c7d985ff-fdce4";
//        DES des = SecureUtil.des(accessKey.getBytes());
//        String desStr = des.encryptBase64(secretKey);
//        System.out.println(desStr);


        File file = new File("C:\\Users\\18514\\Desktop\\test\\要删除的表.txt");
        File file2 = new File("C:\\Users\\18514\\Desktop\\test\\del.sql");

        List<String> tempList = FileUtil.readUtf8Lines(file);
        List<String> tempList2 = Lists.newArrayList();

        for (String item:tempList){
            item = item.trim();
            String str = "drop table "+item+";";
            tempList2.add(str);
        }

        FileUtil.writeUtf8Lines(tempList2,file2);


    }



}






































