package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import org.junit.Before;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;

public class ModeDAOTest {

    String id = "http://www.example.com/accesscontrol/modes/1";
    String roUri = "http://www.example.com/ROs/1/";


    @Before
    private void setUp() {
        Mode mode = new Mode();
        mode.setId(id);
        mode.setRoUri(roUri);
        mode.setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode.PRIVATE);
    }
}
