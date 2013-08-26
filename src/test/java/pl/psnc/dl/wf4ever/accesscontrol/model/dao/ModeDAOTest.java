package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.model.BaseTest;

public class ModeDAOTest extends BaseTest {

    String id = "http://www.example.com/accesscontrol/modes/1";
    String roUri = "http://www.example.com/ROs/1/";
    ModeDAO dao = new ModeDAO();
    Mode mode;


    @Before
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        mode = new Mode();
        mode.setId(id);
        mode.setRoUri(roUri);
        mode.setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode.PRIVATE);
    }


    @Test
    public void testCRUD() {
        dao.save(mode);
        Assert.assertNotNull(dao.findById(mode.getId()));
        dao.delete(mode);
        Assert.assertNull(dao.findById(mode.getId()));
    }

}
