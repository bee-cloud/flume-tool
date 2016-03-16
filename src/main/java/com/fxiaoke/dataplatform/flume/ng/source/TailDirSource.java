package com.fxiaoke.dataplatform.flume.ng.source;

import com.fxiaoke.dataplatform.flume.ng.util.*;
import com.google.common.base.Preconditions;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    public String persistCopyModel;
    private String interval;
    private String filterDate;
    private String filterDate2;
    private String rotateRegex;
    private int bufferSize;
    private long checkPersistTime;
    public static int lockTimeOut = 0;
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
    private volatile boolean recover = false;

    @Override
    public void configure(Context context) {
        File f = new File(context.getString("dir"));
        Preconditions.checkArgument(f != null, "File should not be null!");
        this.dir = f;
        this.startFromEnd = context.getBoolean("startFromEnd", false);
        this.recurseDepth = context.getInteger("depth", 0);
        this.regex = context.getString("regex");
        Preconditions.checkArgument(regex != null, "regex mustn't null!!!");
        this.checkPeriod = context.getInteger("checkPeriod", 250);
        this.cursorType = CursorType.buildCursorType(context.getString("cursorType", "default"));

        this.interval = context.getString("interval", "");
        this.filterDate = context.getString("filterDate", "");
        this.filterDate2 = context.getString("filterDate2", "");
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
            watcher = createWatcher(dir, regex, rotateRegex, recurseDepth);
        } catch (Exception e) {
            LOG.error("########{}", e.getMessage());
            isDirExist = false;
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


    private DirWatcher createWatcher(File dir, final String regex, final String rotateRegex,
                                     final int recurseDepth) {
        DirWatcher watcher = new DirWatcher(dir, new RegexTimeFileFilter(regex, interval, filterDate, filterDate2, persistAbsPath, rotateRegex), checkPeriod, rotateRegex);
        recover = true;
        watcher.recover = true;
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

            final DateFormat dateFormat = new SimpleDateFormat("HH");
            String persistHour;
            final String nowHour=dateFormat.format(System.currentTimeMillis());

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
                    DirWatcher watcher = createWatcher(f, regex, rotateRegex, recurseDepth - 1);
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

                if (recover) {
                    Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);
                    String fname = f.getName();
                    List<Long> list = offsetMap.get(fname);
                    persistHour= dateFormat.format(new File(persistAbsPath).lastModified());

                    if (list != null) {
                        long offset = list.get(0);
                        long lineNumber = list.get(1);

                        if (offset > c.file.length()) {
                            LOG.warn("maybe file rotate,offset>file.length()");
                        }else if(!persistHour.equals(nowHour)) {
                                LOG.warn(String.format("maybe file rotate,persistHour change from %s to %s",persistHour,nowHour));
                        } else {
                            LOG.info(String.format("recover file:%s offset:%s lineNumber:%s", fname, offset, lineNumber));
                            c.setLastChannelPos(offset);
                            c.setLastChannelSize(offset);
                            c.setLineNumber(lineNumber);
                        }
                    }

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
        recover = false;
        watcher.recover = false;

        return watcher;
    }
}
