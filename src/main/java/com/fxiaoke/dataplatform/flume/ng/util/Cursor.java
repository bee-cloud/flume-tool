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

import com.fxiaoke.dataplatform.flume.ng.source.TailSource;
import com.google.common.collect.Maps;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.instrumentation.SourceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * 以tailf方式读取文件内容，可检测文件切分.
 */
public class Cursor {
    private static final Logger LOG = LoggerFactory.getLogger(Cursor.class);
    public final File file;
    ByteBuffer buf;
    FxiaokeRandomAccessFile raf = null;
    FileChannel in = null;
    protected ChannelProcessor channelProcessor;
    private long lastFileMod;
    protected long lastChannelPos;
    private long lastChannelSize;
    private int readFailures;
    protected long allBufNum = 0;
    private long lastPersist = 0;
    private final String rotateRegex;
    protected long lineNumber = 0;
    private String persistAbsPath;
    protected int maxBackoffSleep = 1000;
    private long checkPersistTime;
    private boolean isClosed = false;
    private boolean unChanged = false;
    private SourceCounter sourceCounter;
    protected boolean isRotateEnable;
    protected long rotateInterval;
    protected long lastStopTime = 0;
    public ReadComplete readComplete = ReadComplete.NO;
    private CursorType cursorType;
    private String persistCopyModel;

    public Cursor(ChannelProcessor channelProcessor, File f, String rotateRegex,
                  String persistAbsPath, int bufferSize, long checkPersistTime, SourceCounter sourceCounter,
                  boolean isRotateEnable, long rotateInterval, CursorType cursorType, String persistCopyModel) {
        this(channelProcessor, f, 0, 0, 0, rotateRegex, persistAbsPath,
                bufferSize, checkPersistTime, sourceCounter, isRotateEnable, rotateInterval, cursorType, persistCopyModel);
    }

    public Cursor(ChannelProcessor channelProcessor, File f, long lastReadOffset,
                  long lastFileLen, long lastMod, String rotateRegex,
                  String persistAbsPath, int bufferSize, long checkPersistTime,
                  SourceCounter sourceCounter, boolean isRotateEnable, long rotateInterval,
                  CursorType cursorType, String persistCopyModel) {

        this.channelProcessor = channelProcessor;
        this.file = f;
        this.lastChannelPos = lastReadOffset;
        this.lastChannelSize = lastFileLen;
        this.lastFileMod = lastMod;
        this.readFailures = 0;

        this.rotateRegex = rotateRegex;
        this.persistAbsPath = persistAbsPath;
        this.cursorType = cursorType;
        if (cursorType != CursorType.LINE) {
            LOG.debug("Buffer size is {} byte..", bufferSize);
            buf = ByteBuffer.allocateDirect(bufferSize);
        }

        this.checkPersistTime = checkPersistTime;
        this.sourceCounter = sourceCounter;
        this.isRotateEnable = isRotateEnable;
        this.rotateInterval = rotateInterval;
        this.persistCopyModel = persistCopyModel;
    }

    public void persistOffset(String tag, boolean isForce) {
        LOG.debug(String.format("persist offset tag:%s file:%s persistAbsPath:%s pos:%s " +
                        "lastpersist:%s line:%s", tag, file.getName(), persistAbsPath,
                lastChannelPos, lastPersist, lineNumber));
        long isPersist = PersistOffset.persistData(persistAbsPath, tag, file, lastChannelPos,
                lineNumber, lastPersist, isForce, checkPersistTime, persistCopyModel);
        if (isPersist > 0) {
            lastPersist = isPersist;
        }
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public long getLastFileMod() {
        return lastFileMod;
    }

    public long getLastChannelPos() {
        return lastChannelPos;
    }

    public long getLastChannelSize() {
        return lastChannelSize;
    }

    public void setLastChannelPos(long pos) {
        lastChannelPos = pos;
    }

    public void setLastChannelSize(long pos) {
        lastChannelSize = pos;
    }

    public void init() throws InterruptedException {
        try {
            LOG.debug("new file init" + file);
            closeRAF();
            raf = new FxiaokeRandomAccessFile(file, "r");
            lastChannelPos = 0;
            raf.seek(lastChannelPos);
            in = raf.getChannel();
            lineNumber = 0;
        } catch (FileNotFoundException e) {
            resetRAF();
        } catch (IOException e) {
            resetRAF();
        }
    }

    public void initCursorPos() throws InterruptedException {
        closeRAF();
        try {
            LOG.debug("initCursorPos " + file);
            raf = new FxiaokeRandomAccessFile(file, "r");
            raf.seek(lastChannelPos);
            in = raf.getChannel();
        } catch (FileNotFoundException e) {
            resetRAF();
        } catch (IOException e) {
            resetRAF();
        }
    }

    /**
     * Flush any buffering the cursor has done. If the buffer does not end with
     * '\n', the remainder will get turned into a new event.
     * <p/>
     * This assumes that any remainders in buf are a single event -- this
     * corresponds to the last lines of files that do not end with '\n'
     */
    public void flush() throws InterruptedException {
        LOG.debug("raf:" + raf);
        if (cursorType != CursorType.LINE) {
            closeRAF();
            buf.flip(); // buffer consume mode
            int remaining = buf.remaining();
            if (remaining > 0) {
                byte[] body = new byte[remaining];
                buf.get(body, 0, remaining); // read all data
                processEvent(eventBuild(body, false));
                lastChannelPos += body.length + 1;  //add "\n"
                persistOffset("updateMap", true);
            }
            in = null;
            buf.clear();
        }
    }

    /**
     * 关闭cursor，释放
     */
    public void close() {
        closeRAF();
        in = null;
        if (buf != null) {
            buf.clear();
            buf = null;
        }
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    protected void closeRAF() {
        if (raf != null) {
            try {
                raf.close(); // release handles
            } catch (IOException e) {
                LOG.error("problem closing file " + e.getMessage(), e);
            }
        }
    }

    void resetRAF() throws InterruptedException {
        LOG.debug("reseting cursor");
        if (cursorType != CursorType.LINE) {
            flush();
        }
        lastChannelPos = 0;
        lastFileMod = 0;
        readFailures = 0;
        lineNumber = 0;
    }

    /**
     * 检查文件内容是否有更新
     */
    boolean checkForUpdates() throws IOException {
        LOG.debug("tail " + file + " : recheck");
        if (file.isDirectory()) { // exists but not a file
            IOException ioe = new IOException("Tail expects a file '" + file
                    + "', but it is a dir!");
            LOG.error(ioe.getMessage());
            throw ioe;
        }

        if (!file.exists()) {
            LOG.debug("Tail '" + file + "': nothing to do, waiting for a file");
            return false; // do nothing
        }

        if (!file.canRead()) {
            throw new IOException("Permission denied on " + file);
        }

        try {
            closeRAF();
            raf = new FxiaokeRandomAccessFile(file, "r");
            lastFileMod = file.lastModified();
            in = raf.getChannel();

            LOG.debug("###lastChannelPos=" + lastChannelPos);

            if (lastChannelPos > 0) {
                raf.seek(lastChannelPos);
            } else {
                lastChannelPos = 0;
            }

            lastChannelSize = in.size();

            LOG.debug("Tail '" + file + "': opened last mod=" + lastFileMod
                    + " lastChannelPos=" + lastChannelPos + " lastChannelSize="
                    + lastChannelSize);
            return true;
        } catch (FileNotFoundException fnfe) {
            LOG.debug("Tail '" + file+ "': a file existed then disappeared, odd but continue");
            return false;
        }
    }

    boolean extractLines(ByteBuffer buf) throws IOException, InterruptedException {
        boolean madeProgress = false;
        int start = buf.position();
        buf.mark();

        while (buf.hasRemaining()) {
            byte b = buf.get();
            if (b == '\n') {
                int end = buf.position();
                int sz = end - start;
                byte[] body = new byte[sz - 1];
                buf.reset();
                buf.get(body, 0, sz - 1);
                buf.get();
                buf.mark();
                start = buf.position();
                processEvent(eventBuild(body, false));
                lastChannelPos += body.length + 1;
                persistOffset("updateMap", false);
                madeProgress = true;
            }
        }

        if (buf.limit() == buf.capacity() && !madeProgress) {
            allBufNum++;

            handleBufFull();
            madeProgress = true;
        }

        buf.reset();
        buf.compact();
        return madeProgress;
    }

    void handleBufFull() throws IOException, InterruptedException {
        LOG.debug("###enter cursor allBuf");
        buf.position(0);
        byte[] body = new byte[buf.limit()];
        buf.get(body);

        LOG.debug("handle buf full body start:");
        LOG.debug(new String(body));
        LOG.debug("handle buf full body end:");
        processEvent(eventBuild(body, true));
        lastChannelPos += body.length;
        persistOffset("updateMap", true);
    }

    public int tailBody() throws InterruptedException {
        try {
            if (in == null) {
                LOG.debug("tail " + file + " : cur file is null");
                return checkForUpdates() ? 1 : 0;
            }

            boolean madeProgress = readAllFromChannel();

            if (madeProgress) {
                lastChannelSize = lastChannelPos;
                lastFileMod = file.lastModified();
                LOG.debug("tail " + file + " : new data found");
                unChanged = false;
            }

            long fmod = file.lastModified();
            long flen = file.length(); // length of filename

            /**
             * 判断文件是否切分
             */
            if (file.exists() && flen < lastChannelPos && fmod >= lastFileMod) {
                LOG.debug(String.format("flen:%s lastChannelPos:%s fmod:%s lastFileMod:%s fileName:%s", flen, lastChannelPos, fmod, lastFileMod, file.getName()));

                persistOffset("updateMap", true);
                if (!rotateRegex.equals("") && !file.getName().matches(rotateRegex)) {
                    if (cursorType != CursorType.LINE) {
                        buf.clear();
                    }
                    LOG.info("file rotate=======" + file.getName());
                    return 2;
                } else {
                    flush();
                    init();
                    return 1;
                }
            }

            if (madeProgress) {
                return 1;
            }

            if (flen == lastChannelSize && fmod == lastFileMod) {
              //  LOG.debug("tail " + file + " : no change");
                if (!unChanged) {
                    persistOffset("updateMap", false);
                    unChanged = true;
                }

                return 0;
            }

            return 0;

        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            resetRAF();
            in = null;
            readFailures++;

            if (readFailures > 3) {
                LOG.warn("Encountered " + readFailures + " failures on "
                        + file.getAbsolutePath() + " - sleeping");
                return 0;
            }
        }
        return 1;
    }

    protected boolean readAllFromChannel() throws IOException, InterruptedException {
        boolean madeProgress = false;

        int rd;
        lastStopTime = Clock.unixTime();
        while ((rd = in.read(buf)) > 0) {
            madeProgress = true;

            int lastRd = 0;
            boolean progress = false;
            do {

                if (lastRd == -1 && rd == -1) {
                    return true;
                }

                buf.flip();


                extractLines(buf);

                lastRd = rd;
            } while (progress);

            LOG.debug("###buf remaining size=" + buf.remaining());
            if (buf.remaining() == 0) {
                LOG.debug("###buf remaining clear");
                buf.position(0);
            }

            if (isRotateEnable && Clock.unixTime() - lastStopTime >= rotateInterval) {
                LOG.info("Starting rotate {} .....", file);
                return madeProgress;
            }
        }

        LOG.debug("tail " + file + ": last read position " + lastChannelPos
                + ", madeProgress: " + madeProgress);
        readComplete = ReadComplete.YES;
        lastFileMod = file.lastModified();
        return madeProgress;
    }

    protected void processEvent(Event event) throws InterruptedException {
        if (event.getBody().length == 0) {
            LOG.warn("body length: 0");
            return;
        }

        boolean done = false;
        sourceCounter.incrementAppendReceivedCount();
        sourceCounter.incrementEventReceivedCount();
        while (!done) {
            try {
                channelProcessor.processEvent(event);
                done = true;
            } catch (Exception e) {
                LOG.error("Unhandled exception, logging and sleeping for " + maxBackoffSleep + "ms", e);
                try {
                    Thread.sleep(maxBackoffSleep);
                } catch (InterruptedException ex) {
                    throw new InterruptedException(ex.getMessage());
                }
            }
        }
        //sourceCounter.addNetworkTraffic(event.getBody().length);
        sourceCounter.incrementAppendAcceptedCount();
        sourceCounter.incrementEventAcceptedCount();
    }

    protected Event eventBuild(String body, boolean isFullBuffer) {
        return eventBuild(body.getBytes(), isFullBuffer);
    }

    protected Event eventBuild(byte[] body, boolean isFullBuffer) {
        Map<String, String> headers = Maps.newHashMap();

        headers.put(TailSource.A_TAILSRCFILE, file.getName());
        headers.put("abspath", file.getAbsolutePath());
        if (isFullBuffer) {
            headers.put("buffull", String.valueOf(allBufNum));
        } else {
            if (body.length > 0) {
                lineNumber++;
            }
            headers.put("lineNumber", Long.toString(lineNumber));
        }

        headers.put("hostname", TailSource.hostname);
        return EventBuilder.withBody(body, headers);
    }

    public enum ReadComplete {
        YES, NO
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "file=" + file.getName() +
                '}';
    }

}
