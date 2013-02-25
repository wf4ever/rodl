/**
 * 
 */
package pl.psnc.dl.wf4ever.db.dao;

import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

/**
 * RODL user model.
 * 
 * @author piotrhol
 * 
 */
public final class UserProfileDAO extends AbstractDAO<UserProfile> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find by user login.
     * 
     * @param login
     *            user login
     * @return user profile or null
     */
    public UserProfile findByLogin(String login) {
        return findByPrimaryKey(UserProfile.class, login);
    }


    /**
     * Load from database or create a new instance.
     * 
     * @param login
     *            login
     * @param username
     *            username
     * @param role
     *            role
     * @return an instance
     */
    public UserProfile create(String login, String username, Role role) {
        UserProfile result = findByLogin(login);
        if (result == null) {
            return new UserProfile(login, username, role);
        }
        return result;
    }
}
