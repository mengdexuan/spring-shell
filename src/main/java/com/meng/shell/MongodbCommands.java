package com.meng.shell;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.meng.util.HelpMe;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.List;
import java.util.Map;


@ShellComponent
public class MongodbCommands {

    //临时文件存储目录
//    String baseDir = "/Users/admin/workSpace/test";
    //临时文件存储目录
    String baseDir = "C:\\Users\\18514\\Desktop\\test4";


    //	万易通测试环境地址
//    String uploadUrl = "http://test.wyt2.ticket.iciyun.net/user/outter/fore/upload/common.do";
    //		万易通正式环境地址
	String uploadUrl = "https://wanyitong.51tangpiao.com/user/outter/fore/upload/common.do";

    public MongoClient client = null ;

    public DB DBClient = null ;

    public MongoDatabase MongoDatabase = null ;
    /**
     * 设置mongodb数据库连接信息，参数示例：
     * set-mongo --ip-and-port 10.10.1.69:27017 --user-and-pass wyt-two:23QzU6250BnLu3wT --db wyt_two_asset
     * @param ipAndPort
     * @param userAndPass
     * @param db
     * @return
     */
    @ShellMethod(value = "1.set Mongo",group = "Mongodb Commands")
    public Object setMongo(@ShellOption(help = "ip和port，eg: localhost:27017")String ipAndPort,
                           @ShellOption(help = "user和pass，eg: root:123456")String userAndPass,
                           @ShellOption(help = "数据库名称，eg: baas_rancher")String db){
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        MongoCredential credential = MongoCredential.createCredential(userAndPass.split(":")[0], db, userAndPass.split(":")[1].toCharArray());
        ServerAddress address = new ServerAddress(ipAndPort.split(":")[0], Integer.valueOf(ipAndPort.split(":")[1]));
        //MongoClient(连接地址,连接池参数)
        client = new MongoClient(address, credential, builder.build());
        MongoDatabase = client.getDatabase(db);
        DBClient = client.getDB(db);
        return "mongodb 数据库链接成功!";
    }

    /**
     * update-mongo payment_company,logo_url
     * @param table
     * @return
     */
    @ShellMethod(value = "2.update Mongo",group = "Mongodb Commands")
    public Object updateMongo(@ShellOption(help = "数据格式:表名,字段名... ：signature_parameter,signature_path")String table) {
        List<String> strList = HelpMe.easySplit(table);
        String tableName = strList.get(0);
        DBCollection dbCollection =  DBClient.getCollection(tableName);
        MongoCollection collection = MongoDatabase.getCollection(tableName);
        String sql = "";
        for (int i=0;i<strList.size();i++){
            if (i==0){
            }else {
                sql += strList.get(i);
                sql += ",";
            }
        }
        DBCursor cursor = dbCollection.find();
        System.out.println("从数据集中读取数据：");
        while (cursor.hasNext()){
            BasicDBObject bdbObj = (BasicDBObject) cursor.next();
            if(bdbObj != null) {
                String[] fieldNames = sql.split(",");
                for (String field : fieldNames) {
                    String  ossUrl = bdbObj.getString(field);//读取mongoDB数据库row的amac集合的指定字段managerName
                    if (StrUtil.isNotEmpty(ossUrl)&&(ossUrl.startsWith("http://") || ossUrl.startsWith("https://"))){
                        dealColumn(field,bdbObj.get("id"),ossUrl,collection);
                    }

                }
            }
        }

        client.close();
        return "complete!";
    }


    private void dealColumn(String columnName,Object id,  String ossUrl, MongoCollection collection){
        System.out.println("原字段 ossUrl 值：" +columnName+" : "+ossUrl);


        String extName = FileUtil.extName(ossUrl);

        String finalName = baseDir + "//" + IdUtil.fastSimpleUUID()+"."+extName;

        HttpUtil.downloadFile(ossUrl,finalName);

        File file = new File(finalName);

        Map<String,Object> param = Maps.newHashMap();
        param.put("file",file);

        String body = HttpUtil.post(uploadUrl, param);

        JSONObject json = JSONUtil.parseObj(body);

        String columnVal = ossUrl;

        String success = json.getStr("success");
        if ("true".equals(success)){
            String obj = json.getStr("obj");
            if (obj.startsWith("http") || obj.startsWith("https")){
                columnVal = obj;
            }
        }
        System.out.println("修改过后字段 ossUrl 值：" +columnVal);

        collection.updateOne(Filters.eq("id",id), new Document("$set",new Document(columnName,columnVal)));

        FileUtil.del(file);

        ThreadUtil.safeSleep(300);
    }

    public static Object setMongodbDbInfo1(){
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.connectionsPerHost(10);//每个地址的最大连接数
        builder.connectTimeout(5000);//连接超时时间
        builder.socketTimeout(5000);//设置读写操作超时时间

        MongoCredential credential = MongoCredential.createCredential("wyt-two", "wyt_two_asset", "23QzU6250BnLu3wT".toCharArray());
        ServerAddress address = new ServerAddress("10.10.1.69", 27017);

        //MongoClient(连接地址,连接池参数)
        MongoClient client = new MongoClient(address,credential,builder.build());

        return client.getDatabase("attachment");
    }

    public static void main(String[] args) {
        setMongodbDbInfo1();
    }

}
