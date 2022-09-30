package com.meng.shell;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.druid.pool.DruidDataSource;
import com.meng.util.fsn.FsnInfo;
import com.meng.util.fsn.FsnReader;
import com.meng.util.fsn.FsnRow;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 淮安冠字号码转换
 * @author mengdexuan on 2022/7/8 10:59.
 */
@ShellComponent
public class GzhCommands {

    String baseDir;
    String excelDir;


    @ShellMethod(value = "1.set dir",group = "Gzh Trans")
    public Object setDir(@ShellOption(help = "uploaded 目录位置，eg: C:\\Users\\18514\\Desktop\\test4\\Log\\uploaded")String baseDir,
                           @ShellOption(help = "生成的 excel 目录位置，eg: C:\\Users\\18514\\Desktop\\test4\\file")String excelDir) {

        this.baseDir = baseDir;
        this.excelDir = excelDir;

        System.out.println("baseDir : "+baseDir);
        System.out.println("excelDir : "+excelDir);

        return "";
    }


    /*
        将 FSN zip 文件转换为 excel 文件
     */
    @ShellMethod(value = "2.trans zip to excel",group = "Gzh Trans")
    public Object fsnTrans() {

        File[] fileList = FileUtil.ls(baseDir);

        for (File file : fileList) {
            signal(file.getAbsolutePath());
        }

        return "转换完成！";
    }





    private void signal(String baseDir) {
//        String baseDir = "C:\\Users\\18514\\Desktop\\test4\\uploaded\\2022-04-07";

        List<File> fileList = FileUtil.loopFiles(baseDir);

//        List<String> row0 = CollUtil.newArrayList("冠字号码", "交易时间", "机构号", "设备编号", "币值");
        List<String> row0 = CollUtil.newArrayList("点钞时间", "机具编码", "券别", "冠字号码", "币种","版本","钞票状态");
        List<List<String>> rows = CollUtil.newArrayList();
        rows.add(row0);

        String fileName = "";
        try {
            for (File file : fileList) {
                fileName = parse(rows, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StrUtil.isNotEmpty(fileName)) {
            write(rows, fileName);
        }
    }

    private String parse(List<List<String>> rows, File file) throws Exception {
        String orgNo = "320010";

        String absPath = file.getAbsolutePath();

        if (FileUtil.extName(file).equalsIgnoreCase("zip")) {

            System.out.println("处理文件：" + absPath);

            File unZipFile = ZipUtil.unzip(absPath);

            String tempPath = unZipFile + "/" + unZipFile.getName();

            FsnReader reader = new FsnReader(tempPath);

            FsnInfo info = reader.readFsn();

            ArrayList<FsnRow> list = info.getFsnRows();

            String fileName = "";
            for (FsnRow item : list) {
                fileName = item.getDateStr();

                String time = DateUtil.format(DateUtil.parse(item.getDateTime()), DatePattern.PURE_DATETIME_PATTERN);
                String machineSno = item.getMachineSno();
                int valuta = item.getValuta();
                String sno = item.getSno();
                String moneyFlag = item.getMoneyFlag();

                //	Ver年版或版本号标志；人民币用作年版标志，值填0,1,2，分别代表1990,1999,2005三个年版，可根据实际情况扩充；其余币种填9999（表示不考虑年版）；
                int ver = item.getVer();
                String verStr = "9999";
                if (ver==0){
                    verStr = "1990";
                }else if (ver==1){
                    verStr = "1999";
                }else if (ver==2){
                    verStr = "2005";
                }

                int tfFlag = item.getTfFlag();

                String arr[] = machineSno.split("/");
                List<String> row = CollUtil.newArrayList(time,machineSno,valuta+"",sno,moneyFlag,verStr,tfFlag+"");
                rows.add(row);
            }
            return fileName;

        }

        return "";
    }


    private void write(List<List<String>> rows, String fileName) {

        List<String> row0 = rows.get(0);

        //通过工具类创建writer
//        ExcelWriter writer = ExcelUtil.getWriter("C:\\Users\\18514\\Desktop\\test4\\file\\" + fileName + ".xls");
        ExcelWriter writer = ExcelUtil.getWriter(excelDir+File.separator + fileName + ".csv");
//合并单元格后的标题行，使用默认标题样式
        writer.merge(row0.size() - 1, "数据");
//一次性写出内容，强制输出标题
        writer.write(rows, true);
//关闭writer，释放内存
        writer.close();

        System.out.println("生成 excel 文件：" + fileName + ".csv");
    }


}
