/**
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dashboard.ui.config.components.workspace;

import org.jboss.dashboard.LocaleManager;
import org.jboss.dashboard.ui.UIServices;
import org.jboss.dashboard.ui.taglib.formatter.Formatter;
import org.jboss.dashboard.ui.taglib.formatter.FormatterException;
import org.jboss.dashboard.security.BackOfficePermission;
import org.jboss.dashboard.security.WorkspacePermission;
import org.jboss.dashboard.ui.resources.GraphicElement;
import org.jboss.dashboard.ui.resources.Envelope;
import org.jboss.dashboard.ui.resources.Skin;
import org.jboss.dashboard.users.UserStatus;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jboss.dashboard.workspace.WorkspaceImpl;
import org.jboss.dashboard.workspace.WorkspacesManager;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class WorkspacesPropertiesFormatter extends Formatter {

    @Inject
    protected transient Logger log;

    @Inject
    private WorkspacesPropertiesHandler workspacesPropertiesHandler;

    public UserStatus getUserStatus() {
        return UserStatus.lookup();
    }

    public WorkspacesManager getWorkspacesManager() {
        return UIServices.lookup().getWorkspacesManager();
    }

    public WorkspacesPropertiesHandler getWorkspacesPropertiesHandler() {
        return workspacesPropertiesHandler;
    }

    public void setWorkspacesPropertiesHandler(WorkspacesPropertiesHandler workspacesPropertiesHandler) {
        this.workspacesPropertiesHandler = workspacesPropertiesHandler;
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws FormatterException {
        try {
            renderFragment("outputStart");
            if (getWorkspacesManager().getAllWorkspacesIdentifiers().isEmpty()) {
                renderFragment("outputNoWorkspaces");
            } else {
                renderFragment("outputStartRow");
                renderFragment("outputHeaderDelete");
                setAttribute("value", "ui.workspace.name");
                renderFragment("outputHeaders");
                setAttribute("value", "ui.workspace.title");
                renderFragment("outputHeaders");
                renderFragment("outputEndRow");
            }
            int n = 0;
            for (String workspaceId : getWorkspacesManager().getAllWorkspacesIdentifiers()) {
                WorkspaceImpl workspace = (WorkspaceImpl) getWorkspacesManager().getWorkspace(workspaceId);
                WorkspacePermission viewPerm = WorkspacePermission.newInstance(workspace, WorkspacePermission.ACTION_LOGIN);
                if (workspace == null) continue;
                if (!UserStatus.lookup().hasPermission(viewPerm)) continue;
                String estilo;
                if (n % 2 == 0) estilo = "skn-odd_row";
                else estilo = "skn-even_row";

                renderFragment("outputStartRow");
                setAttribute("value", workspaceId);
                setAttribute("estilo", estilo);

                WorkspacePermission deletePerm = WorkspacePermission.newInstance(workspace, WorkspacePermission.ACTION_DELETE);
                boolean canDelete = getUserStatus().hasPermission(deletePerm);
                if (getWorkspacesManager().getAvailableWorkspacesIds().size() < 2)
                    canDelete = false;

                renderFragment(canDelete ? "outputDelete" : "outputCantDelete");

                setAttribute("value", StringEscapeUtils.ESCAPE_HTML4.translate((String) LocaleManager.lookup().localize(workspace.getName())));
                setAttribute("workspaceId", workspaceId);
                setAttribute("estilo", estilo);

                WorkspacePermission editWorkspacePerm = WorkspacePermission.newInstance(workspace, WorkspacePermission.ACTION_EDIT);
                boolean canEdit = getUserStatus().hasPermission(editWorkspacePerm);

                renderFragment(canEdit ? "outputName" : "outputNameDisabled");

                setAttribute("value", StringEscapeUtils.ESCAPE_HTML4.translate((String) LocaleManager.lookup().localize(workspace.getTitle())));
                setAttribute("estilo", estilo);
                renderFragment("outputTitle");
                renderFragment("outputEndRow");
                n++;
            }
            renderFragment("endTable");

            BackOfficePermission workspacePerm = BackOfficePermission.newInstance(null, BackOfficePermission.ACTION_CREATE_WORKSPACE);
            if (getUserStatus().hasPermission(workspacePerm)) {
                renderFragment("outputCreateWorkspaceStart");
                setAttribute("error", workspacesPropertiesHandler.hasError("name"));
                renderFragment("outputCreateWorkspaceName");
                renderI18nInputs("name", 50, getWorkspacesPropertiesHandler().getName());
                renderFragment("outputEndLine");
                setAttribute("error", workspacesPropertiesHandler.hasError("title"));
                renderFragment("outputCreateWorkspaceTitle");
                renderI18nInputs("title", 1000, getWorkspacesPropertiesHandler().getTitle());
                renderFragment("outputEndLine");
                GraphicElement[] skins = UIServices.lookup().getSkinsManager().getAvailableElements();
                renderFragment("outputCreateWorkspaceSkinsStart");
                for (int i = 0; i < skins.length; i++) {
                    Skin skin = (Skin) skins[i];
                    setAttribute("skinId", skin.getId());
                    setAttribute("skinTitle", skin.getDescription().get(LocaleManager.currentLang()));
                    if (skin.getId().equals(UIServices.lookup().getSkinsManager().getDefaultElement().getId()))
                        setAttribute("selected", "selected");
                    else
                        setAttribute("selected", "");
                    renderFragment("outputCreateWorkspaceSkins");
                }
                renderFragment("outputCreateWorkspaceSkinsEnd");

                GraphicElement[] envelopes = UIServices.lookup().getEnvelopesManager().getAvailableElements();
                renderFragment("outputCreateWorkspaceEmvelopesStart");
                for (int i = 0; i < envelopes.length; i++) {
                    Envelope envelope = (Envelope) envelopes[i];
                    setAttribute("envelopeId", envelope.getId());
                    setAttribute("envelopeTitle", envelope.getDescription().get(LocaleManager.currentLang()));
                    if (envelope.getId().equals(UIServices.lookup().getEnvelopesManager().getDefaultElement().getId()))
                        setAttribute("selected", "selected");
                    else
                        setAttribute("selected", "");
                    renderFragment("outputCreateWorkspaceEmvelopes");
                }
                renderFragment("outputCreateWorkspaceEmvelopesEnd");

                renderFragment("outputCreateWorkspaceEnd");
            }
            renderFragment("outputEnd");
        } catch (Exception e) {
            log.error("Error:", e);
        }
    }

    protected void renderI18nInputs(String fieldName, int maxlength, Map<String, String> defaultValue) {
        setAttribute("name", fieldName);
        renderFragment("outputI18nStart");
        String[] langs = LocaleManager.lookup().getPlatformAvailableLangs();
        if (langs != null) {
            for (String lang : langs) {
                String value = defaultValue != null ? defaultValue.get(lang) : null;
                setAttribute("name", fieldName);
                setAttribute("langId", lang);
                setAttribute("maxlength", maxlength);
                setAttribute("value", value != null ? value : "");
                renderFragment("outputInput");
            }
        }
        renderFragment("outputI18nEnd");
    }
}
