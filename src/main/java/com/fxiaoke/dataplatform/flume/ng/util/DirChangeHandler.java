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

/**
 * The dir watcher periodically looks at a directory and fires events based on
 * changes to the contents of the directory. When actions on files have been
 * detected these actions are fired.
 */
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
