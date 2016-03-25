package com.fxiaoke.dataplatform.flume.ng.util;

import java.io.File;

/**
 * This is a filter that has a simple predicate that will select or reject a
 * file based on instance specified criteria
 */
public interface FileFilter {
    /**
     * True if file means specified criteria
     */
    boolean isSelected(File f);

}
