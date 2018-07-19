package kr.saintdev.pst.models.consts.version;

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-05-10
 */

public class Versions {
    public interface VERSION_NAME {
        String D_NAME = "Dora Stephanie";       // Version D
        String E_NAME = "Eil Ayase";            // Version E
        String F_NAME = "Felix Argyle";         // Version F
    }

    public interface SDK_LEVEL {
        int D_CLOSE_BETA_1 = 400;
        int D_CLOSE_BETA_2 = 401;
        int D_OPEN_BETA_402 = 402;      // Open Beta 4.0.2
        int E_OPEN_BETA_500 = 500;      // Open Beta 5.0.0
        int E_RELEASE_501 = 501;      // Release 5.0.1
        int E_RELEASE_511 = 511;      // Release 5.1.1
        int E_RELEASE_512 = 512;      // Release 5.1.2
    }

    /**
     * @return 현재 SDK 버전 이름을 리턴합니다.
     */
    public static String getVersionName() {
        return VERSION_NAME.E_NAME;
    }

    /**
     *
     * @return 현재 SDK 버전 코드를 리턴합니다.
     */
    public static int getVersionCode() {
        return SDK_LEVEL.E_RELEASE_512;
    }

    /**
     *
     * @return 현재 버전 문자열을 리턴합니다.
     */
    public static String getVersionString() {
        return "Release v5.1.2 - 07.20";
    }
}
