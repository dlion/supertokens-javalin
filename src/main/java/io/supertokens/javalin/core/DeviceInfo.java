package io.supertokens.javalin.core;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo {

    private static DeviceInfo instance = null;
    private List<Device> frontendSDK = new ArrayList<>();

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
        public String name;
        public String version;

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
