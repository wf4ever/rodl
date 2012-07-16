package pl.psnc.dl.wf4ever.auth;

import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

public class SMSThreadLocal extends ThreadLocal<SemanticMetadataService> {

    private SemanticMetadataService sms;


    public SMSThreadLocal(SemanticMetadataService sms) {
        this.sms = sms;
    }


    @Override
    protected SemanticMetadataService initialValue() {
        return sms;
    }

}
