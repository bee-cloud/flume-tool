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
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public class WriteConf {

	
	public synchronized  static void writeFlumeConf(Set<String> fileSet,String outputPath) throws Exception {
		if(fileSet.size()==0){
			return;
		}
		String agentSource = "agent.sources = ";
		String agentSinks = "agent.sinks = ";
		String agentChannels = "agent.channels = ";
		
		String agentSourcesType= "agent.sources.{0}.type = com.fxiaoke.dataplatform.flume.ng.source.TailSource";
		String agentSourcesFile = "agent.sources.{0}.file= {1}";
	    String agentSourcesLogicalNode = "agent.sources.{0}.logicalNode= {1}";
	    
	    String agentSourcesPersistDir = "agent.sources.{0}.persistDir= {1}";
	    String agentSourcesChannels = "agent.sources.{0}.channels = {0}";
	    String agentChannelsType = "agent.channels.{0}.type = memory";
	    
	    String agentChannelsCapacity = "agent.channels.{0}.capacity = 10000";
	    String agentChannelsTransactionCapacity = "agent.channels.{0}.transactionCapacity = 100";
	    String agentSinksType = "agent.sinks.{0}.type = logger";
	    String agentSinksTypeArg = "agent.sinks.{0}.maxBytesToLog = 1000";
	    String agentSinksChannel = "agent.sinks.{0}.channel = {0}";
	    int i = 1;
		File file = new File(outputPath);

		if (!file.exists()) {
			file.createNewFile();
		} else {
			file.delete();
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file, true); 
		
	    for(String filePath:fileSet) {
	    	String current = " r"+i;
	    	agentSource += current;
	    	agentSinks += current;
	    	agentChannels += current;
	    	i++;
	    }
    	out.write((agentSource+"\n").getBytes("utf-8"));
    	out.write((agentSinks+"\n").getBytes("utf-8"));
    	out.write((agentChannels+"\n").getBytes("utf-8"));
    	out.write("########################\n".getBytes("utf-8"));
	i=1;

        String[] split=null;
        String logicalNode=null;

	    for(String filePath:fileSet) {
	    	String current = "r"+i;
	    	out.write((MessageFormat.format(agentSourcesType,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentSourcesFile,current,filePath)+"\n").getBytes("utf-8"));

                split=filePath.split("/");

                logicalNode=split[split.length-2]+"."+split[split.length-1].replaceAll("\\.\\d{4}.*\\.log","");

	    	out.write((MessageFormat.format(agentSourcesLogicalNode,current,logicalNode)+"\n").getBytes("utf-8"));

	    	out.write((MessageFormat.format(agentSourcesPersistDir,current,"/tmp/logs")+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentSourcesChannels,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentChannelsType,current)+"\n").getBytes("utf-8"));
	    	
	    	out.write((MessageFormat.format(agentChannelsCapacity,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentChannelsTransactionCapacity,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentSinksType,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentSinksTypeArg,current)+"\n").getBytes("utf-8"));
	    	out.write((MessageFormat.format(agentSinksChannel,current)+"\n").getBytes("utf-8"));
	    	out.write("########################\n".getBytes("utf-8"));
	    	i++;
	    }
	    
	   out.close();
		
	}
	public static void main(String[] args ) throws Exception {
		Set<String> fileSet = new HashSet<String>();
		fileSet.add("e:\\log\\a.log");
		fileSet.add("e:\\log\\b.log");
		fileSet.add("e:\\log\\aa\\a1.log");
		writeFlumeConf(fileSet,"E:\\log\\flume.conf");
		
	}
}
