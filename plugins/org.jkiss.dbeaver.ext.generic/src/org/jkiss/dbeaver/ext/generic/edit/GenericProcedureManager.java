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
package org.jkiss.dbeaver.ext.generic.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericObjectContainer;
import org.jkiss.dbeaver.ext.generic.model.GenericProcedure;
import org.jkiss.dbeaver.ext.generic.model.GenericStructContainer;
import org.jkiss.dbeaver.ext.generic.model.meta.GenericMetaModel;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.messages.ModelMessages;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.struct.cache.ListCache;

import java.util.List;
import java.util.Map;

/**
 * Generic procedure manager
 */
public class GenericProcedureManager extends SQLObjectEditor<GenericProcedure, GenericStructContainer> {

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return FEATURE_EDITOR_ON_CREATE;
    }

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, GenericProcedure> getObjectsCache(GenericProcedure object)
    {
        GenericStructContainer container = object.getContainer();
        return new ListCache<>(((GenericObjectContainer) container).getProcedureCache());
    }

    @Override
    protected GenericProcedure createDatabaseObject(
        DBRProgressMonitor monitor, DBECommandContext context, final Object container,
        Object from, Map<String, Object> options)
    {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectCreateCommand command, Map<String, Object> options) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectDeleteCommand command, Map<String, Object> options)
    {
        GenericProcedure object = command.getObject();
        String procedureName;
        GenericMetaModel metaModel = object.getDataSource().getMetaModel();
        if (metaModel.supportsOverloadedProcedureNames()) {
            try {
                procedureName = object.getProcedureSignature(monitor, metaModel.showProcedureParamNames());
            } catch (DBException e) {
                log.debug("Can't read procedure/function parameters", e);
                procedureName = object.getFullyQualifiedName(DBPEvaluationContext.DDL);
            }
        } else {
            procedureName = object.getFullyQualifiedName(DBPEvaluationContext.DDL);
        }
        actions.add(
            new SQLDatabasePersistAction(
                ModelMessages.model_jdbc_drop_table,
                "DROP " + object.getProcedureType().name() +  //$NON-NLS-2$
                    " " + procedureName)
        );
    }

    @Override
    public boolean canCreateObject(Object container) {
        return false;
    }

    @Override
    public boolean canDeleteObject(GenericProcedure object) {
        return true;
    }

}
