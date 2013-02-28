/**
 * 
 */
package pl.psnc.dl.wf4ever.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

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

    private static final UserMetadata ADMIN = new UserMetadata("wfadmin", "John Doe", Role.ADMIN);

    private static final UserMetadata USER = new UserMetadata("test-" + new Date().getTime(), "test user",
            Role.AUTHENTICATED);

    private static final URI RO_URI = URI.create("http://example.org/ROs/foobar/");

    private static final String BASE = "/tmp/testdl/";

    private static final String USER_PASSWORD = "foo";


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        Files.createDirectories(Paths.get(BASE));
    }


    @After
    public void tearDown()
            throws IOException {
        try {
            DigitalLibrary dl = new FilesystemDL(BASE);
            dl.deleteResearchObject(RO_URI);
        } catch (Exception e) {

        }
        try {
            DigitalLibrary dlA = new FilesystemDL(BASE);
            dlA.deleteUser(USER.getLogin());
        } catch (Exception e) {

        }
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        try {
            Files.delete(Paths.get(BASE));
        } catch (DirectoryNotEmptyException | NoSuchFileException e) {
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
        DigitalLibrary dlA = new FilesystemDL(BASE);
        assertTrue(dlA.createUser(USER.getLogin(), USER_PASSWORD, USER.getName()));
        DigitalLibrary dl = new FilesystemDL(BASE);
        dl.createResearchObject(RO_URI, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);
        InputStream in = dl.getFileContents(RO_URI, MAIN_FILE_PATH);
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
        DigitalLibrary dlA = new FilesystemDL(BASE);
        assertTrue(dlA.createUser(USER.getLogin(), USER_PASSWORD, USER.getName()));
        assertFalse(dlA.createUser(USER.getLogin(), USER_PASSWORD, USER.getName()));
        DigitalLibrary dl = new FilesystemDL(BASE);
        UserMetadata user = dl.getUserProfile(USER.getLogin());
        Assert.assertEquals("User login is equal", USER.getLogin(), user.getLogin());
        Assert.assertEquals("User name is equal", USER.getName(), user.getName());
    }


    @Test
    public final void testStoreAttributes()
            throws DigitalLibraryException, IOException, ConflictException, NotFoundException, AccessDeniedException {
        DigitalLibrary dlA = new FilesystemDL(BASE);
        dlA.createUser(USER.getLogin(), USER_PASSWORD, USER.getName());
        DigitalLibrary dl = new FilesystemDL(BASE);
        dl.createResearchObject(RO_URI, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);
        Multimap<URI, Object> atts = HashMultimap.create();
        atts.put(URI.create("a"), "foo");
        atts.put(URI.create("a"), "bar");
        atts.put(URI.create("b"), "lorem ipsum");
        dl.storeAttributes(RO_URI, atts);
    }
}
