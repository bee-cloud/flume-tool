package com.fxiaoke.dataplatform.flume.ng.util;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * This class watches a specified directory for deletions, creations and
 * "age off" events. It spawns a thread that periodically checks the directory.
 */
public class DirWatcher {
    static final Logger LOG = LoggerFactory.getLogger(DirWatcher.class);

    final private List<DirChangeHandler> list = Collections
            .synchronizedList(new ArrayList<DirChangeHandler>());
    public boolean initIsRotate = false;
    private File dir;
    private volatile boolean done = false;
    private Set<File> previous = new HashSet<File>();
    private long sleep_ms;
    private Periodic thread;
    private FileFilter filter;
    private boolean isDirExist;
    private boolean flags = false;

    /**
     * checkperiod is the amount of time in milliseconds between directory polls.
     */
    public DirWatcher(File dir, FileFilter filter, long checkPeriod) {
        Preconditions.checkNotNull(dir);
        Preconditions.checkArgument(dir.isDirectory(), dir + " is not a directory");
        isDirExist = true;
        this.thread = null;
        this.dir = dir;
        this.sleep_ms = checkPeriod;
        this.filter = filter;
    }

    /**
     * Start the directory watching. This implementation uses a thread to poll
     * periodically. If called multiple times, it will only start a single thread.
     * This is not threadsafe
     */
    public void start() {
        if (thread != null) {
            LOG.warn("Dir watcher already started!");
            return;
        }
        this.thread = new Periodic();
        this.thread.start();
        LOG.info("Started dir watcher thread");
    }

    /**
     * This attempts to stop the dir watching. With this thread-based
     * implementation it, blocks until the thread is done. This is not thread
     * safe.
     */
    public void stop() {
        if (thread == null) {
            LOG.info("DirWatcher stop watching dir:" + dir);

            LOG.warn("DirWatcher already stopped");
            return;
        }

        done = true;

        try {
            thread.join();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        // waiting for thread to complete.
        LOG.info("Stopped dir watcher thread");
        thread = null;
    }

    /**
     * This the core check method that updates information from the previous poll
     * and fires events based on changes.
     */
    public boolean check() {
        File[] files = dir.listFiles();
        if (files == null) {
            LOG.info("dir " + dir.getAbsolutePath() + " does not exist!");
            isDirExist = false;

            Set<File> removedFiles = new HashSet<File>(previous);
            for (File f : removedFiles) {
                if (f.isDirectory() || filter.isSelected(f)) {
                    fireDeletedFile(f);
                }
            }

            previous.clear();

            return false;
        }

        if (!isDirExist) {
            flags = true;
            isDirExist = true;
        }

        Set<File> newfiles = new HashSet<File>(Arrays.asList(files));
        Set<File> addedFiles = new HashSet<File>(newfiles);
        addedFiles.removeAll(previous);
        List<File> addFiles = new ArrayList<File>();

        for (File f : addedFiles) {
            if (f.isDirectory() || filter.isSelected(f)) {
                addFiles.add(f);
            } else {
                newfiles.remove(f);
            }
        }

        Collections.sort(addFiles);

        for (File f : addFiles) {
            fireCreatedFile(f);
        }

        Set<File> removedFiles = new HashSet<File>(previous);
        removedFiles.removeAll(newfiles);
        for (File f : removedFiles) {
            fireDeletedFile(f);
        }

        previous = newfiles;

        return true;
    }

    /**
     * Add a handler callback object.
     */
    public void addHandler(DirChangeHandler dch) {
        list.add(dch);
    }

    /**
     * Fire the 'created' callback on all handlers.
     */
    public void fireCreatedFile(File f) {

        // make copy so it is thread safe
        DirChangeHandler[] hs = list.toArray(new DirChangeHandler[0]);
        for (DirChangeHandler h : hs) {
            h.fileCreated(f);
            LOG.info("fireCreatedFile:" + f.getAbsolutePath());
        }

    }

    /**
     * Fire the 'deleted' callback on all handlers.
     */
    public void fireDeletedFile(File f) {
        // make copy so it is thread safe
        DirChangeHandler[] hs = list.toArray(new DirChangeHandler[0]);
        for (DirChangeHandler h : hs) {
            h.fileDeleted(f);
            LOG.info("fireDeletedFile:" + f.getAbsolutePath());
        }
    }

    public boolean isFlags() {
        return flags;
    }

    /**
     * This thread periodically checks a directory for updates
     */
    class Periodic extends Thread {
        boolean isExit = false;

        Periodic() {
            super("DirWatcher");
        }

        public void run() {
            try {

                LOG.info("DirWatcher start watching dir:" + dir);

                while (!done) {
                    try {
                        isExit = check();
                        Clock.sleep(sleep_ms);
                    } catch (NumberFormatException nfe) {
                        LOG.warn("wtf ", nfe);
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("Maybe dir: " + dir + " is not exists");
                LOG.warn("InterruptedException:", e);
            }

            LOG.debug("exit dirwatchthread");
        }
    }
}
