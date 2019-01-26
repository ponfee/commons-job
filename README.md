Simply schedule job
==================

# 简介

An easy and simply schedule job framework
<br/>

## 目录说明

- `src/main/java/`

Java源码，包含SpringBoot的启动类SpringBootStartup

- `src/main/resources/`

application.yml 全局资源配置文件  
application-jdbc.yml 数据库相关配置文件  
application-redis.yml redis相关配置文件  
application-mvc.yml 页面mvc相关配置文件  
<br/>

## 使用说明

执行两个数据库建表脚本：  
- sql-script/quartz_tables_mysql_innodb.sql<br/>
- sql-script/job_tables_mysql.sql<br/>

任务调度的两种方式（选择使用其中一个）：  
- quartz方式：resources/spring/spring-quartz-job.xml<br/>
- simply方式：resources/spring/spring-spy-job.xml<br/>

在resources/spring/spring-bean.xml文件中以上两者选（引入）其一

## Dockerfile使用步骤  
1、使用gradle的bootRepackage进行打包  
2、Dockerfile目录下使用命令：docker build -t springboot:v1.0 .  
3、启动本地redis，并修改application-dev.yml中redis的IP地址为宿主机的IP地址如：192.168.1.111，mysql的IP地址同理  
4、使用命令：docker run --name springbootTemplate -d -p 8080:8080 springboot:v1.0  
5、直接访问测试地址即可  
<br/>

### 注意事项

- `Dockerfile`中的`APP_NAME`对应`jar.baseName-jar.version`
- `Dockerfile`中的`APP_PORT`&`EXPOSE`根据项目情况填写
<br/>

## 快速启动和停止应用的脚本
app.sh脚本为快速启动应用和关闭应用的脚本，使用方法如下：  

首先，将你需要发布的jar包，和含有上述内容的脚本app.sh，上传至linux服务器，**注意两者必须处于同一目录**，并且该目录下只有一个jar包，并给与app.sh相应执行权限，chmod 777 app.sh

然后就可以执行脚本，命令如下

| 命令 | 作用 |
| :-: | :-: |
| ./app.sh start | 启动应用 |
| ./app.sh stop | 关闭应用 |
| ./app.sh restart | 重启应用 |
| ./app.sh status | 查看应用状态 |
| ./app.sh stop -f | 强制kill应用进程  |

**注意，重新发布应用时，先stop再上传替换jar包**

脚本中可以修改的地方：  
19行： nohup java -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -Xms512M -Xmx4G -jar $appName > /dev/null 2>&1 &  
这是最终jar的启动命令，在这里你需要对gc、Xms、Xmx等针对你机器的实际情况修改，还可以添加你所需要的启动参数等。  

56行： for i in {3..1}  
这里是设置restart的时候等待的时间，因为有的项目在3秒之内可能没有办法正常停止，所以可以调整为5秒，保证应用确实正常停止后再启动  
