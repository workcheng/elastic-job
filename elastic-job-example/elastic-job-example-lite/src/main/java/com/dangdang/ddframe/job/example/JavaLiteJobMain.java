/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.log.JobEventLogConfiguration;
import com.dangdang.ddframe.job.example.job.dataflow.JavaDataflowJob;
import com.dangdang.ddframe.job.example.job.listener.SimpleDistributeListener;
import com.dangdang.ddframe.job.example.job.listener.SimpleListener;
import com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public final class JavaLiteJobMain {
    
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:4181", "elastic-job-example-lite-java");
    
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        NestedZookeeperServer.start(4181);
        new JavaLiteJobMain().init();
    }
    
    private void init() {
        regCenter.init();
        
        final JobEventConfiguration jobLogEventConfig = new JobEventLogConfiguration();
        
        final SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(
                JobCoreConfiguration.newBuilder("javaSimpleJob", "0/30 * * * * ?", 10).shardingItemParameters("0=A,1=B,2=C,3=D,4=E,5=F,6=G,7=H,8=I,9=J").build(), 
                JavaSimpleJob.class.getCanonicalName());
        
        final DataflowJobConfiguration dataflowJobConfig = new DataflowJobConfiguration(
                JobCoreConfiguration.newBuilder("javaDataflowElasticJob", "0/5 * * * * ?", 10).shardingItemParameters("0=A,1=B,2=C,3=D,4=E,5=F,6=G,7=H,8=I,9=J").build(), 
                JavaDataflowJob.class.getCanonicalName(), true);
        
        final ScriptJobConfiguration scriptJobConfig = new ScriptJobConfiguration(JobCoreConfiguration.newBuilder("scriptElasticJob", "0/5 * * * * ?", 10)
                .shardingItemParameters("0=A,1=B,2=C,3=D,4=E,5=F,6=G,7=H,8=I,9=J").jobEventConfiguration(jobLogEventConfig).build(), 
                buildScriptCommandLine());
                
        new JobScheduler(regCenter, LiteJobConfiguration.newBuilder(simpleJobConfig).build(), new SimpleListener(), new SimpleDistributeListener(1000L, 2000L)).init();
        new JobScheduler(regCenter, LiteJobConfiguration.newBuilder(dataflowJobConfig).build()).init();
        new JobScheduler(regCenter, LiteJobConfiguration.newBuilder(scriptJobConfig).build()).init();
    }
    
    private static String buildScriptCommandLine() {
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return Paths.get(JavaLiteJobMain.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
        } else {
            Path result = Paths.get(JavaLiteJobMain.class.getResource("/script/demo.sh").getPath());
            changeFilePermissions(result);
            return result.toString();
        }
    }
    
    private static void changeFilePermissions(final Path path) {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
