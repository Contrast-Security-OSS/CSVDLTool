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

package com.contrastsecurity.csvdltool;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.FunctionsApi;
import com.contrastsecurity.csvdltool.api.ResultsApi;
import com.contrastsecurity.csvdltool.model.Account;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.serverless.Function;
import com.contrastsecurity.csvdltool.model.serverless.Result;

public class ServerlessResultGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private Account account;
    private List<Function> functions;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ServerlessResultGetWithProgress(Shell shell, PreferenceStore ps, Organization org, Account account) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.account = account;
        this.functions = new ArrayList<Function>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            Api functionsApi = new FunctionsApi(this.shell, this.ps, this.org, this.account);
            List<Function> tmpFunctions = (List<Function>) functionsApi.get();
            // Map<Function, List<Result>> functionMap = new HashMap<Function, List<Result>>();
            for (Function function : tmpFunctions) {
                // functionMap.put(function, new ArrayList<Result>());
                this.functions.add(function);
            }
            Thread.sleep(200);
            Api resultsApi = new ResultsApi(this.shell, this.ps, this.org, this.account);
            List<Result> results = (List<Result>) resultsApi.get();
            for (Result result : results) {
                Function chkFunction = new Function(result.getResourceId());
                // if (functionMap.containsKey(chkFunction)) {
                // functionMap.get(chkFunction).add(result);
                // }
                if (this.functions.contains(chkFunction)) {
                    int index = this.functions.indexOf(chkFunction);
                    Function f = this.functions.get(index);
                    f.getResults().add(result);
                }
            }
            Thread.sleep(100);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public List<Function> getFunctions() {
        return this.functions;
    }

}
