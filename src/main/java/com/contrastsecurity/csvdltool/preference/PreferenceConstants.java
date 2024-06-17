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
    public static final String CONTRAST_URL = new String("com.contrastsecurity.csvdltool.contrast_url"); //$NON-NLS-1$
    public static final String USERNAME = new String("com.contrastsecurity.csvdltool.username"); //$NON-NLS-1$
    public static final String SERVICE_KEY = new String("com.contrastsecurity.csvdltool.service_key"); //$NON-NLS-1$
    public static final String PASSWORD = new String("com.contrastsecurity.csvdltool.password"); //$NON-NLS-1$
    public static final String BASIC_AUTH_STATUS = new String("com.contrastsecurity.csvdltool.basic_auth_status"); //$NON-NLS-1$
    public static final String PASS_TYPE = new String("com.contrastsecurity.csvdltool.pass_type"); //$NON-NLS-1$
    public static final String XSRF_TOKEN = new String("com.contrastsecurity.csvdltool.xsrf_token"); //$NON-NLS-1$
    public static final String TARGET_ORGS = new String("com.contrastsecurity.csvdltool.target_orgs"); //$NON-NLS-1$
    public static final String TSV_STATUS = new String("com.contrastsecurity.csvdltool.tsv_status"); //$NON-NLS-1$
    public static final String AUTH_RETRY_MAX = new String("com.contrastsecurity.csvdltool.auth_retry_max"); //$NON-NLS-1$
    public static final String SERVERLESS_HOST = new String("com.contrastsecurity.csvdltool.serverless.host"); //$NON-NLS-1$
    public static final String SERVERLESS_TOKEN = new String("com.contrastsecurity.csvdltool.serverless.token"); //$NON-NLS-1$

    public static final String PROXY_YUKO = new String("com.contrastsecurity.csvdltool.proxy_yuko"); //$NON-NLS-1$
    public static final String PROXY_HOST = new String("com.contrastsecurity.csvdltool.proxy_host"); //$NON-NLS-1$
    public static final String PROXY_PORT = new String("com.contrastsecurity.csvdltool.proxy_port"); //$NON-NLS-1$
    public static final String PROXY_AUTH = new String("com.contrastsecurity.csvdltool.proxy_auth"); //$NON-NLS-1$
    public static final String PROXY_USER = new String("com.contrastsecurity.csvdltool.proxy_user"); //$NON-NLS-1$
    public static final String PROXY_PASS = new String("com.contrastsecurity.csvdltool.proxy_pass"); //$NON-NLS-1$
    public static final String PROXY_TMP_USER = new String("com.contrastsecurity.csvdltool.proxy_tmp_user"); //$NON-NLS-1$
    public static final String PROXY_TMP_PASS = new String("com.contrastsecurity.csvdltool.proxy_tmp_pass"); //$NON-NLS-1$
    public static final String IGNORE_SSLCERT_CHECK = new String("com.contrastsecurity.csvdltool.ignore_sslcert_check"); //$NON-NLS-1$
    public static final String CONNECTION_TIMEOUT = new String("com.contrastsecurity.csvdltool.connection_timeout"); //$NON-NLS-1$
    public static final String SOCKET_TIMEOUT = new String("com.contrastsecurity.csvdltool.socket_timeout"); //$NON-NLS-1$
    public static final String AUTO_RELOGIN_INTERVAL = new String("com.contrastsecurity.csvdltool.auto_relogin_interval"); //$NON-NLS-1$

    public static final String SLEEP_VUL = new String("com.contrastsecurity.csvdltool.sleep_vul"); //$NON-NLS-1$

    public static final String CSV_OUT_HEADER_VUL = new String("com.contrastsecurity.csvdltool.csv_out_header_vul"); //$NON-NLS-1$
    public static final String CSV_COLUMN_VUL = new String("com.contrastsecurity.csvdltool.csv_out_column_vul"); //$NON-NLS-1$
    public static final String CSV_FILE_FORMAT_VUL = new String("com.contrastsecurity.csvdltool.csv_file_format_vul"); //$NON-NLS-1$

    public static final String SLEEP_LIB = new String("com.contrastsecurity.csvdltool.sleep_lib"); //$NON-NLS-1$

    public static final String CSV_OUT_HEADER_LIB = new String("com.contrastsecurity.csvdltool.csv_out_header_lib"); //$NON-NLS-1$
    public static final String CSV_COLUMN_LIB = new String("com.contrastsecurity.csvdltool.csv_out_column_lib"); //$NON-NLS-1$
    public static final String CSV_FILE_FORMAT_LIB = new String("com.contrastsecurity.csvdltool.csv_file_format_lib"); //$NON-NLS-1$

    public static final String ATTACK_RANGE_DAYTIME = new String("com.contrastsecurity.csvdltool.attack_range_daytime"); //$NON-NLS-1$
    public static final String ATTACK_RANGE_NIGHTTIME = new String("com.contrastsecurity.csvdltool.attack_range_nighttime"); //$NON-NLS-1$
    public static final String ATTACK_START_WEEKDAY = new String("com.contrastsecurity.csvdltool.attack_start_weekday"); //$NON-NLS-1$
    public static final String ATTACK_DETECTED_DATE_FILTER = new String("com.contrastsecurity.csvdltool.attack_detected_date_filter"); //$NON-NLS-1$
    public static final String ATTACK_DETECTED_DATE_TERM_FR = new String("com.contrastsecurity.csvdltool.attack_detected_date_term_fr"); //$NON-NLS-1$
    public static final String ATTACK_DETECTED_DATE_TERM_TO = new String("com.contrastsecurity.csvdltool.attack_detected_date_term_to"); //$NON-NLS-1$
    public static final String CSV_OUT_HEADER_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_out_header_attackevent"); //$NON-NLS-1$
    public static final String CSV_COLUMN_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_out_column_attackevent"); //$NON-NLS-1$
    public static final String CSV_FILE_FORMAT_ATTACKEVENT = new String("com.contrastsecurity.csvdltool.csv_file_format_attackevent"); //$NON-NLS-1$

    public static final String CSV_OUT_HEADER_SERVER = new String("com.contrastsecurity.csvdltool.csv_out_header_server"); //$NON-NLS-1$
    public static final String CSV_COLUMN_SERVER = new String("com.contrastsecurity.csvdltool.csv_out_column_server"); //$NON-NLS-1$
    public static final String CSV_FILE_FORMAT_SERVER = new String("com.contrastsecurity.csvdltool.csv_file_format_server"); //$NON-NLS-1$

    public static final String VUL_ONLY_PARENT_APP = new String("com.contrastsecurity.csvdltool.only_parent_app_check"); //$NON-NLS-1$
    public static final String VUL_ONLY_CURVUL_EXP = new String("com.contrastsecurity.csvdltool.only_curvul_exp_check"); //$NON-NLS-1$
    public static final String INCLUDE_DESCRIPTION = new String("com.contrastsecurity.csvdltool.include_description"); //$NON-NLS-1$
    public static final String INCLUDE_STACKTRACE = new String("com.contrastsecurity.csvdltool.include_stacktrace"); //$NON-NLS-1$

    public static final String ONLY_HAS_CVE = new String("com.contrastsecurity.csvdltool.only_has_cve"); //$NON-NLS-1$
    public static final String WITH_CVSS = new String("com.contrastsecurity.csvdltool.with_cvss"); //$NON-NLS-1$
    public static final String WITH_EPSS = new String("com.contrastsecurity.csvdltool.with_epss"); //$NON-NLS-1$
    public static final String INCLUDE_CVE_DETAIL = new String("com.contrastsecurity.csvdltool.include_cve_detail"); //$NON-NLS-1$

    public static final String INCLUDE_ARCHIVED_PROJ = new String("com.contrastsecurity.csvdltool.include_archived_proj"); //$NON-NLS-1$
    public static final String INCLUDE_SCANRESULT_STACKTRACE = new String("com.contrastsecurity.csvdltool.include_scanresult_stacktrace"); //$NON-NLS-1$
    public static final String SLEEP_SCANRESULT = new String("com.contrastsecurity.csvdltool.sleep_scanresult"); //$NON-NLS-1$
    public static final String CSV_OUT_HEADER_SCANRESULT = new String("com.contrastsecurity.csvdltool.csv_out_header_scanresult"); //$NON-NLS-1$
    public static final String CSV_COLUMN_SCANRESULT = new String("com.contrastsecurity.csvdltool.csv_out_column_scanresult"); //$NON-NLS-1$
    public static final String CSV_FILE_FORMAT_SCANRESULT = new String("com.contrastsecurity.csvdltool.csv_file_format_scanresult"); //$NON-NLS-1$

    public static final String FILE_OUT_MODE = new String("com.contrastsecurity.csvdltool.file_out_mode"); //$NON-NLS-1$
    public static final String FILE_OUT_DIR = new String("com.contrastsecurity.csvdltool.file_out_dir"); //$NON-NLS-1$

    public static final String OPENED_MAIN_TAB_IDX = new String("com.contrastsecurity.csvdltool.opened_main_tab_idx"); //$NON-NLS-1$
    public static final String OPENED_SUB_TAB_IDX = new String("com.contrastsecurity.csvdltool.opened_sub_tab_idx"); //$NON-NLS-1$
    public static final String MEM_WIDTH = new String("com.contrastsecurity.csvdltool.mem_width"); //$NON-NLS-1$
    public static final String MEM_HEIGHT = new String("com.contrastsecurity.csvdltool.mem_height"); //$NON-NLS-1$
}
