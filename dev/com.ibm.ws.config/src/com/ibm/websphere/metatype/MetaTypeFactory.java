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
package com.ibm.websphere.metatype;

import java.util.List;

import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

public interface MetaTypeFactory {

    int DURATION_TYPE   = 1000;
    int PID_TYPE        = 1001;
    int LOCATION_TYPE   = 1002;
    int PASSWORD_TYPE   = 1003;
    int DURATION_S_TYPE = 1004;
    int DURATION_M_TYPE = 1005;
    int DURATION_H_TYPE = 1006;
    int ON_ERROR_TYPE   = 1007;
    int HASHED_PASSWORD_TYPE = 1008;
    int LOCATION_FILE_TYPE   = 1009;
    int LOCATION_DIR_TYPE    = 1010;
    int LOCATION_URL_TYPE    = 1011;
    int TOKEN_TYPE      = 1012;

    AttributeDefinition createAttributeDefinition(
        AttributeDefinitionProperties properties);

    ObjectClassDefinition createObjectClassDefinition(
        ObjectClassDefinitionProperties properties,
        List<AttributeDefinition> requiredAttributes,
        List<AttributeDefinition> optionalAttributes);

    Integer getIBMType(String typeName);
}
