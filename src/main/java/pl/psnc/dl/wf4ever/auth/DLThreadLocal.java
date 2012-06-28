package pl.psnc.dl.wf4ever.auth;

import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;

public class DLThreadLocal extends ThreadLocal<DigitalLibrary> {

    private DigitalLibrary dl;


    public DLThreadLocal(DigitalLibrary dl) {
        this.dl = dl;
    }


    @Override
    protected DigitalLibrary initialValue() {
        return dl;
    }

}
