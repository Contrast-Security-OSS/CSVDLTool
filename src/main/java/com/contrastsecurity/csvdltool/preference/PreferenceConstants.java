/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.csvdltool.preference;

public class PreferenceConstants {
    public static final String CONTRAST_URL = new String("com.contrastsecurity.csvdltool.contrast_url");
    public static final String SERVICE_KEY = new String("com.contrastsecurity.csvdltool.service_key");
    public static final String USERNAME = new String("com.contrastsecurity.csvdltool.username");
    public static final String TARGET_ORGS = new String("com.contrastsecurity.csvdltool.target_orgs");
    public static final String TSV_STATUS = new String("com.contrastsecurity.csvdltool.tsv_status");

    public static final String PROXY_YUKO = new String("com.contrastsecurity.csvdltool.proxy_yuko");
    public static final String PROXY_HOST = new String("com.contrastsecurity.csvdltool.proxy_host");
    public static final String PROXY_PORT = new String("com.contrastsecurity.csvdltool.proxy_port");
    public static final String PROXY_AUTH = new String("com.contrastsecurity.csvdltool.proxy_auth");
    public static final String PROXY_USER = new String("com.contrastsecurity.csvdltool.proxy_user");
    public static final String PROXY_PASS = new String("com.contrastsecurity.csvdltool.proxy_pass");
    public static final String PROXY_TMP_USER = new String("com.contrastsecurity.csvdltool.proxy_tmp_user");
    public static final String PROXY_TMP_PASS = new String("com.contrastsecurity.csvdltool.proxy_tmp_pass");
    public static final String IGNORE_SSLCERT_CHECK = new String("com.contrastsecurity.csvdltool.ignore_sslcert_check");
    public static final String CONNECTION_TIMEOUT = new String("com.contrastsecurity.csvdltool.connection_timeout");
    public static final String SOCKET_TIMEOUT = new String("com.contrastsecurity.csvdltool.socket_timeout");

    public static final String SLEEP_VUL = new String("com.contrastsecurity.csvdltool.sleep_vul");

    public static final String CSV_OUT_HEADER_VUL = new String("com.contrastsecurity.csvdltool.csv_out_header_vul");
    public static final String CSV_COLUMN_VUL = new String("com.contrastsecurity.csvdltool.csv_out_column_vul");
    public static final String CSV_FILE_FORMAT_VUL = new String("com.contrastsecurity.csvdltool.csv_file_format_vul");

    public static final String SLEEP_LIB = new String("com.contrastsecurity.csvdltool.sleep_lib");

    public static final String CSV_OUT_HEADER_LIB = new String("com.contrastsecurity.csvdltool.csv_out_header_lib");
    public static final String CSV_COLUMN_LIB = new String("com.contrastsecurity.csvdltool.csv_out_column_lib");
    public static final String CSV_FILE_FORMAT_LIB = new String("com.contrastsecurity.csvdltool.csv_file_format_lib");

    public static final String ATTACK_RANGE_DAYTIME = new String("com.contrastsecurity.csvdltool.attack_range_daytime");
    public static final String ATTACK_RANGE_NIGHTTIME = new String("com.contrastsecurity.csvdltool.attack_range_nighttime");
    public static final String ATTACK_START_WEEKDAY = new String("com.contrastsecurity.csvdltool.attack_start_weekday");
    public static final String ATTACK_DETECTED_DATE_FILTER = new String("com.contrastsecurity.csvdltool.attack_detected_date_filter");
    public static final String CSV_OUT_HEADER_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_out_header_attackevent");
    public static final String CSV_COLUMN_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_out_column_attackevent");
    public static final String CSV_FILE_FORMAT_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_file_format_attackevent");

    public static final String VUL_ONLY_PARENT_APP = new String("com.contrastsecurity.csvdltool.only_parent_app_check");
    public static final String INCLUDE_DESCRIPTION = new String("com.contrastsecurity.csvdltool.include_description");
    public static final String INCLUDE_STACKTRACE = new String("com.contrastsecurity.csvdltool.include_stacktrace");

    public static final String ONLY_HAS_CVE = new String("com.contrastsecurity.csvdltool.only_has_cve");
    public static final String INCLUDE_CVE_DETAIL = new String("com.contrastsecurity.csvdltool.include_cve_detail");

    public static final String OPENED_MAIN_TAB_IDX = new String("com.contrastsecurity.csvdltool.opened_main_tab_idx");
    public static final String OPENED_SUB_TAB_IDX = new String("com.contrastsecurity.csvdltool.opened_sub_tab_idx");
    public static final String MEM_WIDTH = new String("com.contrastsecurity.csvdltool.mem_width");
    public static final String MEM_HEIGHT = new String("com.contrastsecurity.csvdltool.mem_height");
}
