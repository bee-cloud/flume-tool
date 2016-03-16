package com.fxiaoke.dataplatform.flume.ng.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提供1.按正则过滤文件 2.按正则和文件中的日期过滤文件
 */
public class RegexTimeFileFilter implements FileFilter {
    static final Logger LOG = LoggerFactory.getLogger(RegexTimeFileFilter.class);
    Pattern p;
    static final Pattern pFile = Pattern.compile("(?is)([0-9-_]|gz$)+");
    private String interval;
    private String filterDate;
    private String filterDate2;
    private String persistAbsPath;
    private boolean recover = true;
    private String rotateRegex;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RegexTimeFileFilter(String regex, String interval, String filterDate, String filterDate2, String persistAbsPath, String rotateRegex) {
        this.p = Pattern.compile(regex);
        this.interval = interval;
        this.filterDate = filterDate;
        this.filterDate2 = filterDate2;
        this.persistAbsPath = persistAbsPath;
        this.rotateRegex = rotateRegex;

        LOG.debug("scp regex" + regex);
        LOG.debug("scp regex" + interval);
        LOG.debug("scp regex" + filterDate);
        LOG.debug("scp regex persistAbsPath:" + persistAbsPath);
        LOG.debug("scp rotateregex:" + rotateRegex);
    }

    public void setRecover(boolean b) {
        recover = b;
    }

    public boolean getRecover() {
        return recover;
    }

    @Override
    public boolean isSelected(File f) {
        LOG.debug("scp regex enter selecte");
        LOG.debug("filename=" + f.getName());



        boolean regx = p.matcher(f.getName()).matches();
        boolean rb = false;

        long timestamp = 0;
        long timestamp2 = 0;
        long lastModified = 0;
        long fileTimestamp = 0;

        LOG.debug("regx=" + regx);

        if (!regx) {
            return regx;
        }

        try {
            if (!filterDate.equals("")) {
                timestamp = formatter.parse(filterDate).getTime();
            }
            timestamp2 = 0;

            if (!filterDate2.equals("")) {
                timestamp2 = formatter.parse(filterDate2).getTime();

                if (timestamp2 < timestamp && timestamp > 0) {
                    LOG.warn("filterDate should be gt filterDate2");
                    return false;
                }
            }

            lastModified = f.lastModified();
            fileTimestamp = new Date(lastModified).getTime();

            LOG.debug("scp regex fileTimestamp:" + fileTimestamp);
            LOG.debug("scp regex timestamp:" + timestamp);

            if (interval.equals("")) {
                rb = regx;
            } else if (interval.equals("eq")) {
                //return fileTimestamp==timestamp;
                rb = fileTimestamp == timestamp;
            } else if (interval.equals("lt")) {
                rb = fileTimestamp < timestamp;
            } else if (interval.equals("le")) {
                rb = fileTimestamp <= timestamp;
            } else if (interval.equals("gt")) {
                rb = fileTimestamp > timestamp;
            } else if (interval.equals("ge")) {
                rb = fileTimestamp >= timestamp;
            } else if (interval.equals("ne")) {
                rb = fileTimestamp != timestamp;
            } else if (interval.equals("range")) {
                rb = fileTimestamp >= timestamp && fileTimestamp <= timestamp2;
            } else {
                rb = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        LOG.debug("rb=" + rb);
        LOG.debug("recover=" + recover);

        if (!rb) {
            return rb;
        }

        String fileName = f.getName();
        Matcher m1 = pFile.matcher(fileName);
        String fileSub1 = m1.replaceAll("");

        if (fileSub1.equals(fileName)) {
            if (rotateRegex.equals("")) {
                LOG.warn("filesub error");
                return false;
            } else {
                return true;
            }
        }

        Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);

        LOG.debug("offsetMap size:" + offsetMap.size());

        if (offsetMap.size() == 0) {
            setRecover(false);
            return rb;
        }
        boolean rb2 = false;

        for (Map.Entry<String, List<Long>> map : offsetMap.entrySet()) {
            String filePath = map.getKey();
            Matcher m = pFile.matcher(filePath);
            String fileSub = m.replaceAll("");

            LOG.debug(String.format("filesub:%s filepath:%s", fileSub, filePath));

            if (fileSub.equals(filePath)) {
                continue;
            }

            LOG.debug(String.format("filesrc:%s filedst:%s filesub:%s", filePath, fileName, fileSub));

            if (fileSub1.equals(fileSub)) {
                rb = fileName.compareTo(filePath) >= 0;
                LOG.debug("rb:" + rb);
                if (rb) {
                    rb2 = true;
                }
            }
        }

        if (rb2) {
            return rb2;
        } else {
            return rb;
        }

    }

}
