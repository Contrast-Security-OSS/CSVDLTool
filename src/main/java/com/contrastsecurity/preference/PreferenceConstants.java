/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
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
 */

package com.contrastsecurity.preference;

public class PreferenceConstants {

    // サーバツリー基点ディレクトリリスト
    public static final String CURRENT_PROP_PATH = new String("jp.co.tabocom.teratermstation.currentPropPath");

    // サーバツリー基点ディレクトリリスト
    public static final String TARGET_DIRS = new String("jp.co.tabocom.teratermstation.targetDirs");

    // TeraTermマクロexeのフルパス
    public static final String TTPMACRO_EXE = new String("jp.co.tabocom.teratermstation.ttpmacroExe");
    // ========== 実行に必要なディレクトリ関連 ========== //
    // 作業用ディレクトリ（ttlファイルの置き場所）
    public static final String WORK_DIR = new String("jp.co.tabocom.teratermstation.workDir");
    // ログディレクトリ（logファイルが置かれます）
    public static final String LOG_DIR = new String("jp.co.tabocom.teratermstation.logDir");
    // TeraTermのINIファイルの置き場所（基本この中のINIファイルを使用します）
    public static final String INIFILE_DIR = new String("jp.co.tabocom.teratermstation.inifileDir");

    // ========== TTLマクロ生成に関する設定 ========== //
    // TTLファイルの出力文字コード
    public static final String TTL_CHARCODE = new String("jp.co.tabocom.teratermstation.ttlCharCode");
    // 認証パスワード伏字文字列
    public static final String TTL_AUTH_PWD_HIDE = new String("jp.co.tabocom.teratermstation.ttlAuthPwdHide");

    // ========== TTLマクロ生成に関する設定 ========== //
    // ログ階層ディレクトリ
    public static final String LOGDIR_PATH = new String("jp.co.tabocom.teratermstation.logDirPath");
    // ログファイル名
    public static final String LOGFILE_NAME = new String("jp.co.tabocom.teratermstation.logFileName");
    // Teratermのlogopenのオプションについて
    public static final String LOGOPEN_OPTION = new String("jp.co.tabocom.teratermstation.logopenOption");

    // ========== その他 ========== //
    // 開いてるタブの位置の保存用（次にツールを起動した時に最後に開いてたタブが選択されるようにするため）
    public static final String OPENED_TAB_IDX = new String("jp.co.tabocom.teratermstation.openedTabIdx");
    // 開発環境で使用するときに毎回IDとパスワードを入れるのが面倒なので、それで使用するためのもの（読み込み専用）
    public static final String AUTH_USER_PWD = new String("jp.co.tabocom.teratermstation.authinfo_");
    // 認証情報オプション接頭辞
    public static final String AUTH_OPTION = new String("jp.co.tabocom.teratermstation.authoption_");
    // ウインドウサイズの幅
    public static final String MEM_WIDTH = new String("jp.co.tabocom.teratermstation.memWidth");
    // ウインドウサイズの縦
    public static final String MEM_HEIGHT = new String("jp.co.tabocom.teratermstation.memHeight");
}
