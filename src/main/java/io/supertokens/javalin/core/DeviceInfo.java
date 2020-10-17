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

package io.supertokens.javalin.core;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo {

    private static DeviceInfo instance = null;
    private final List<Device> frontendSDK = new ArrayList<>();

    @TestOnly
    public static void reset() {
        instance = null;
    }

    private DeviceInfo() {}

    public static DeviceInfo getInstance() {
        if (instance == null) {
            synchronized (DeviceInfo.class) {
                if (instance == null) {
                    instance = new DeviceInfo();
                }
            }
        }
        return instance;
    }

    public Device[] getFrontendSDKs() {
        synchronized (DeviceInfo.class) {
            if (this.frontendSDK.size() == 0) {
                return new Device[]{};
            }
            Device[] result = new Device[this.frontendSDK.size()];
            for (int i = 0; i < this.frontendSDK.size(); i++) {
                result[i] = this.frontendSDK.get(i);
            }
            return result;
        }
    }

    public void addToFrontendSDKs(Device device) {
        synchronized (DeviceInfo.class) {
            for (Device device1 : this.frontendSDK) {
                if (device.equals(device1)) {
                    return;
                }
            }
            this.frontendSDK.add(device);
        }
    }

    public static class Device {
        public final String name;
        public final String version;

        public Device(String name, String version) {
            this.name = name;
            this.version = version;
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Device)) {
                return false;
            }
            Device otherDevice = (Device)other;
            return this.version.equals(otherDevice.version) &&
                    this.name.equals(otherDevice.name);
        }
    }
}
