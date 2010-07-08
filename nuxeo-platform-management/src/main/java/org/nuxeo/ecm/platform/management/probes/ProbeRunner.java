/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     mcedica
 */
package org.nuxeo.ecm.platform.management.probes;

import java.util.Collection;
import java.util.Set;

public interface ProbeRunner {
    
    /**
     * Runs all probes
     * */
    boolean run() ;
    
    /**
     * Runs a probe
     * @return true if probe succeeds, false if not
     * */
    boolean runProbe(ProbeInfo probe);
    
    Collection<ProbeInfo> getRunWithSucessProbesInfo();
    
    Set<String> getProbeNames() ;
    
    ProbeInfo getProbeInfo(String probeQualifiedName);
    
    Set<String> getProbesInError();

    Set<String> getProbesInSuccess() ;
    
}
