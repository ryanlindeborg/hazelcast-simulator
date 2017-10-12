/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.simulator.utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public final class NativeUtils {

    private static final Logger LOGGER = Logger.getLogger(NativeUtils.class);

    private NativeUtils() {
    }

    public static String execute(String command) {
        return new BashCommand(command).execute();
    }

    public static String execute(String command, boolean throwException) {
        return new BashCommand(command)
                .setThrowsException(throwException)
                .execute();
    }

    public static void kill(int pid) {
        LOGGER.info("Sending -9 signal to PID " + pid);
        try {
            Runtime.getRuntime().exec("/bin/kill -9 " + pid + " >/dev/null");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
     *
     * @return the PID of this JVM or <tt>-1</tt> if not PID was retrieved
     */
    public static int getPID() {
        Integer pid = getPidFromManagementBean();
        if (pid != null) {
            return pid;
        }
        pid = getPidViaReflection();
        if (pid != null) {
            return pid;
        }
        return -1;
    }

    static Integer getPidFromManagementBean() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return getPidFromBeanString(name);
    }

    static Integer getPidFromBeanString(String name) {
        int indexOf = name.indexOf('@');
        if (indexOf == -1) {
            return null;
        }
        String pidString = name.substring(0, indexOf);
        try {
            return Integer.parseInt(pidString);
        } catch (NumberFormatException e) {
            LOGGER.warn(e);
            return null;
        }
    }

    static Integer getPidViaReflection() {
        try {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement management = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pidMethod = management.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);

            return (Integer) pidMethod.invoke(management);
        } catch (Exception e) {
            LOGGER.warn(e);
            return null;
        }
    }
}
