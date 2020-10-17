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

package io.supertokens.javalin.tests;

import io.supertokens.javalin.Config;
import io.supertokens.javalin.core.DeviceInfo;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.querier.Querier;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static String getInstallationDir() { // without a /
        String result = System.getProperty("installdir");
        return result == null || result.equals("") ? "../supertokens-root" : result;
    }

    public static void executeCommand(String[] command, boolean waitFor) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(getInstallationDir()));
        Process p = pb.start();
        if (waitFor) {
            p.waitFor();
        }
    }

    public static void setKeyValueInConfig(String key, String value) throws IOException {
        File file = new File(getInstallationDir() + "/config.yaml");
        FileWriter fr = new FileWriter(file, true);
        BufferedWriter br = new BufferedWriter(fr);
        br.write(key + ": " + value + "\n");
        br.close();
        fr.close();
    }

    public static Map<String, String> extractInfoFromResponse(HttpURLConnection connection) {
        Map<String, String> result = new HashMap<>();
        connection.getHeaderFields().forEach((key, value) -> {
            if (key == null) {
                return;
            }
            if (key.equals("anti-csrf")) {
                result.put("antiCsrf", value.get(0));
            } else if (key.equals("id-refresh-token")) {
                result.put("idRefreshTokenFromHeader", value.get(0));
            } else if (key.equalsIgnoreCase("set-cookie")) {
                for (String i : value) {
                    if (i.split(";")[0].split("=")[0].equals("sAccessToken")) {
                        try {
                            String[] tokenValue = i.split(";")[0].split("=");
                            result.put("accessToken",tokenValue.length == 2 ? tokenValue[1] : "");
                            result.put("accessTokenPath",i.split(";")[1].split("=")[1]);
                            if (i.split(";")[2].split("=")[0].trim().equals("Expires")) {
                                result.put("accessTokenExpiry", i.split(";")[2].split("=")[1]);
                            } else {
                                result.put("accessTokenDomain", i.split(";")[2].split("=")[1]);
                                result.put("accessTokenExpiry", i.split(";")[3].split("=")[1]);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            result.put("accessToken", "");
                            result.put("accessTokenPath", "");
                            result.put("accessTokenDomain", "");
                            result.put("accessTokenExpiry", "");
                        }
                    } else if (i.split(";")[0].split("=")[0].equals("sRefreshToken")) {
                        try {
                            String[] tokenValue = i.split(";")[0].split("=");
                            result.put("refreshToken", tokenValue.length == 2 ? tokenValue[1] : "");
                            result.put("refreshTokenPath",i.split(";")[1].split("=")[1]);
                            if (i.split(";")[2].split("=")[0].trim().equals("Expires")) {
                                result.put("refreshTokenExpiry", i.split(";")[2].split("=")[1]);
                            } else {
                                result.put("refreshTokenDomain", i.split(";")[2].split("=")[1]);
                                result.put("refreshTokenExpiry", i.split(";")[3].split("=")[1]);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            result.put("refreshToken", "");
                            result.put("refreshTokenPath", "");
                            result.put("refreshTokenDomain", "");
                            result.put("refreshTokenExpiry", "");
                        }
                    } else {
                        try {
                            String[] tokenValue = i.split(";")[0].split("=");
                            result.put("idRefreshTokenFromCookie", tokenValue.length == 2 ? tokenValue[1] : "");
                            if (i.split(";")[2].split("=")[0].trim().equals("Expires")) {
                                result.put("idRefreshTokenExpiry", i.split(";")[2].split("=")[1]);
                            } else {
                                result.put("idRefreshTokenDomain", i.split(";")[2].split("=")[1]);
                                result.put("idRefreshTokenExpiry", i.split(";")[3].split("=")[1]);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            result.put("idRefreshTokenFromCookie", "");
                            result.put("idRefreshTokenDomain", "");
                            result.put("idRefreshTokenExpiry", "");
                        }
                    }
                }
            }
        });
        return result;
    }

    public static void setupST() throws IOException, InterruptedException {
        String installationPath = getInstallationDir();
        executeCommand(new String[]{"cp", "temp/licenseKey", "./licenseKey"}, true);
        executeCommand(new String[]{"cp", "temp/config.yaml", "./config.yaml"}, true);
        setKeyValueInConfig("refresh_api_path", "/refresh");
        setKeyValueInConfig("enable_anti_csrf", "true");
    }

    public static void cleanST() throws IOException, InterruptedException {
        String installationPath = getInstallationDir();
        executeCommand(new String[]{"rm", "licenseKey"}, true);
        executeCommand(new String[]{"rm", "config.yaml"}, true);
        executeCommand(new String[]{"rm", "-rf", ".webserver-temp-*"}, true);
        executeCommand(new String[]{"rm", "-rf", ".started"}, true);
    }

    public static List<String> getListOfPids() throws FileNotFoundException {
        List<String> result = new ArrayList<>();
        String installationPath = getInstallationDir();
        File f = new File(installationPath + "/.started");
        if (!f.exists()) {
            return new ArrayList<>();
        }
        for (String fileName : Objects.requireNonNull(f.list())) {
            File myObj = new File(installationPath + "/.started/" + fileName);
            Scanner myReader = new Scanner(myObj);
            StringBuilder data = new StringBuilder();
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine());
            }
            result.add(data.toString());
            myReader.close();
        }
        return result;
    }

    public static void stopST(String pid) throws Exception {
        List<String> pidsBefore = getListOfPids();
        if (pidsBefore.size() == 0) {
            return;
        }
        executeCommand(new String[]{"kill", pid}, true);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 10000) {
            List<String> pidsAfter = getListOfPids();
            if (new HashSet<>(pidsAfter).contains(pid)) {
                Thread.sleep(100);
            } else {
                return;
            }
        }
        throw new Exception("error while stopping ST with PID: " + pid);
    }

    public static void killAllST() throws FileNotFoundException {
        List<String> pids = getListOfPids();
        pids.forEach(s -> {
            try {
                stopST(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        HandshakeInfo.reset();
        DeviceInfo.reset();
        Querier.reset();
        Config.reset();
    }

    static String startST() throws Exception {
        return startST("localhost", 8080);
    }

    static String startST(String host, Integer port) throws Exception {
        String installationPath = getInstallationDir();
        List<String> pidsBefore = getListOfPids();
        executeCommand(new String[]{"bash", "-c", "java -Djava.security.egd=file:/dev/urandom -classpath \"./core/*:./plugin-interface/*\" io.supertokens.Main ./ DEV host=" +
                                host + " port=" + port}, false);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 50000) {
            List<String> pidsAfter = getListOfPids();
            if (pidsAfter.size() <= pidsBefore.size()) {
                Thread.sleep(100);
                continue;
            }
            List<String> nonIntersection = pidsAfter.stream().filter(x -> !new HashSet<>(pidsBefore).contains(x)).collect(Collectors.toList());;
            if (nonIntersection.size() != 1) {
                throw new Exception("something went wrong while starting ST");
            } else {
                return nonIntersection.get(0);
            }
        }
        throw new Exception("could not start ST process");
    }

}
