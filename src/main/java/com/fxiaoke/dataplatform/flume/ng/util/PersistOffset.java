package com.fxiaoke.dataplatform.flume.ng.util;

import com.fxiaoke.dataplatform.flume.ng.source.TailSource;
import com.fxiaoke.dataplatform.flume.ng.util.proto.Offset.dataSet;
import com.fxiaoke.dataplatform.flume.ng.util.proto.Offset.offsetKv;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 使用protobuffer持久化文件偏移量
 */
public class PersistOffset {
    private static final Logger LOG = LoggerFactory.getLogger(PersistOffset.class);
    private static final ReentrantReadWriteLock checkpointLock =
            new ReentrantReadWriteLock(true);

    private static final ReentrantReadWriteLock.ReadLock checkpointReadLock = checkpointLock.readLock();
    private static final ReentrantReadWriteLock.WriteLock checkpointWriterLock = checkpointLock.writeLock();
    private static String homePath = System.getProperty("user.home").replace('\\', '/');

    public static String getOffsetFile(String persistDir, String logicalNode) {
        String flumeLogDir = "";
        if (!Strings.isNullOrEmpty(persistDir)) {
            flumeLogDir = persistDir;
        } else {
            flumeLogDir = getFlumeLogDir();
            if (Strings.isNullOrEmpty(flumeLogDir)) {
                flumeLogDir = homePath + "/.flume/persistDir/";
            }
        }

        if (!flumeLogDir.endsWith(File.separator)) {
            flumeLogDir += File.separator;
        }

        LOG.debug("flume log dir:" + flumeLogDir);
        String persistName = flumeLogDir + logicalNode;
        new File(flumeLogDir).mkdirs();
        LOG.debug("persistName:" + persistName);
        return persistName;

    }

    private static String getFlumeLogDir() {
        String flumeLogDir = "";
        flumeLogDir = System.getenv("FLUME_LOG_DIR");
        if (null == flumeLogDir) {
            LOG.debug("$FLUME_LOG_DIR unset");
        }
        return flumeLogDir;
    }

    public static Map<String, List<Long>> getOffsetMap(String offsetFile) {
        Map<String, List<Long>> offsetMap = new TreeMap<String, List<Long>>();
        try {
            File file = new File(offsetFile);
            if (!file.exists()) {
                LOG.warn("persist file not exists:" + file.getAbsolutePath());
                return offsetMap;
            }

            if (file.length() == 0) {
                file = new File(file + ".bak");
                if (!file.exists()) {
                    LOG.warn("persist bak file not exists:" + file.getAbsolutePath());
                    return offsetMap;
                }
            }

            while (!tryLockShared()) {
                LOG.debug("Try to get the share lock!!");
            }

            LOG.debug("Get the share lock!!");
            dataSet r;
            FileInputStream input = null;
            try {
                input = new FileInputStream(offsetFile);
                r = dataSet.parseFrom(input);
            } finally {
                unlockShared();
                if (input != null) {
                    input.close();
                }
            }

            for (offsetKv kv : r.getKmapList()) {
                String fileName = kv.getFilename();
                long offset = kv.getOffset();
                long lineNum = kv.getLineNumber();
                List<Long> list = new ArrayList<Long>(2);
                list.add(0, offset);
                list.add(1, lineNum);
                offsetMap.put(fileName, list);
            }

            long isRotate = r.getIsRotate() ? 1 : 0;
            List<Long> list = new ArrayList<Long>(2);
            list.add(0, isRotate);
            list.add(1, isRotate);
            offsetMap.put("isRotate", list);

        } catch (Exception err) {
            LOG.warn("getoffsetmap  exception", err);
        }
        return offsetMap;
    }

    public static long persistData(String persistAbsPath, String tag, File file,
                                   long offSet, long lineNumber, long lastPersist,
                                   boolean isForce, long checkPersistTime, String persistCopyModel) {
        long nowPersist = Clock.unixTime();
        if (lastPersist == 0 && !isForce) {
            return nowPersist;
        }

        long diff = nowPersist - lastPersist;
        LOG.debug("checkPersistTime: {}, isForce:　{}", checkPersistTime, isForce);
        if (diff < checkPersistTime && !isForce) {
            return 0;
        }

        try {
            String filePath = file.getParent();
            Map<String, List<Long>> map = getOffsetMap(persistAbsPath);
            LOG.debug("persistData {}", map);
            dataSet.Builder d = dataSet.newBuilder();
            String fname = file.getName();

            List<Long> list = new ArrayList<Long>(2);
            list.add(0, offSet);
            list.add(1, lineNumber);

            if (tag.equals("updateMap")) {
                map.put(fname, list);
                List<Long> isRotate = map.get("isRotate");
                if (isRotate == null) {
                    LOG.error("Rotate list is null!!!!");
                    d.setIsRotate(false);
                } else {
                    if (isRotate.get(0) == 1) {
                        d.setIsRotate(true);
                    } else {
                        d.setIsRotate(false);
                    }
                }
            } else if (tag.equals("deleteMap")) {
                map.remove(fname);
                List<Long> isRotate = map.get("isRotate");
                if (isRotate == null) {
                    LOG.error("Rotate list is null!!!!");
                    d.setIsRotate(false);
                } else {
                    if (isRotate.get(0) == 1) {
                        d.setIsRotate(true);
                    } else {
                        d.setIsRotate(false);
                    }
                }
            } else if (tag.equals("setRotateStart")) {
                d.setIsRotate(true);
                List<Long> isRotateList = new ArrayList<Long>(2);
                isRotateList.add(1L);
                isRotateList.add(1L);
                map.put("isRotate", isRotateList);
                map.put(fname, list);
                LOG.debug("######setRotateStart");
            } else if (tag.equals("setRotateEnd")) {
                d.setIsRotate(false);
                List<Long> isRotateList = new ArrayList<Long>(2);
                isRotateList.add(0L);
                isRotateList.add(0L);
                map.put("isRotate", isRotateList);
                map.put(fname, list);
                LOG.debug("######setRotateEnd");
            }
            List<offsetKv> dlist = new ArrayList<offsetKv>();

            for (Map.Entry<String, List<Long>> loop : map.entrySet()) {
                String key = loop.getKey();
                File checkFile = new File(filePath + "/" + key);
                if (!checkFile.exists() && !key.equals("isRotate")) {
                    LOG.debug(String.format("remove file:%s not exists ", key));
                    continue;
                }
                dlist.add(offsetKv.newBuilder().setFilename(key).setOffset(loop.getValue().get(0)).setLineNumber(loop.getValue().get(1)).build());
            }

            d.addAllKmap(dlist);

            dataSet outputStr = d.build();

            while (!tryLockExclusive()) {
                LOG.debug("Try to get the exclusive lock!!!!");
            }
            FileOutputStream output = null;
            try {
                LOG.debug("Copy persist file.....");
                if (persistCopyModel.equals("channel")) {
                    //通过Channel复制性能比较好，但是在复制过程中文件有一段时间为空，可能会出问题
                    copyByFileChannel(new File(persistAbsPath), new File(persistAbsPath + ".bak"));
                } else if (persistCopyModel.equals("googleapi")) {
                    copyByGoogleAPI(persistAbsPath);
                } else if (persistCopyModel.equals("cp")) {
                    //通过调用Linux cp命令复制，不会出现文件为空的问题，但是这种方式耗CPU比较高
                    copyByShell(persistAbsPath);
                } else {
                    LOG.debug("Error copy model! Will use channel..");
                    copyByFileChannel(new File(persistAbsPath), new File(persistAbsPath + ".bak"));
                }
                output = new FileOutputStream(persistAbsPath);
                outputStr.writeTo(output);

            } finally {
                unlockExclusive();
                if (output != null) {
                    output.close();
                }
            }

            LOG.debug("#######" + getOffsetMap(persistAbsPath).get("isRotate"));
        } catch (Exception err) {
            LOG.error("getoffsetmap  exception:", err);
            return 0;
        }

        return nowPersist;
    }

    private static boolean tryLockExclusive() {
        try {
            return checkpointWriterLock.tryLock(TailSource.lockTimeOut,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted while waiting for log exclusive lock", ex);
            Thread.currentThread().interrupt();
        }
        return false;
}

    private static void unlockExclusive() {
        checkpointWriterLock.unlock();
    }

    static boolean tryLockShared() {
      try {
            return checkpointReadLock.tryLock(TailSource.lockTimeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted while waiting for log shared lock", ex);
            Thread.currentThread().interrupt();
        }
        return false;
  }

    static void unlockShared() {
        checkpointReadLock.unlock();
    }

    private static void copyByFileChannel(File source, File target) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            if (!source.exists()) {
                return;
            }

            fi = new FileInputStream(source);
            fo = new FileOutputStream(target);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            LOG.error("copyByFileChannel Error!! " + e.getMessage());
        } finally {
            try {
                if (fi != null) {
                    fi.close();
                }
                if (in != null) {
                    in.close();
                }
                if (fo != null) {
                    fo.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LOG.error("copyByFileChannel close Error!! " + e.getMessage());
            }
        }
    }

    private static void copyByShell(String persistAbsPath) throws IOException {
        LOG.debug("cp " + persistAbsPath + " " + persistAbsPath + ".bak");
        Process process = Runtime.getRuntime().exec("cp " + persistAbsPath + " " + persistAbsPath + ".bak");
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        BufferedReader input = new BufferedReader(ir);
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }//end
    }

    private static void copyByGoogleAPI(String persistAbsPath) throws IOException {
        File source = new File(persistAbsPath);
        if (source.exists()) {
            Files.copy(source, new File(persistAbsPath + ".bak"));
        } else {
            LOG.info("{} is not exist!! Create it....", source);
        }
    }
}
