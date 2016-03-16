/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public boolean getRecover() {
        return false;
    }

    public void setRecover(boolean b) {
    }
}
