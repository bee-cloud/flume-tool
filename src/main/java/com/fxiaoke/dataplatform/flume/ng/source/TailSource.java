package com.fxiaoke.dataplatform.flume.ng.source;

import com.fxiaoke.dataplatform.flume.ng.util.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 以tailf方式读取指定文件内容
 */
public class TailSource extends AbstractSource implements EventDrivenSource,        Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(TailSource.class);
    public static final String A_TAILSRCFILE = "tailSrcFile";
    private boolean isRotate = false;

    public long historyFileMod;
    public long historyChannelPos;
    public long historyChannelSize;
    public long historylineNumber;
    private String logicalNode;
    private String persistDir;

    private String rotateRegex;
    private Pattern rotatePattern;
    private String runMode = "tail";

    ChannelProcessor channelProcessor;
    private static int thdCount = 0;
    private volatile boolean done = false;

    private Set<String> offSet = new HashSet<String>();

    private long sleepTime = 100; //毫秒
    final List<Cursor> cursors = new ArrayList<Cursor>();
    private final List<Cursor> newCursors = new ArrayList<Cursor>();
    private final List<Cursor> rmCursors = new ArrayList<Cursor>();

    private TailThread thd = null;

    public static String hostname;

    private File file;
    private long fileLen;
    private boolean startFromEnd;
    private long offset;
    private boolean flags = false;
    private int bufferSize;
    private long checkPersistTime;
    public static int lockTimeOut = 0;
    private boolean isRemoveCursor = false;
    private long maxNoChangeTime;
    private SourceCounter sourceCounter;
    private String persistAbsPath;
    private boolean isRotateEnable = false;
    private long rotateInterval = 0;
    private CursorType cursorType;
    private String persistCopyModel;
    private boolean isPersist=true;

    public TailSource() {
        LOG.info("Starting TailSource.....");
    }


    public TailSource(long waitTime, boolean isRemoveCursor, long maxNoChangeTime) {
        this.sleepTime = waitTime;
        this.isRemoveCursor = isRemoveCursor;
        this.maxNoChangeTime = maxNoChangeTime;
        initHostName();
    }

    @Override
    public void configure(Context context) {
        sleepTime = context.getInteger("waitTime", 100);
        String fileName = context.getString("file");

        Preconditions.checkArgument(fileName != null, "Null File is an illegal argument");
        Preconditions.checkArgument(sleepTime > 0, "waitTime <=0 is an illegal argument");

        file = new File(fileName);
        fileLen = file.length();
        startFromEnd = context.getBoolean("startFromEnd", false);
        offset = context.getLong("offset", 0L);
        logicalNode = context.getString("logicalNode");
        Preconditions.checkArgument(logicalNode != null, "logicalNode  is an illegal argument");
        persistDir = context.getString("persistDir");
        rotateRegex = context.getString("rotateRegex", "");
        this.cursorType = CursorType.buildCursorType(context.getString("cursorType", "default"));
        this.bufferSize = context.getInteger("bufferSize", 2 << 20);
        this.checkPersistTime = context.getLong("checkPersistTime", 100L);
        this.lockTimeOut = context.getInteger("lockTimeOut", 1000);
        this.isRemoveCursor = context.getBoolean("isRemoveCursor", false);
        this.maxNoChangeTime = context.getLong("maxNoChangeTime", 60 * 60 * 1000L);
        this.persistCopyModel = context.getString("persistCopyModel", "channel").trim().toLowerCase();

        this.isRotateEnable = context.getBoolean("isRotateEnable", false);
        this.rotateInterval = context.getInteger("rotateInterval", 3600 * 1000);
        Preconditions.checkArgument(rotateInterval >= 0, "rotateInterval must be a positive number!");

        persistAbsPath = PersistOffset.getOffsetFile(persistDir, logicalNode);

        if (sourceCounter == null) {
            sourceCounter = new SourceCounter(getName());
        }
        flags = true;
    }

    @Override
    public void start() {
        if (thd != null) {
            throw new IllegalStateException("Attempted to open tail source twice!");
        }

            rotatePattern = Pattern.compile(rotateRegex);

        if (flags) {
            long readOffset = startFromEnd ? fileLen : offset;
            initHostName();
            sourceCounter.start();
            Cursor c;
            channelProcessor = getChannelProcessor();



        if (startFromEnd) {
            c = new Cursor(channelProcessor, file, readOffset, readOffset, file.lastModified(),
                    rotateRegex, persistAbsPath, bufferSize, checkPersistTime, sourceCounter,
                    isRotateEnable, rotateInterval, cursorType, persistCopyModel);
        } else {
            c = new Cursor(channelProcessor, file, rotateRegex, persistAbsPath, bufferSize,
                    checkPersistTime, sourceCounter, isRotateEnable, rotateInterval, cursorType,
                    persistCopyModel);
        }

            addCursor(c, true);
        }

        
        if (runMode != null && runMode.equals("tail")) {
            recover();
        }
        

        thd = new TailThread();
        thd.start();
    }

    @Override
    public void stop() {
        LOG.info("TailSource stop ....");
        try {
            synchronized (this) {
                if (flags) {
                    sourceCounter.stop();
                }
                done = true;
                if (thd == null) {
                    LOG.warn("TailSource double closed");
                    return;
                }
                while (thd.isAlive()) {
                    thd.join(100L);
                    thd.interrupt();
                }
                thd = null;
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for  runner to stop.", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.warn("Exception while stop TailSource", e);
        }
    }

    private void initHostName() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e2) {
            LOG.error("Unknow Host {} Exception.");
        } finally {
            String hostname = "";
            if (address != null) {
                hostname = address.getHostName();

                this.hostname = hostname;
            }

            LOG.debug("########hostname is: " + hostname);

        }
    }

    class TailThread extends Thread {

        TailThread() {
            super("TailThread-" + thdCount++);
        }

        @Override
        public void run() {
            int oldSize = 0;
            boolean flags = false;
            if (logicalNode != null) {
                LOG.info("start logicalnode:" + logicalNode);
            } else {
                LOG.warn("logicalnode is null");
            }

            LOG.debug("start tailthread");
            try {
                // initialize based on initial settings.
                for (Cursor c : cursors) {
                    c.initCursorPos();
                }

                while (!done) {
                    synchronized (newCursors) {
                        for (Cursor c : newCursors) {
                            LOG.info("####new cursor: " + c.file);
                            c.initCursorPos();
                        }
                        cursors.addAll(newCursors);
                        newCursors.clear();
                    }

                    synchronized (rmCursors) {
                        cursors.removeAll(rmCursors);
                        for (Cursor c : rmCursors) {
                            if (!c.isClosed()) {
                                //oldSize--;
                                isRotate = false;
                                c.flush();
                                c.persistOffset("deleteMap", true);
                                c.close();
                                offSet.remove(c.file.getName());
                                LOG.info("####rm cursor: {}", c.file);
                            } else {
                                LOG.info("####Cursor: {} is already removed!!", c.file);
                            }
                        }
                        rmCursors.clear();
                    }

                    int madeProgress = 0;
                    if (isRotate && cursors.size() <= oldSize) {
                        LOG.debug("file rotate cursors.size() <= {} sleep", oldSize);
                        Clock.sleep(sleepTime);
                        continue;
                    }

                    for (Cursor c : cursors) {
                        LOG.debug("Progress loop: " + c.file);
                        String fname = c.file.getName();
                        Matcher matcher = rotatePattern.matcher(fname);
                        if (rotatePattern != null && isRotate) {
                            LOG.debug("matche fname{}, match result: {}", fname, matcher.matches());
                            if (matcher.matches()) {
                                if (offSet.contains(fname)) {
                                    LOG.warn("offmap has key:" + fname + " skip tailbody");
                                    continue;
                                } else {
                                    offSet.add(fname);
                                }
                                c.setLineNumber(getHistorylineNumber());
                                c.setLastChannelPos(getHistoryChannelPos());
                                c.setLastChannelSize(getHistoryChannelSize());
                                c.initCursorPos();
                                LOG.info(String.format("read rotate file:%s  line:%s channelpos:%s channelsize:%s filemod:%s",
                                        fname, getHistorylineNumber(), getHistoryChannelPos(), getHistoryChannelSize(),
                                        c.getLastFileMod()));
                                isRotate = false;

                                c.tailBody();
                                c.flush();
                                c.persistOffset("setRotateEnd", true);
                                madeProgress = 1;
                                continue;
                            } else {
                                continue;
                            }
                        } else if (rotatePattern != null && !isRotate && matcher.matches()
                                && c.file.exists() && !offSet.contains(fname) && c.file.length() == 0) {
                            LOG.info("222222 cursors:{}, fileLength:{}, oldSize: " + oldSize + " rotatePattern: " + rotatePattern +
                                    "isRotate: " + isRotate, cursors, c.file.length());
                            isRotate = true;
                            flags = true;
                            continue;
                        }

                        if (flags && !matcher.matches()) {
                            c.init();
                            LOG.warn("reSet {}'s cursor!", fname);
                            flags = false;
                        }

                        int rt = c.tailBody();

			if(rt>0){
				isPersist=true;
			}

                        if (rt == 1) {
                            madeProgress = 1;
                        }

                        if (rt == 2) {
                            oldSize = cursors.size();
                            LOG.info("111111 cursors:{}, fileLength:{}, oldSize: " + oldSize + " rotatePattern: " + rotatePattern +
                                    "isRotate: " + isRotate, cursors, c.file.length());
                            LOG.info("file rotate:" + c.file);
                            isRotate = true;
                            madeProgress = 2;
                            setHistorylineNumber(c.getLineNumber());
                            setHistoryChannelPos(c.getLastChannelPos());
                            setHistoryChannelSize(c.getLastChannelSize());
                            setHistoryFileMod(c.getLastFileMod());

                            LOG.info(String.format("file:%s line:%s channelpos:%s channelsize:%s filemod:%s",
                                    c.file, c.getLineNumber(), c.getLastChannelPos(), c.getLastChannelSize(),
                                    c.getLastFileMod()));
                            c.init();
                            c.persistOffset("setRotateStart", true);
                            break;
                        }


                        if (isRemoveCursor && c.readComplete == Cursor.ReadComplete.YES
                                && Clock.unixTime() - c.getLastFileMod() >= maxNoChangeTime) {
                            LOG.info("File: {} isn't change in {} ms .....", c.file, maxNoChangeTime);
                            c.persistOffset("updateMap", true);
                            removeCursor(c);
                        }
                    }


                    if (madeProgress == 0) {
                        Clock.sleep(sleepTime);
			if(isPersist){
                    		for (Cursor c : cursors) {
					c.persistOffset("updateMap",true);
				}

				isPersist=false;
			}
                    }
                }
                LOG.warn("Tail got done flag");
            } catch (InterruptedException e) {
                for (Cursor c : cursors) {
                    LOG.info("######Tail_thread exception " + c.file);
                    c.close();
                }
                LOG.error("Tail thread interrupted: " + e.getMessage(), e);
            } catch (Exception err) {
                LOG.warn("tailsource err:", err);
            } finally {
                LOG.info("TailThread has exited");
            }

        }
    }

    public void setRotateRegex(String regx) {
        rotateRegex = regx;
    }

    public void setMode(String m) {
        runMode = m;
    }

    public void setHistoryFileMod(long historyFileMod) {
        this.historyFileMod = historyFileMod;
    }

    public long getHistoryChannelPos() {
        return historyChannelPos;
    }

    public void setHistoryChannelPos(long historyChannelPos) {
        this.historyChannelPos = historyChannelPos;
    }

    public long getHistoryChannelSize() {
        return historyChannelSize;
    }

    public void setHistoryChannelSize(long historyChannelSize) {
        this.historyChannelSize = historyChannelSize;
    }

    public long getHistorylineNumber() {
        return historylineNumber;
    }

    public void setHistorylineNumber(long historylineNumber) {
        this.historylineNumber = historylineNumber;
    }

    public void setLogicalNode(String name) {
        logicalNode = name;
    }

    public void recover() {
        Map<String, List<Long>> offsetMap = PersistOffset.getOffsetMap(persistAbsPath);
        if (offsetMap.size() == 0) {
            return;
        }
	    DateFormat dateFormat = new SimpleDateFormat("HH");
	    String persistHour;
        String nowHour=dateFormat.format(System.currentTimeMillis());

        for (Cursor c : cursors) {
            String fname = c.file.getName();
            List<Long> list = offsetMap.get(fname);
            if (list == null) {
                continue;
            }
            long offset = list.get(0);
            long lineNumber = list.get(1);
            persistHour= dateFormat.format(new File(persistAbsPath).lastModified()); 


            if (offset > c.file.length()) {
                LOG.warn("maybe file rotate,offset>file.length()");
                continue;
            }

            if (!persistHour.equals(nowHour)) {
                LOG.warn(String.format("maybe file rotate,persistHour change from %s to %s",persistHour,nowHour));
                continue;
            }

            LOG.info(String.format("recover file:%s offset:%s lineNumber:%s", fname, offset, lineNumber));
            c.setLastChannelPos(offset);
            c.setLastChannelSize(offset);
            c.setLineNumber(lineNumber);
        }


    }


    synchronized public void addCursor(Cursor cursor, boolean flags) {
        Preconditions.checkArgument(cursor != null);

        if (thd == null) {
            cursors.add(cursor);
            LOG.info("Unstarted Tail has added cursor: " + cursor.file);
        } else {
            while (!Strings.isNullOrEmpty(rotateRegex) && !isRotate && !flags) {
                try {
                    Clock.sleep(sleepTime);
                    LOG.debug("Sleep while isRotate is false..");
                } catch (InterruptedException e) {
                    LOG.warn("Sleep InterruptedException: {}", e.getMessage());
                }
            }

            synchronized (newCursors) {
                newCursors.add(cursor);
            }
            LOG.info("Tail added new cursor to new cursor list: " + cursor.file);
        }

    }

    synchronized public void removeCursor(Cursor cursor) {
        Preconditions.checkArgument(cursor != null);

        LOG.info("Tail added rm cursor to rmCursors: " + cursor.file);

        if (thd == null) {
            cursors.remove(cursor);
        } else {

            synchronized (rmCursors) {
                rmCursors.add(cursor);
            }
        }

    }
}
