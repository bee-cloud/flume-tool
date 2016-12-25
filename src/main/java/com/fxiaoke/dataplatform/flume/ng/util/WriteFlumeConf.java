package com.fxiaoke.dataplatform.flume.ng.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by wangjiezhao on 2016/3/22.
 */
public class WriteFlumeConf {
    private static final Logger LOG = LoggerFactory.getLogger(WriteFlumeConf.class);
    final private static String confTemplate = "\n" +
            "############################################################################################################\n" +
            "agent.sources.kafka${number}.type = com.fxiaoke.dataplatform.flume.ng.source.TailDirSource\n" +
            "agent.sources.kafka${number}.dir=${dir}\n" +
            "agent.sources.kafka${number}.regex=${fileName}\\\\.(?:\\\\d{4}-\\\\d{2}-\\\\d{2}-\\\\d{2}\\\\.log)\n" +
            "agent.sources.kafka${number}.rotateRegex= ${fileName}\\\\.(?:\\\\d{4}-\\\\d{2}-\\\\d{2}-\\\\d{2}\\\\.log)\n" +
            "agent.sources.kafka${number}.timeReg=.*(\\\\d{4}-\\\\d{2}-\\\\d{2}-\\\\d{2})\\\\.log.*\n" +
            "agent.sources.kafka${number}.timeFormat=yyyy-MM-dd-HH\n" +
            "agent.sources.kafka${number}.checkPeriod=60000\n" +
            "agent.sources.kafka${number}.logicalNode= ${logicalNode}\n" +
            "agent.sources.kafka${number}.persistDir= /opt/fs/flume/logs\n" +
            "agent.sources.kafka${number}.channels = kafka${number}\n" +
            "\n" +
            "agent.channels.kafka${number}.type = memory\n" +
            "agent.channels.kafka${number}.capacity = 10000\n" +
            "agent.channels.kafka${number}.transactionCapacity = 1\n" +
            "\n" +
            "agent.sinks.kafka${number}.type =  org.apache.flume.sink.kafka.KafkaSink\n" +
            "agent.sinks.kafka${number}.channel = kafka${number}\n" +
            "agent.sinks.kafka${number}.topic = ${topic}\n" +
            "#agent.sinks.kafka${number}.brokerList =172.17.43.5:9092,172.17.43.6:9092,172.17.43.7:9092\n" +
            "#agent.sinks.kafka${number}.brokerList = vlnx043005:9092,vlnx043006:9092,vlnx043007:9092\n" +
            "agent.sinks.kafka${number}.brokerList = ${brokerList}\n" +
            "agent.sinks.kafka${number}.requiredAcks = 0\n" +
            "agent.sinks.kafka${number}.batchSize=1\n";

    public synchronized static void writeConf(Set<String> fileSet, String outputPath) throws Exception {
        if (fileSet.size() == 0) {
            return;
        }

        TreeSet<String> treeSet = new TreeSet<String>(fileSet);

        File file = new File(outputPath);

        int count = 1;

        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file, true);

        String headerTemplate = "agent.sources= ${header}\n" +
                "agent.sinks= ${header}\n" +
                "agent.channels= ${header}\n";

        StringBuilder headerBuf = new StringBuilder();
        String header;
        for (int i = 1; i <= treeSet.size(); i++) {
            headerBuf.append(" kafka" + i);
        }
        header = headerBuf.toString();

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("header", header);

        String headerConf = template(headerTemplate, dataMap);
        out.write(headerConf.getBytes("UTF-8"));

        for (String fileName : treeSet) {
            out.write(createConf(fileName, count).getBytes("UTF-8"));

            count++;
        }
        out.close();
    }

    public static String template(String templateContent, Map<String, Object> dataMap) {

        Configuration cfg = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("myTemplate", templateContent);

        cfg.setTemplateLoader(stringLoader);

        try {
            Template template = cfg.getTemplate("myTemplate", "utf-8");
            StringWriter writer = new StringWriter();
            try {
                template.process(dataMap, writer);
                return writer.toString();
            } catch (TemplateException e) {
                LOG.warn("TemplateException:", e);
            }
        } catch (IOException e) {
            LOG.warn("IOException", e);
        }

        return "";
    }


    public static String createConf(String fileAbsPath, int number) {
        File file = new File(fileAbsPath);
        String dir = file.getParent();
        String fileName = file.getName();

        fileName = fileName.replaceAll("(?:\\.|_)?\\d{4}.*log", "");
        String logicalNode = new File(dir).getName() + "." + fileName;


        String brokerList = "vlnx103126:9092,vlnx103127:9092,vlnx103128:9092"; //测试环境zookeeper集群

        File confFile = new File("/opt/fs/flume/conf/base.yaml");
        if (!confFile.exists()) {
            LOG.warn("base.yaml not exists");
        } else {
            try {
                InputStream input = new FileInputStream(confFile);

                Yaml yaml = new Yaml();


                Map<String, String> map = (Map<String, String>) yaml.load(input);
                if (map.containsKey("brokerList")) {
                    brokerList = map.get("brokerList");
                }
            } catch (FileNotFoundException e) {
                LOG.warn("exception base.yaml not exists");
            }

        }


        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("number", number);
        dataMap.put("dir", dir);
        dataMap.put("fileName", fileName);
        dataMap.put("logicalNode", logicalNode);
        dataMap.put("topic", logicalNode);
        dataMap.put("brokerList", brokerList);

        String conf = template(confTemplate, dataMap);
        return conf;

    }
}
