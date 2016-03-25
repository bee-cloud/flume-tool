package com.fxiaoke.dataplatform.flume.ng.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * This returns true if the filename (not the directory part) matches the
 * specified regular expression.
 * <p/>
 * This class is not thread safe because pattern is not thread safe.
 */
public class RegexFileFilter implements FileFilter {
    Pattern p; // not thread safe

    public RegexFileFilter(String regex) {
        this.p = Pattern.compile(regex);
    }

    @Override
    public boolean isSelected(File f) {
        return p.matcher(f.getName()).matches();
    }

}
