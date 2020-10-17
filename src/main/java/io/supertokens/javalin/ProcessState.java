/*
 * Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 * This software is licensed under the Apache License, Version 2.0 (the
 * "License") as published by the Apache Software Foundation.
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.supertokens.javalin;

import java.util.ArrayList;
import java.util.List;

public class ProcessState {

    private final List<EventAndException> history = new ArrayList<>();
    private static ProcessState instance = null;

    private ProcessState() {

    }

    public static void reset(){
        instance = null;
    }

    public static ProcessState getInstance() {
        if (instance == null) {
            instance = new ProcessState();
        }
        return instance;
    }

    public EventAndException getLastEventByName(PROCESS_STATE processState) {
        if (!Constants.IS_TESTING) {
            return null;
        }
        synchronized (this) {
            for (int i = history.size() - 1; i >= 0; i--) {
                if (history.get(i).state == processState) {
                    return history.get(i);
                }
            }
            return null;
        }
    }

    public void addState(PROCESS_STATE processState, Exception e) {
        if (!Constants.IS_TESTING) {
            return;
        }
        synchronized (this) {
            history.add(new EventAndException(processState, e));
        }
    }

    public enum PROCESS_STATE {
        CALLING_SERVICE_IN_VERIFY
    }

    public static class EventAndException {
        public final Exception exception;
        final PROCESS_STATE state;

        public EventAndException(PROCESS_STATE state, Exception e) {
            this.state = state;
            this.exception = e;
        }
    }

}
