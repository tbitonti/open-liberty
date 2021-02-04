/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.bval;

import com.ibm.ws.javaee.dd.bval.ValidationConfig;
import com.ibm.ws.javaee.ddmodel.DDEntryAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public class ValidationConfigEntryAdapter implements DDEntryAdapter<ValidationConfig> {

    @Override
    public ValidationConfig adapt(Container root,
                                  OverlayContainer rootOverlay,
                                  ArtifactEntry artifactEntry,
                                  Entry entryToAdapt) throws UnableToAdaptException {

        // Cache using the validation configuration path in the validation
        // configuration root container.

        String configPath = artifactEntry.getPath();

        ValidationConfig validationConfig = (ValidationConfig)
            rootOverlay.getFromNonPersistentCache(configPath, ValidationConfig.class);

        if (validationConfig == null) {
            try {
                ValidationConfigDDParser ddParser =
                    new ValidationConfigDDParser(root, entryToAdapt);
                validationConfig = ddParser.parse();
            } catch (ParseException e) {
                throw new UnableToAdaptException(e);
            }

            rootOverlay.addToNonPersistentCache(configPath, ValidationConfig.class, validationConfig);
        }

        return validationConfig;
    }

}
