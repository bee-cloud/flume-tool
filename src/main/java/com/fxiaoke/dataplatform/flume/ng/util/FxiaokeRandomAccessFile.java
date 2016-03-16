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
