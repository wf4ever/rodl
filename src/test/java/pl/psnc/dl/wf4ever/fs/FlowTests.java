/**
 * 
 */
package pl.psnc.dl.wf4ever.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.common.UserProfile.Role;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;

/**
 * @author piotrek
 * 
 */
public class FlowTests {

    private String userId;

    private static final String ADMIN_ID = "wfadmin";
    private static final String USER_PASSWORD = "password";

    private static final String USERNAME = "John Doe";

    private DigitalLibrary dl;

    private static final FileRecord[] files = new FileRecord[3];

    private static final String[] directories = { "", "dir/", "testdir" };

    private static final String MAIN_FILE_MIME_TYPE = "text/plain";

    private static final String MAIN_FILE_CONTENT = "test";

    private static final String MAIN_FILE_PATH = "mainFile.txt";

    private static final URI RO_URI = URI.create("http://example.org/ROs/foobar/");

    private ResearchObject ro;

    private static final String BASE = "/tmp/testdl/";


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception {
    }


    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() {
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        UserProfile admin = UserProfile.create(ADMIN_ID, "admin", Role.ADMIN);
        admin.save();
        userId = "test-" + new Date().getTime();
        dl = new FilesystemDL(BASE, ADMIN_ID);
        dl.createUser(userId, USER_PASSWORD, USERNAME);
        dl = new FilesystemDL(BASE, userId);
        ro = ResearchObject.create(RO_URI);
        dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);

        files[0] = new FileRecord("file1.txt", "file1.txt", "text/plain");
        files[1] = new FileRecord("file2.txt", "dir/file2.txt", "text/plain");
        files[2] = new FileRecord("file3.jpg", "testdir/file3.jpg", "image/jpg");
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
            throws Exception {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        dl = new FilesystemDL(BASE, ADMIN_ID);
        dl.deleteResearchObject(ro);
        dl = new FilesystemDL(BASE, ADMIN_ID);
        dl.deleteUser(userId);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
    }


    @Test
    public final void testAddingResources()
            throws DigitalLibraryException, IOException, NotFoundException, ConflictException, AccessDeniedException {
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        getZippedVersion();
        getFileContent(files[0]);
        getFileContent(files[1]);
        checkFileExists(files[0].path);
        getZippedFolder(directories[1]);
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        deleteFile(files[0].path);
        deleteFile(files[1].path);
        checkNoFile(files[0].path);
        checkNoFile(files[1].path);
    }


    @Test
    @Ignore
    public final void testEmptyDirectory()
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        createOrUpdateDirectory(directories[1]);
        getZippedFolder(directories[1]);
        createOrUpdateFile(files[1]);
        deleteFile(files[1].path);
        getZippedFolder(directories[1]);
        deleteFile(directories[1]);
        checkNoFile(directories[1]);
    }


    @Test
    @Ignore
    public final void testPermissions()
            throws DigitalLibraryException, IOException, NotFoundException, ConflictException, AccessDeniedException {
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        dl = new FilesystemDL(BASE, userId);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        getFileContent(files[0]);
        getFileContent(files[1]);
        checkCantCreateOrUpdateFile(files[0]);
        checkCantCreateOrUpdateFile(files[1]);
    }


    private void checkNoFile(String path)
            throws DigitalLibraryException, IOException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            dl.getFileContents(ro, path).close();
            fail("Deleted file doesn't throw IdNotFoundException");
        } catch (NotFoundException e) {
            // good
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void checkFileExists(String path)
            throws DigitalLibraryException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            Assert.assertTrue(dl.fileExists(ro, path));
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void deleteFile(String path)
            throws DigitalLibraryException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            dl.deleteFile(ro, path);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void getZippedFolder(String path)
            throws DigitalLibraryException, IOException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            InputStream zip = dl.getZippedFolder(ro, path);
            assertNotNull(zip);
            zip.close();
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void getFileContent(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            InputStream f = dl.getFileContents(ro, file.path);
            assertNotNull(f);
            f.close();
            assertEquals(file.mimeType, dl.getFileInfo(ro, file.path).getMimeType());
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void getZippedVersion()
            throws DigitalLibraryException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            InputStream zip1 = dl.getZippedResearchObject(ro);
            assertNotNull(zip1);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void createOrUpdateFile(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            InputStream f = file.open();
            ResourceInfo r1 = dl.createOrUpdateFile(ro, file.path, f, file.mimeType);
            f.close();
            assertNotNull(r1);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private void checkCantCreateOrUpdateFile(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        InputStream f = file.open();
        try {
            dl.createOrUpdateFile(ro, file.path, f, file.mimeType);
            fail("Should throw an exception when creating file");
        } catch (Exception e) {
            // good
        } finally {
            f.close();
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }

    }


    private void createOrUpdateDirectory(String path)
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ResourceInfo r1 = dl.createOrUpdateFile(ro, path, new ByteArrayInputStream(new byte[0]), "text/plain");
            assertNotNull(r1);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }


    private class FileRecord {

        public String name;

        public String path;

        public String mimeType;


        /**
         * @param name
         * @param dir
         * @param path
         */
        public FileRecord(String name, String path, String mimeType) {
            this.name = name;
            this.path = path;
            this.mimeType = mimeType;
        }


        public InputStream open() {
            return this.getClass().getClassLoader().getResourceAsStream(name);
        }
    }

}
