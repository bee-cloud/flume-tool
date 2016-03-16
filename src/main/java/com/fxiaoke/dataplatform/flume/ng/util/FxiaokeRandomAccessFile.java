package com.fxiaoke.dataplatform.flume.ng.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FxiaokeRandomAccessFile extends RandomAccessFile {
    private static final Logger LOG = LoggerFactory.getLogger(FxiaokeRandomAccessFile.class);

    public FxiaokeRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    public String ReadLine() throws IOException {
        //input.setLength(0);
        StringBuffer input = new StringBuffer();

        int c = -1;
        boolean eol = false;
        long before = getFilePointer();
        while (!eol) {
            switch (c = read()) {
                case -1:
                    if (input.length() > 0) {
                        LOG.debug("WARN: No data to read, and the current data in buffer is : \n\n{}", input.toString());
                        seek(before);
                        LOG.debug("WARN: before pointer: {}, current pointer: {}", before, getFilePointer());
                        return null;
                    }
                    eol = true;
                    break;
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

}
