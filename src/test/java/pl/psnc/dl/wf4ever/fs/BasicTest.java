/**
 * 
 */
package pl.psnc.dl.wf4ever.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.common.UserProfile.Role;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author piotrhol
 * 
 */
public class BasicTest {

    private static final String MAIN_FILE_MIME_TYPE = "text/plain";

    private static final String MAIN_FILE_CONTENT = "test";

    private static final String MAIN_FILE_PATH = "mainFile.txt";

    private static final String ADMIN_ID = "wfadmin";

    private static final String USERNAME = "John Doe";

    private static final String USER_ID = "test-" + new Date().getTime();

    private static final String USER_PASSWORD = "password";

    private static final URI RO_URI = URI.create("http://example.org/ROs/foobar/");

    private ResearchObject ro;

    private static final String BASE = "/tmp/testdl/";


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        UserProfile admin = UserProfile.create(ADMIN_ID, "admin", Role.ADMIN);
        admin.save();
        HibernateUtil.getSessionFactory().getCurrentSession().flush();
        ro = ResearchObject.create(RO_URI);
        Files.createDirectories(Paths.get(BASE));
    }


    @After
    public void tearDown()
            throws IOException {
        try {
            DigitalLibrary dl = new FilesystemDL(BASE, ADMIN_ID);
            dl.deleteResearchObject(ro);
        } catch (Exception e) {

        }
        try {
            DigitalLibrary dlA = new FilesystemDL(BASE, ADMIN_ID);
            dlA.deleteUser(USER_ID);
        } catch (Exception e) {

        }
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        try {
            Files.delete(Paths.get(BASE));
        } catch (DirectoryNotEmptyException e) {
            // was not empty
        }
    }


    /**
     * Test method for
     * {@link pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource#createVersion(java.lang.String, java.lang.String, java.lang.String, java.net.URI)}
     * .
     * 
     * @throws DigitalLibraryException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws IOException
     * @throws AccessDeniedException
     * @throws pl.psnc.dl.wf4ever.dl.AccessDeniedException
     */
    @Test
    public final void testCreateVersionStringStringStringURI()
            throws DigitalLibraryException, NotFoundException, ConflictException, IOException,
            pl.psnc.dl.wf4ever.dl.AccessDeniedException {
        DigitalLibrary dlA = new FilesystemDL(BASE, ADMIN_ID);
        assertTrue(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
        DigitalLibrary dl = new FilesystemDL(BASE, USER_ID);
        dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);
        InputStream in = dl.getFileContents(ro, MAIN_FILE_PATH);
        try {
            String file = IOUtils.toString(in);
            assertEquals("Manifest is properly saved", MAIN_FILE_CONTENT, file);
        } finally {
            in.close();
        }
    }


    @Test
    public final void testGetUserProfile()
            throws DigitalLibraryException, IOException, NotFoundException {
        DigitalLibrary dlA = new FilesystemDL(BASE, ADMIN_ID);
        assertTrue(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
        assertFalse(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
        DigitalLibrary dl = new FilesystemDL(BASE, USER_ID);
        UserProfile user = dl.getUserProfile();
        Assert.assertEquals("User login is equal", USER_ID, user.getLogin());
        Assert.assertEquals("User name is equal", USERNAME, user.getName());
    }


    @Test
    public final void testCreateDuplicateVersion()
            throws DigitalLibraryException, IOException, ConflictException, AccessDeniedException {
        DigitalLibrary dlA = new FilesystemDL(BASE, ADMIN_ID);
        dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
        DigitalLibrary dl = new FilesystemDL(BASE, USER_ID);
        dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);
        try {
            dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
                MAIN_FILE_MIME_TYPE);
            fail("Should throw conflict exception");
        } catch (ConflictException e) {
            // good
        } catch (Exception e) {
            fail("Threw a wrong exception: " + e.getClass().toString());
        }
    }


    @Test
    public final void testStoreAttributes()
            throws DigitalLibraryException, IOException, ConflictException, NotFoundException, AccessDeniedException {
        DigitalLibrary dlA = new FilesystemDL(BASE, ADMIN_ID);
        dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
        DigitalLibrary dl = new FilesystemDL(BASE, USER_ID);
        dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);
        Multimap<URI, Object> atts = HashMultimap.create();
        atts.put(URI.create("a"), "foo");
        atts.put(URI.create("a"), "bar");
        atts.put(URI.create("b"), "lorem ipsum");
        dl.storeAttributes(ro, atts);
    }
}
