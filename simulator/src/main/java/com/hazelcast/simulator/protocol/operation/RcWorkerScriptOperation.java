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
package com.hazelcast.simulator.protocol.operation;

import com.hazelcast.simulator.protocol.registry.WorkerQuery;

public class RcWorkerScriptOperation implements SimulatorOperation {

    private String command;
    private WorkerQuery workerQuery = new WorkerQuery();
    private boolean fireAndForget;

    public RcWorkerScriptOperation(String command) {
        this.command = command;
    }

    public RcWorkerScriptOperation(String command, WorkerQuery workerQuery, boolean fireAndForget) {
        this.command = command;
        this.workerQuery = workerQuery;
        this.fireAndForget = fireAndForget;
    }

    public String getCommand() {
        return command;
    }

    public RcWorkerScriptOperation setWorkerQuery(WorkerQuery workerQuery) {
        this.workerQuery = workerQuery;
        return this;
    }

    public RcWorkerScriptOperation setFireAndForget(boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }

    public WorkerQuery getWorkerQuery() {
        return workerQuery;
    }

    public boolean isFireAndForget() {
        return fireAndForget;
    }
}
