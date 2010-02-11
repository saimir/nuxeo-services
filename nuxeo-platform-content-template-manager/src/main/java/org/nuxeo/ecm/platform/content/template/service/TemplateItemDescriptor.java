/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.content.template.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Template item descriptor. Immutable.
 */
@XObject(value = "templateItem")
public class TemplateItemDescriptor  implements Serializable {

    private static final long serialVersionUID = 18765764747899L;
    
    @XNode("@typeName")
    protected String typeName;

    @XNode("@id")
    protected String id;

    @XNode("@title")
    protected String title;

    @XNode("@path")
    protected String path;

    @XNode("@description")
    protected String description;

    // Declared as ArrayList to be serializable.
    @XNodeList(value = "acl/ace", type = ArrayList.class, componentType = ACEDescriptor.class)
    public List<ACEDescriptor> acl = new ArrayList<ACEDescriptor>();

    @XNodeList(value = "properties/property", type = ArrayList.class, componentType = PropertyDescriptor.class)
    public List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTitle() {
        return title;
    }

    public List<ACEDescriptor> getAcl() {
        return acl;
    }

    public List<PropertyDescriptor> getProperties() {
        return properties;
    }

}
