/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fxiaoke.dataplatform.flume.ng.util;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class FileRotateJnotifyWatch extends Thread {
    static final Logger LOG = LoggerFactory.getLogger(FileJnotifyWatch.class);
    static final String regex = "(?:\\.|_)?\\d{4}-\\d{2}-\\d{2}.*";
    static final String separator = System.getProperty("file.separator");
    public static Set<String> fileSet = new ConcurrentHashSet<String>();
    private String path;
    private String confPath;

    public FileRotateJnotifyWatch(String path, String confPath) {
        this.path = path;
        this.confPath = confPath;
    }

    public static List<String> setWatchDir(String watchConf) throws IOException {
        ArrayList<String> watchDirs = new ArrayList<String>();


        File fileName = new File(watchConf);
        if (!fileName.exists()) {
            LOG.warn("watchConf not exists :" + watchConf);
            return watchDirs;
        }

        FileInputStream file = new FileInputStream(fileName);
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(file));
        String watchField = reader.readLine();
        File watchDir = null;
        while (watchField != null) {
            watchField = watchField.trim();

            if (!watchField.startsWith("#")) {
                watchDir = new File(watchField);
                if (!watchDir.exists()) {
                    LOG.warn("watchDir not exists :" + watchField);
                } else if (!watchDir.isDirectory()) {
                    LOG.warn("watchDir is not a directory :" + watchField);
                } else {
                    watchDirs.add(watchField);
                }
            }

            watchField = reader.readLine();
        }

        return watchDirs;

    }

    public static void init(String path) throws Exception {
        /**
         * jnotify动态库 - 32位
         */
        final String NATIVE_LIBRARIES_32BIT = "/lib/native_libraries/32bits/";
        /**
         * jnotify动态库 - 64位
         */
        final String NATIVE_LIBRARIES_64BIT = "/lib/native_libraries/64bits/";
        Properties sysProps = System.getProperties();
        String osArch = (String) sysProps.get("os.arch");
        String userDir = (String) sysProps.getProperty("user.dir");

        String fileAdd;

        // 判断系统是32bit还是64bit，决定调用对应的dll文件
        String jnotifyDir = NATIVE_LIBRARIES_64BIT;
        if (!osArch.contains("64")) {
            jnotifyDir = NATIVE_LIBRARIES_32BIT;
        }
        // 获取目录路径
        String pathToAdd = userDir + jnotifyDir;
        boolean isAdded = false;
        final Field usrPathsField = ClassLoader.class
                .getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        final String[] paths = (String[]) usrPathsField.get(null);
        for (String p : paths) {
            if (p.equals(pathToAdd)) {
                isAdded = true;
                break;
            }
        }
        if (!isAdded) {
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[newPaths.length - 1] = pathToAdd;
            usrPathsField.set(null, newPaths);
        }
        usrPathsField.setAccessible(false);


        File folder = new File(path);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    init(file.getAbsolutePath());
                } else {
                    if (file.getAbsolutePath().matches(".*\\d{4}-\\d{2}-\\d{2}-\\d{2}.*")) {
                        fileAdd = file.getParent() + separator + file.getName().replaceAll(regex, "");
                        fileSet.add(fileAdd);
                    }
                }
            }
        }
    }

    public static void watchServiced(String path, final String confPath) {
        FileRotateJnotifyWatch thread = new FileRotateJnotifyWatch(path, confPath);
        thread.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            watchServiced(args[0], args[1]);
        }
    }

    public void run() {
        try {
            List<String> watchDirs = setWatchDir(path);

            if (watchDirs.size() == 0) {
                LOG.warn("#############watchDir is null################");
                return;
            }
            for (String watchDir : watchDirs) {
                init(watchDir);
            }


            LOG.info("allFiles:");
            for (String file : fileSet) {
                LOG.info(file);
            }
            LOG.info("init configure file");
            //WriteConf.writeFlumeConf(fileSet, confPath);
            WriteFlumeConf.writeConf(fileSet, confPath);
            // 监听事件
            for (String watchDir : watchDirs) {
                JNotify.addWatch(watchDir, JNotify.FILE_ANY, true, new JNotifyListener() {
                    @Override
                    public void fileRenamed(int wd, String rootPath, String oldName,
                                            String newName) {

                        String oldPath = rootPath + separator + oldName;
                        String newPath = rootPath + separator + newName;
                        File folder = new File(newPath);
                        if (folder.isDirectory()) {
                            LOG.info("rename oldName = " + oldPath + ", newName = "
                                    + newPath);

                            for (String file : fileSet) {
                                if (file.startsWith(oldPath)) {
                                    fileSet.remove(file);
                                    String newFile = file.replace(oldPath, newPath);
                                    fileSet.add(newFile);
                                    LOG.info("dir change:" + newFile);
                                    LOG.info("allFiles:");
                                    for (String file2 : fileSet) {
                                        LOG.info(file2);
                                    }
                                    LOG.info("recreate configure file");
                                    try {
                                        //WriteConf.writeFlumeConf(fileSet, confPath);
                                        WriteFlumeConf.writeConf(fileSet, confPath);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void fileModified(int wd, String rootPath, String fileName) {
                    }

                    @Override
                    public void fileDeleted(int wd, String rootPath, String fileName) {
                    }


                    @Override
                    public void fileCreated(int wd, String rootPath, String fileName) {
                        LOG.info("new " + fileName);
                        String fullPath = rootPath + separator + fileName;
                        LOG.info(fullPath);
                        File folder = new File(fullPath);
                        if (folder.isFile() && fullPath.matches(".*\\d{4}-\\d{2}-\\d{2}-\\d{2}.*")) {
                            fullPath = rootPath + separator + fileName.replaceAll(regex, "");
                            if (!fileSet.contains(fullPath)) {
                                fileSet.add(fullPath);
                                LOG.info("addFile：" + fullPath);
                                LOG.info("allFiles:");
                                for (String file : fileSet) {
                                    LOG.info(file);
                                }
                                LOG.info("recreate configure file");
                                try {
                                    //WriteConf.writeFlumeConf(fileSet, confPath);
                                    WriteFlumeConf.writeConf(fileSet, confPath);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
            /*
            while (true) {

            }
            */
        } catch (Exception e1) {
            e1.printStackTrace();
        }


    }
}

