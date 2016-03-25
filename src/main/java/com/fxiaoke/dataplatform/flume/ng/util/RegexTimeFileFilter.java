/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.fxiaoke.dataplatform.flume.ng.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * 提供1.按正则过滤文件 2.按正则和文件中的日期过滤文件
 */
public class RegexTimeFileFilter implements FileFilter {
    static final Logger LOG = LoggerFactory.getLogger(RegexTimeFileFilter.class);
    private Pattern p;

    private String interval;
    private String filterDateStart;
    private String filterDateEnd;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String timeReg;
    private String timeFormat;

    long timestampStart;
    long timestampEnd;

    long fileTimestamp;

    public RegexTimeFileFilter(String regex, String interval, String filterDateStart, String filterDateEnd,String timeReg,String timeFormat) {
        this.p = Pattern.compile(regex);
        this.interval = interval;
        this.filterDateStart = filterDateStart;
        this.filterDateEnd = filterDateEnd;
        this.timeReg=timeReg;
        this.timeFormat=timeFormat;

        LOG.debug("regex:" + regex);

        LOG.debug("interval:" + interval);
        LOG.debug("filterDateStart:" + filterDateStart);
        LOG.debug("filterDateEnd:" + filterDateEnd);
    }

    @Override
    public boolean isSelected(File f) {
        LOG.debug("scp regex enter selecte");
        LOG.debug("filename=" + f.getName());

        boolean isMatch = p.matcher(f.getName()).matches();
        boolean rb;

        timestampStart=0;
        timestampEnd=0;

        if (!isMatch) {
            return false;
        }

        try {
            if (!filterDateStart.equals("")) {
                timestampStart = formatter.parse(filterDateStart).getTime();
            }else{
                return true;
            }

            if (!filterDateEnd.equals("")) {
                timestampEnd = formatter.parse(filterDateEnd).getTime();

                if (timestampEnd < timestampStart && timestampStart > 0) {
                    LOG.warn("filterDateEnd should be gt filterDateStart");
                    return false;
                }
            }

            fileTimestamp = Tools.getFileTimestamp(f.getAbsolutePath(),timeReg, timeFormat);

            LOG.debug("fileTimestamp:" + fileTimestamp);
            LOG.debug("timestampStart:" + timestampStart);
            LOG.debug("timestampEnd:" + timestampEnd);

            if (interval.equals("")) {
                rb = true;
            } else if (interval.equals("eq")) {
                rb = fileTimestamp == timestampStart;
            } else if (interval.equals("lt")) {
                rb = fileTimestamp < timestampStart;
            } else if (interval.equals("le")) {
                rb = fileTimestamp <= timestampStart;
            } else if (interval.equals("gt")) {
                rb = fileTimestamp > timestampStart;
            } else if (interval.equals("ge")) {
                rb = fileTimestamp >= timestampStart;
            } else if (interval.equals("ne")) {
                rb = fileTimestamp != timestampStart;
            } else if (interval.equals("range")) {
                rb = fileTimestamp >= timestampStart && fileTimestamp <= timestampEnd;
            } else {
                rb = true;
            }
        } catch (ParseException e) {
            LOG.warn("fileSelect error:",e);
            return false;
        }

        return rb;
    }

}
