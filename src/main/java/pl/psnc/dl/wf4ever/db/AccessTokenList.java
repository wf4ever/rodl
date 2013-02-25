package pl.psnc.dl.wf4ever.db;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of access tokens for the User Management API.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement(name = "access-tokens")
public class AccessTokenList {

    /** the list of tokens. */
    protected List<AccessToken> list = new ArrayList<AccessToken>();


    /**
     * Constructor.
     */
    public AccessTokenList() {
    }


    /**
     * Constructor.
     * 
     * @param list
     *            the list of tokens
     */
    public AccessTokenList(List<AccessToken> list) {
        this.list = list;
    }


    @XmlElement(name = "access-token")
    public List<AccessToken> getList() {
        return list;
    }
}
