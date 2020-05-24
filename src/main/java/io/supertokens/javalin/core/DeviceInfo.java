package io.supertokens.javalin.core;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo {

    private static DeviceInfo instance = null;
    private List<Device> frontendSDK = new ArrayList<>();

    private DeviceInfo() {}

    public synchronized static DeviceInfo getInstance() {
        if (instance == null) {
            instance = new DeviceInfo();
        }
        return instance;
    }

    public Device[] getFrontendSDKs() {
        synchronized (DeviceInfo.class) {
            return (Device[]) this.frontendSDK.toArray();
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
        String name;
        String version;

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
