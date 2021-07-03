package com.meng.shell;

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





}






































