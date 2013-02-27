/*
 * (C) Copyright 2002 - 2006 Nuxeo SARL <http://nuxeo.com> and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 *
 *
 */

package org.nuxeo.ecm.platform.filemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentTreeReader;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveWriter;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.filemanager.service.extension.ExportedZipImporter;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import com.google.inject.Inject;

/**
 * Check IO archive import via Unit Tests.
 *
 * @author tiry
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(repositoryName = "default", init = RepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.mimetype.api",
        "org.nuxeo.ecm.platform.mimetype.core",
        "org.nuxeo.ecm.platform.types.api",
        "org.nuxeo.ecm.platform.types.core",
        "org.nuxeo.ecm.platform.filemanager.core" })
public class TestExportedZipImporterPlugin {

    protected DocumentModel sourceWS;

    protected DocumentModel destWS;

    protected DocumentModel wsRoot;

    @Inject
    protected CoreSession coreSession;

    private File archiveFile;

    public void createTestDocumentsAndArchive() throws Exception {
        wsRoot = coreSession.getDocument(new PathRef(
                "default-domain/workspaces"));

        DocumentModel ws = coreSession.createDocumentModel(
                wsRoot.getPathAsString(), "sourceWS", "Workspace");
        ws.setProperty("dublincore", "title", "Source Workspace");
        ws = coreSession.createDocument(ws);

        DocumentModel ws2 = coreSession.createDocumentModel(
                wsRoot.getPathAsString(), "destWS", "Workspace");
        ws2.setProperty("dublincore", "title", "Destination Workspace");
        ws2 = coreSession.createDocument(ws2);

        DocumentModel file = coreSession.createDocumentModel(
                ws.getPathAsString(), "myfile", "File");
        file.setProperty("dublincore", "title", "MyFile");
        file.setProperty("dublincore", "coverage", "MyFileCoverage");
        file = coreSession.createDocument(file);

        DocumentModel folder = coreSession.createDocumentModel(
                ws.getPathAsString(), "myfolder", "Folder");
        folder.setProperty("dublincore", "title", "MyFolder");
        folder = coreSession.createDocument(folder);

        DocumentModel subfile = coreSession.createDocumentModel(
                folder.getPathAsString(), "mysubfile", "File");
        subfile.setProperty("dublincore", "title", "MySubFile");
        subfile = coreSession.createDocument(subfile);

        DocumentReader reader = new DocumentTreeReader(coreSession, ws, false);

        archiveFile = File.createTempFile("TestExportedZipImporterPlugin_", ",zip");
        archiveFile.delete();
        DocumentWriter writer = new NuxeoArchiveWriter(archiveFile);

        DocumentPipe pipe = new DocumentPipeImpl(10);
        pipe.setReader(reader);
        pipe.setWriter(writer);
        pipe.run();

        writer.close();
        reader.close();

        sourceWS = ws;
        destWS = ws2;
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
    }

    @After
    public void cleanupTempFolder() {
        FileUtils.deleteQuietly(archiveFile);
    }

    @Test
    public void testArchiveDetection() throws Exception {
        createTestDocumentsAndArchive();
        ZipFile archive = ExportedZipImporter.getArchiveFileIfValid(archiveFile);
        assertNotNull(archive);
        archive.close();
    }

    @Test
    public void testImportViaFileManager() throws Exception {
        createTestDocumentsAndArchive();
        FileManager fm = Framework.getService(FileManager.class);
        Blob blob = new FileBlob(archiveFile);
        fm.createDocumentFromBlob(coreSession, blob, destWS.getPathAsString(),
                true, "toto.zip");
        DocumentModelList children = coreSession.getChildren(destWS.getRef());
        assertTrue(children.size() > 0);
        DocumentModel importedWS = children.get(0);
        assertEquals(importedWS.getTitle(), sourceWS.getTitle());
        DocumentModelList subChildren = coreSession.getChildren(importedWS.getRef());
        assertSame(2, subChildren.size());

        DocumentModel subFolder = coreSession.getChild(importedWS.getRef(),
                "myfolder");
        assertNotNull(subFolder);

        DocumentModel subFile = coreSession.getChild(importedWS.getRef(),
                "myfile");
        assertNotNull(subFile);

        DocumentModelList subSubChildren = coreSession.getChildren(subFolder.getRef());
        assertSame(1, subSubChildren.size());
    }

    @Test
    public void testOverrideImportViaFileManager() throws Exception {
        createTestDocumentsAndArchive();
        // first update the source DM of the exported source
        sourceWS.setProperty("dublincore", "title", "I have been changed");
        sourceWS = coreSession.saveDocument(sourceWS);

        // remove one children
        DocumentModel subFile = coreSession.getChild(sourceWS.getRef(),
                "myfile");
        coreSession.removeDocument(subFile.getRef());
        coreSession.save();

        Framework.getLocalService(EventService.class).waitForAsyncCompletion();

        FileManager fm = Framework.getService(FileManager.class);
        Blob blob = new FileBlob(archiveFile);
        fm.createDocumentFromBlob(coreSession, blob, wsRoot.getPathAsString(),
                true, "toto.zip");
        sourceWS = coreSession.getChild(wsRoot.getRef(), "sourceWS");
        assertNotNull(sourceWS);
        assertEquals("Source Workspace", sourceWS.getTitle());

        subFile = coreSession.getChild(sourceWS.getRef(), "myfile");
        assertNotNull(subFile);
    }

}
