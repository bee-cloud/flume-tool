package com.fxiaoke.dataplatform.flume.ng.source;

import com.fxiaoke.dataplatform.flume.ng.util.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;

import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;


/**
 *监控指定目录下符合指定正则的文件列表，以tailf方式读取文件内容
 */
public class TailDirSource extends AbstractSource implements EventDrivenSource,
        Configurable {

    public static final Logger LOG = LoggerFactory.getLogger(TailDirSource.class);

    private DirWatcher watcher;
    private ConcurrentMap<String, DirWatcher> subdirWatcherMap;
    private TailSource tail;
    private File dir;
    private String regex;
    private boolean startFromEnd;
    private int recurseDepth;
    private int checkPeriod;
    private String logicalNode;
    String persistDir;

    private long recoverOffset=0;
    private long recoverLineNumber=0;

    public String persistCopyModel;

    private String interval;
    private String filterDateStart;
    private String filterDateEnd;
    private String rotateRegex;
    private int bufferSize;
    private long checkPersistTime;
    public static int lockTimeOut = 0;
    private String timeReg;
    private String timeFormat;

    int waitTime;
     boolean isRemoveCursor = false;
    long maxNoChangeTime;
    private CursorType cursorType;
    private String persistAbsPath;
    private boolean isDirExist = true;

    boolean initIsRotate;

    private SourceCounter sourceCounter;

    private boolean isRotateEnable = false;
    private long rotateInterval = 0;

    private volatile boolean dirChecked = false;


    @Override
    public void configure(Context context) {
        File f = new File(context.getString("dir"));
        Preconditions.checkArgument(f != null, "File should not be null!");
        this.dir = f;
        Preconditions.checkArgument(dir.exists()&&dir.isDirectory(),"dir not exists or not directory");

        this.startFromEnd = context.getBoolean("startFromEnd", false);
        this.recurseDepth = context.getInteger("depth", 0);
        this.regex = context.getString("regex");
        Preconditions.checkArgument(regex != null, "regex mustn't null!!!");
        this.checkPeriod = context.getInteger("checkPeriod", 250);
        this.cursorType = CursorType.buildCursorType(context.getString("cursorType", "default"));

        this.interval = context.getString("interval", "");
        this.filterDateStart = context.getString("filterDateStart", "");
        this.filterDateEnd = context.getString("filterDateEnd", "");
        this.rotateRegex = context.getString("rotateRegex", "");
        this.waitTime = context.getInteger("waitTime", 100);
        this.logicalNode = context.getString("logicalNode");
        Preconditions.checkArgument(logicalNode != null, "logicalNode should not be null!");
        this.persistDir = context.getString("persistDir");
        this.bufferSize = context.getInteger("bufferSize", 2 << 20);
        this.checkPersistTime = context.getLong("checkPersistTime", 100L);
        this.lockTimeOut = context.getInteger("lockTimeOut", 1000);
        this.isRemoveCursor = context.getBoolean("isRemoveCursor", false);
        this.maxNoChangeTime = context.getLong("maxNoChangeTime", 60 * 60 * 1000L);

        this.isRotateEnable = context.getBoolean("isRotateEnable", false);
        this.rotateInterval = context.getInteger("rotateInterval", 3600 * 1000);
        Preconditions.checkArgument(rotateInterval >= 0, "rotateInterval must be a positive number!");
        this.persistCopyModel = context.getString("persistCopyModel", "channel").trim().toLowerCase();

        this.timeReg=context.getString("timeReg","");
        this.timeFormat=context.getString("timeFormat","");


        persistAbsPath = PersistOffset.getOffsetFile(persistDir, logicalNode);
        this.tail = new TailSource(waitTime, isRemoveCursor, maxNoChangeTime);

        if (sourceCounter == null) {
            sourceCounter = new SourceCounter(getName());
        }
    }

    @Override
    public void start() {
        Preconditions.checkState(watcher == null,
                "Attempting to open an already open TailDirSource (" + dir
                        + ", \"" + regex + "\")");
        subdirWatcherMap = new ConcurrentHashMap<String, DirWatcher>();
        tail.channelProcessor = getChannelProcessor();

        try {
            watcher = createWatcher(dir, regex, recurseDepth);
        } catch (Exception e) {
            LOG.error("exception:", e);
            isDirExist = false;
        }

        try {
            recover();
        }catch(Exception e){
            LOG.warn("exception:",e);
        }

        dirChecked = true;
        watcher.start();
        tail.setLogicalNode(logicalNode);
        tail.setRotateRegex(rotateRegex);
        LOG.debug("start tailsource open");
        tail.setMode("taildir");
        sourceCounter.start();
        tail.start();
    }


    @Override
    public void stop() {
        LOG.info("TailDirSource stop ....");
        tail.stop();
        this.watcher.stop();
        sourceCounter.stop();
        this.watcher = null;
        for (DirWatcher watcher : subdirWatcherMap.values()) {
            watcher.stop();
        }
        subdirWatcherMap = null;
        super.stop();
    }

    /**
     * 监控指定目录下文件变更，使用正则和日期信息过滤文件
     * @param dir
     * @param regex
     * @param recurseDepth
     * @return
     */
    private DirWatcher createWatcher(File dir, final String regex,final int recurseDepth) {
        DirWatcher watcher = new DirWatcher(dir, new RegexTimeFileFilter(regex, interval, filterDateStart, filterDateEnd,timeReg,timeFormat), checkPeriod);

        boolean flags;
        if (!isDirExist) {
            flags = true;
            isDirExist = true;
        } else {
            flags = watcher.isFlags();
        }

        final boolean finalFlags = flags;
        watcher.addHandler(new DirChangeHandler() {

            Map<String, Cursor> curmap = new HashMap<String, Cursor>();

            @Override
            public void fileCreated(File f) {
                if (f.isDirectory()) {
                    if (recurseDepth <= 0) {
                        LOG.debug("Tail dir will not read or recurse "
                                + "into subdirectory " + f + ", this watcher recurseDepth: "
                                + recurseDepth);
                        return;
                    }

                    LOG.info("added dir " + f + ", recurseDepth: " + (recurseDepth - 1));
                    DirWatcher watcher = createWatcher(f, regex,recurseDepth - 1);
                    watcher.start();
                    subdirWatcherMap.put(f.getPath(), watcher);
                    return;
                }

                LOG.info("added file " + f);
                Cursor c;
               if (startFromEnd && !dirChecked) {
                 c = new Cursor(tail.channelProcessor, f, f.length(), f.length(), f.lastModified(),
                         rotateRegex, persistAbsPath, bufferSize, checkPersistTime, sourceCounter,
                         isRotateEnable, rotateInterval, cursorType, persistCopyModel);
               } else {
                 c = new Cursor(tail.channelProcessor, f, rotateRegex, persistAbsPath, bufferSize,
                         checkPersistTime, sourceCounter, isRotateEnable, rotateInterval, cursorType,
                         persistCopyModel);
               }

                curmap.put(f.getPath(), c);
                tail.addCursor(c, finalFlags ? finalFlags : f.getName().matches(regex));
            }

            @Override
            public void fileDeleted(File f) {
                LOG.debug("handling deletion of file " + f);
                String fileName = f.getPath();
                DirWatcher watcher = subdirWatcherMap.remove(fileName);
                if (watcher != null) {
                    LOG.info("removed dir " + f);
                    LOG.info("stopping watcher for dir: " + f);
                    watcher.stop();
                    watcher.check();
                    return;
                }

                Cursor c = curmap.remove(fileName);
                if (c != null) {
                    LOG.info("removed file " + f);
                    tail.removeCursor(c);
                }
            }

        });

        Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);
        List<Long> list = offsetMap.get("isRotate");
        if (list != null) {
            initIsRotate = list.get(0) > 0;
            if (initIsRotate && !rotateRegex.equals("")) {
                watcher.initIsRotate = true;
            }
        }

        watcher.check();

        return watcher;
    }

    /**
     * 根据持久化的偏移量恢复日志读取，
     * 长时间故障时，可收集最后一次持久化时间后的日志
     */
    public void recover() throws ParseException, IOException, InterruptedException {
        Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);
        if (offsetMap.size() == 0) {
            return;
        }

        long zpFile=0;

        String replaceFileName;

        List<String> recoverFiles=checkRecoverFiles();

        if(recoverFiles.size()==0){
            LOG.info("recoverFiles is null");
            return;
        }

        Cursor c;

        String file;
        while(true) {
            for (String fileName : recoverFiles) {
                LOG.info("recover read file:"+fileName);

                file=new File(fileName).getName();
                replaceFileName=file.replaceAll(".gz|.zip","");
                setRecoverPosition(offsetMap,replaceFileName);

                if (fileName.endsWith("zip")) {
                        Tools.readZipFile(tail.channelProcessor, fileName, recoverLineNumber);
                } else if (fileName.endsWith("gz")) {
                        Tools.readGZFile(tail.channelProcessor, fileName, recoverLineNumber);
                } else {
                    c = new Cursor(tail.channelProcessor, new File(fileName), rotateRegex, persistAbsPath, bufferSize,
                            checkPersistTime, sourceCounter, isRotateEnable, rotateInterval, cursorType,
                            persistCopyModel);
                    c.setLastChannelPos(recoverOffset);
                    c.setLineNumber(recoverLineNumber);
                    c.initCursorPos();
                    c.tailBody();
                    c.flush();
                }

            }

            recoverFiles=checkRecoverFiles();

            if(recoverFiles.size()==0){
                LOG.info("recoverFiles is null");
                return;
            }

            zpFile=0;

            for(String fileName:recoverFiles){
                if(fileName.endsWith("gz")||fileName.endsWith("zip")){
                    zpFile++;
                }
            }

            if(zpFile==0){
                return;
            }
        }
    }

    /**
     * 从持久化文件中恢复日志偏移量信息
     */
    public void setRecoverPosition( Map<String, List<Long>> offsetMap,String fileName){
        List<Long> persistList=offsetMap.get(fileName);
        if (persistList != null) {
            recoverOffset = persistList.get(0);
            recoverLineNumber = persistList.get(1);
            LOG.info("recover FileName:"+fileName+" offset="+recoverOffset+"  lineNumber="+recoverLineNumber);
        }else{
            recoverOffset=0;
            recoverLineNumber=0;
        }
    }


    public List<String> checkRecoverFiles() throws ParseException {
        List<String> recoverFiles=new ArrayList<String>();
        Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);
        if (offsetMap.size() == 0) {
            return recoverFiles;
        }

        String absPath;
        long timestamp;

        DateFormat logDateFormat;


        if(!timeFormat.equals("")){
            logDateFormat=new SimpleDateFormat(timeFormat);
        }else{
            logDateFormat=new SimpleDateFormat("yyyy-MM-dd-HH");
        }

        long persistTimestamp=new File(persistAbsPath).lastModified();
        persistTimestamp=logDateFormat.parse(logDateFormat.format(persistTimestamp)).getTime();

        File checkDir = dir;
        FileFilter fileFilter = new RegexFileFilter(regex+"(?:\\.zip|\\.gz)?");
        File[] files = checkDir.listFiles(fileFilter);

        if(files.length==0){
            LOG.warn("recover recoverList is null");
            return  recoverFiles;
        }

        for(File fileName:files) {
            absPath=fileName.getAbsolutePath();

            LOG.debug("file:"+absPath+"timereg:"+timeReg+"timeformat:"+timeFormat);
            timestamp = Tools.getFileTimestamp(absPath, timeReg, timeFormat);
            if(timestamp>=persistTimestamp){
                recoverFiles.add(absPath);
            }
        }

        Collections.sort(recoverFiles);

        return recoverFiles;
    }
}
