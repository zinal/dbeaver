/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.app.standalone.update;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.registry.updater.VersionDescriptor;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.app.standalone.DBeaverApplication;
import org.jkiss.dbeaver.ui.app.standalone.internal.CoreApplicationActivator;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;
import org.osgi.framework.Version;

import java.io.IOException;
import java.util.Calendar;

/**
 * Version checker job
 */
public class DBeaverVersionChecker extends AbstractJob {

    private static final Log log = Log.getLog(DBeaverVersionChecker.class);

    private static final boolean SKIP_VERSION_CHECK;
    private static final Version OVERRIDE_PRODUCT_VERSION;

    static {
        String versionProperty = CommonUtils.toString(System.getProperty("dbeaver.debug.override-product-version"));
        Version version = null;

        if (CommonUtils.isNotEmpty(versionProperty)) {
            try {
                version = new Version(versionProperty);
            } catch (Exception e) {
                log.debug("Cannot parse override version '" + versionProperty + "'", e);
            }
        }

        OVERRIDE_PRODUCT_VERSION = version;
        SKIP_VERSION_CHECK = CommonUtils.toBoolean(System.getProperty("dbeaver.debug.skip-version-check"));
    }

    private final boolean showAlways;

    public DBeaverVersionChecker(boolean force)
    {
        super("DBeaver new version release checker");
        this.showAlways = force;
        setUser(false);
        setSystem(true);
    }

    @Override
    protected IStatus run(DBRProgressMonitor monitor)
    {
        if (monitor.isCanceled() || DBWorkbench.getPlatform().isShuttingDown()) {
            return Status.CANCEL_STATUS;
        }
        boolean showUpdateDialog = showAlways;
        if (!showUpdateDialog) {
            // Check for auto-update settings
            showUpdateDialog = DBWorkbench.getPlatform().getPreferenceStore().getBoolean(DBeaverPreferences.UI_AUTO_UPDATE_CHECK);
            if (showUpdateDialog) {

                long lastVersionCheckTime = DBWorkbench.getPlatform().getPreferenceStore().getLong(DBeaverPreferences.UI_UPDATE_CHECK_TIME);
                if (lastVersionCheckTime > 0) {
                    // Do not check more often than daily
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(lastVersionCheckTime);
                    int checkMonth = cal.get(Calendar.MONTH);
                    int checkDay = cal.get(Calendar.DAY_OF_MONTH);
                    cal.setTimeInMillis(System.currentTimeMillis());
                    int curMonth = cal.get(Calendar.MONTH);
                    int curDay = cal.get(Calendar.DAY_OF_MONTH);
                    if (curMonth == checkMonth && curDay == checkDay) {
                        // Already checked today
                        return Status.OK_STATUS;
                    }
                }
            }
        }
        if (!showAlways && !showUpdateDialog) {
            return Status.OK_STATUS;
        }

        DBWorkbench.getPlatform().getPreferenceStore().setValue(DBeaverPreferences.UI_UPDATE_CHECK_TIME, System.currentTimeMillis());
        IProduct product = Platform.getProduct();
        if (product == null) {
            // No product!
            log.error("No Eclipse product found. Installation is corrupted");
            return Status.OK_STATUS;
        }
        final String updateURL = product.getProperty("versionUpdateURL");
        if (updateURL == null) {
            return Status.OK_STATUS;
        }

        final Version currentVersion = getProductVersion();
        final VersionDescriptor newVersion;

        try {
            newVersion = new VersionDescriptor(DBWorkbench.getPlatform(), updateURL);
        } catch (IOException e) {
            if (showAlways) {
                // Show error dialog only if fired by user
                DBWorkbench.getPlatformUI().showError(CoreMessages.dialog_version_update_title, CoreMessages.dialog_version_update_error_cannot_check_version, e);
            }
            return Status.CANCEL_STATUS;
        }

        if (showAlways || (!isSuppressed(newVersion) && (SKIP_VERSION_CHECK || newVersion.getProgramVersion().compareTo(currentVersion) > 0))) {
            showUpdaterDialog(currentVersion, newVersion);
        }

        return Status.OK_STATUS;
    }

    private void showUpdaterDialog(@NotNull Version currentVersion, @NotNull VersionDescriptor newVersion)
    {
        UIUtils.asyncExec(() -> {
            DBeaverApplication.getInstance().notifyVersionUpgrade(currentVersion, newVersion, !showAlways);
        });
    }

    private static boolean isSuppressed(@NotNull VersionDescriptor version) {
        CoreApplicationActivator activator = CoreApplicationActivator.getDefault();
        return activator != null && activator.getPreferenceStore().getBoolean("suppressUpdateCheck." + version.getPlainVersion());
    }

    @NotNull
    private static Version getProductVersion() {
        return OVERRIDE_PRODUCT_VERSION == null ? GeneralUtils.getProductVersion() : OVERRIDE_PRODUCT_VERSION;
    }
}
