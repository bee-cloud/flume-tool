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

public enum CursorType {
    DEFAULT("default"),
    DELIM("delim"),
    BLOCK("block"),
    LINE("line");

    private final String cursorType;
    private static final Logger LOG = LoggerFactory.getLogger(CursorType.class);

    private CursorType(String cursorType) {
        this.cursorType = cursorType;
    }

    public static CursorType buildCursorType(String cursorType) {
        CursorType[] values = CursorType.values();
        for (CursorType ct : values) {
            if (ct.getCursorType().equals(cursorType.toLowerCase())) {
                LOG.info("Build-in cursor type : {}", cursorType);
                return ct;
            }
        }

        LOG.info("{} isn't a build-in cursor type, use default cursor type!", cursorType);
        return CursorType.DEFAULT;

    }

    public String getCursorType() {
        return cursorType;
    }

}
