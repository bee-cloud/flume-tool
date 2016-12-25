package com.fxiaoke.dataplatform.flume.ng.util;

import java.io.File;

public interface DirChangeHandler {
    /**
     * 处理文件删除或移除
     */
    void fileDeleted(File f);

    /**
     * 处理文件生成或移入
     */
    void fileCreated(File f);

}
