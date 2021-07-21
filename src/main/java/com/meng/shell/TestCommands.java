package com.meng.shell;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Set;


@ShellComponent
public class TestCommands {


    @ShellMethod(value = "Add two integers together.",group = "Test Commands")
    public int add(@ShellOption(defaultValue="1",help = "这是第1个值")int a,
                   @ShellOption(help = "这是第2个值")int b) {

        System.out.println("a:"+a+",b:"+b);

        int c = a + b;

        return c;
    }


    public static void main(String[] args) {

        Set<Class<?>> list = ClassUtil.scanPackageByAnnotation("com", ShellComponent.class);


        System.out.println(list.size());
    }



}






































