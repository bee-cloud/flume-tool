/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.fxiaoke.dataplatform.flume.ng.util;

import com.fxiaoke.dataplatform.flume.ng.source.TailSource;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by wangjiezhao on 2016/3/17.
 */
public class Tools {
    private static final Logger LOG = LoggerFactory.getLogger(Tools.class);

    private static boolean isAlphabet(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static char getDelimiter(String dataFormat) {
        dataFormat = dataFormat.toLowerCase();
        for (char c : dataFormat.toCharArray()) {
            if (!isAlphabet(c)) {
                return c;
            }
        }

        return '-';
    }

    public static long getFileTimestamp(String filePath, String timereg, String timeFormat) throws ParseException {
        LOG.debug("filePath:"+filePath);
        LOG.debug("timereg:"+timereg);
        LOG.debug("timeFormat:"+timeFormat);

        Pattern pdate = Pattern.compile(timereg);
        boolean hasYear = false;
        String dateStr = "";

        long fileTimestamp = 0;

        hasYear = timeFormat.contains("y");

        if (!hasYear) {
            String delimiter = getDelimiter(timeFormat) + "";
            timeFormat = "yyyy" + delimiter + timeFormat;
            int year = Calendar.getInstance().get(Calendar.YEAR);
            dateStr = year + delimiter;
        }

//        SimpleDateFormat fileDateFormat = new SimpleDateFormat(timeFormat, Locale.US);
        SimpleDateFormat fileDateFormat = new SimpleDateFormat(timeFormat);


        Matcher mdate = pdate.matcher(filePath);
        String group = "";
        if (mdate.find()) {
            int groupCount = mdate.groupCount();

            StringBuilder matcherTmp = new StringBuilder();
            String matcher = "";

            for (int i = 1; i <= groupCount; i++) {
                group = mdate.group(i);
                if (Strings.isNullOrEmpty(group)) {
                    LOG.debug("group is null.");
                    group = "00";
                }

                matcherTmp.append(group);
                matcherTmp.append(" ");
            }

            matcher = matcherTmp.toString().trim();

            dateStr += matcher;
            LOG.debug("matcher:"+matcher);
            LOG.debug("datestr:"+dateStr);

            Date dt = fileDateFormat.parse(dateStr);
            fileTimestamp = dt.getTime();
        }

        return fileTimestamp;
    }

    public static void readZipFile(ChannelProcessor channelProcessor,String fileAbsPath,long lineNumber) throws IOException {
        LOG.debug("fileabspath:"+fileAbsPath+" linenumber:"+lineNumber);

        ZipFile zipFile=new ZipFile(fileAbsPath);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();

        ZipEntry zipEntry;

        long count=0;
        String line;
        BufferedReader br;

        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement();

            if(zipEntry==null){
                continue;
            }
            if (!zipEntry.isDirectory()) {
                br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));

                while((line = br.readLine()) != null) {
                    count++;
                    if(count<=lineNumber){
                        continue;
                    }


                    Event event=eventBuild(line,fileAbsPath,lineNumber);
                    channelProcessor.processEvent(event);
                }

                br.close();

            }
        }

        zipFile.close();
    }

    public static void readGZFile(ChannelProcessor channelProcessor,String fileAbsPath, long lineNumber) throws IOException {
        LOG.debug("fileabspath:"+fileAbsPath+" linenumber:"+lineNumber);


        GZIPInputStream in = new GZIPInputStream(new FileInputStream(fileAbsPath));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        long count=0;


        while((line = br.readLine()) != null) {
            count++;
            if(count<=lineNumber){
                continue;
            }


            Event event=eventBuild(line,fileAbsPath,lineNumber);
            channelProcessor.processEvent(event);
        }

        br.close();
        in.close();

    }

   public static Event eventBuild(String body,String absPath,long lineNumber) {
        return eventBuild(body.getBytes(),absPath,lineNumber);
    }

    public static Event eventBuild(byte[] body,String absPath,long lineNumber) {
        Map<String, String> headers = Maps.newHashMap();


        headers.put("abspath",absPath);
        headers.put("lineNumber", Long.toString(lineNumber));
        headers.put("hostname", TailSource.hostname);
        return EventBuilder.withBody(body, headers);
    }
}

