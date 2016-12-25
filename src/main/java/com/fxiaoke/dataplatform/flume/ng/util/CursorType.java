package com.fxiaoke.dataplatform.flume.ng.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CursorType {
    DEFAULT("default"),
    DELIM("delim"),
    BLOCK("block"),
    LINE("line");

    private static final Logger LOG = LoggerFactory.getLogger(CursorType.class);
    private final String cursorType;

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
