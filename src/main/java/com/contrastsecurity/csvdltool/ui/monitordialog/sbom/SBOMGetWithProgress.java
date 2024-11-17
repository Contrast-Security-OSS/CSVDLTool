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

package com.contrastsecurity.csvdltool.ui.monitordialog.sbom;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.AppInfo;
import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.sbom.SBOMCyclonedxApi;
import com.contrastsecurity.csvdltool.api.sbom.SBOMSpdxApi;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class SBOMGetWithProgress implements IRunnableWithProgress {

    public enum SBOMTypeEnum {
        CYCLONEDX,
        SPDX,
    }

    private Shell shell;
    private PreferenceStore ps;
    private String outDirPath;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private SBOMTypeEnum sbomTypeEnum;
    private Timer timer;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public SBOMGetWithProgress(Shell shell, PreferenceStore ps, String outDirPath, List<String> dstApps, Map<String, AppInfo> fullAppMap, SBOMTypeEnum sbomTypeEnum) {
        this.shell = shell;
        this.ps = ps;
        this.outDirPath = outDirPath;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.sbomTypeEnum = sbomTypeEnum;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        int auto_login_interval = this.ps.getInt(PreferenceConstants.AUTO_RELOGIN_INTERVAL);
        if (auto_login_interval > 0) {
            TimerTask task = new TimerTask() {
                public void run() {
                    shell.getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            ((CSVDLToolShell) shell).getMain().loggedOut();
                        }
                    });
                }
            };
            this.timer = new Timer();
            int time = 1000 * 60 * auto_login_interval;
            this.timer.schedule(task, time, time);
        }
        monitor.setTaskName(Messages.getString("sbomgetwithprogress.progress.loading.starting.sbom")); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor, dstApps.size());

        String jsonFolderFormat = this.ps.getString(PreferenceConstants.JSON_FILE_FORMAT_SBOM);
        if (jsonFolderFormat == null || jsonFolderFormat.isEmpty()) {
            jsonFolderFormat = this.ps.getDefaultString(PreferenceConstants.JSON_FILE_FORMAT_SBOM);
        }
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("format", this.sbomTypeEnum.name().toLowerCase()); //$NON-NLS-1$
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String jsonFolderFormated = sub.replace(jsonFolderFormat);

        String timestamp = new SimpleDateFormat(jsonFolderFormated).format(new Date());
        int sleepTrace = this.ps.getInt(PreferenceConstants.SLEEP_SBOM);
        try {
            Set<Organization> orgs = new HashSet<Organization>();
            for (String appLabel : dstApps) {
                orgs.add(fullAppMap.get(appLabel).getOrganization());
            }

            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName(Messages.getString("sbomgetwithprogress.progress.loading.sbom")); //$NON-NLS-1$
            // SubMonitor child1Monitor = sub1Monitor.split(100).setWorkRemaining(dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("%s[%s] %s (%d/%d)", Messages.getString("sbomgetwithprogress.progress.loading.sbom"), org.getName(), appName, appIdx, //$NON-NLS-1$ //$NON-NLS-2$
                        dstApps.size()));
                Api sbomApi = null;
                if (this.sbomTypeEnum == SBOMTypeEnum.CYCLONEDX) {
                    sbomApi = new SBOMCyclonedxApi(this.shell, this.ps, org, appId);
                } else {
                    sbomApi = new SBOMSpdxApi(this.shell, this.ps, org, appId);
                }
                String json = (String) sbomApi.get();
                String filePath = appName + ".json"; //$NON-NLS-1$
                filePath = this.outDirPath + System.getProperty("file.separator") + timestamp + System.getProperty("file.separator") + filePath; //$NON-NLS-1$ //$NON-NLS-2$
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Files.writeString(Paths.get(filePath), json, StandardCharsets.UTF_8);
                monitor.subTask(""); //$NON-NLS-1$
                // child1Monitor.worked(1);
                appIdx++;
                subMonitor.worked(1);
                Thread.sleep(sleepTrace);
            }
        } catch (OperationCanceledException oce) {
            throw new InvocationTargetException(new OperationCanceledException(Messages.getString("sbomgetwithprogress.canceled"))); //$NON-NLS-1$
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (this.timer != null) {
                this.timer.cancel();
            }
        }
        monitor.done();
    }
}
