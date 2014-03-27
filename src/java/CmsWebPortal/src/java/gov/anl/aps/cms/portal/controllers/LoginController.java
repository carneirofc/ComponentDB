/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.anl.aps.cms.portal.controllers;

import gov.anl.aps.cms.portal.model.beans.UserFacade;
import gov.anl.aps.cms.portal.model.entities.EntityInfo;
import gov.anl.aps.cms.portal.model.entities.User;
import gov.anl.aps.cms.portal.model.entities.UserGroup;
import gov.anl.aps.cms.portal.utilities.ConfigurationUtility;
import gov.anl.aps.cms.portal.utilities.LdapUtility;
import gov.anl.aps.cms.portal.utilities.SessionUtility;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.log4j.Logger;

/**
 * Login controller.
 */
@Named("loginController")
@SessionScoped
public class LoginController implements Serializable
{

    @EJB
    private UserFacade userFacade;

    private String username = null;
    private String password = null;
    private String currentPage = null;
    private boolean loggedInAsAdmin = false;
    private boolean loggedInAsUser = false;
    private User user = null;

    private static final String AdminGroupListPropertyName = "cms.portal.adminGroupList";
    private static final List<String> adminGroupNameList = ConfigurationUtility.getPortalPropertyList(AdminGroupListPropertyName);
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    /**
     * Constructor.
     */
    public LoginController() {
    }

    /**
     * Get password.
     *
     * @return login password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password.
     *
     * @param password login password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get username.
     *
     * @return login username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username.
     *
     * @param username login username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Check if user is logged in.
     *
     * @return true if admin is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return (loggedInAsAdmin || loggedInAsUser);
    }

    /**
     * Check if admin is logged in.
     *
     * @return true if admin is logged in, false otherwise
     */
    public boolean isLoggedInAsAdmin() {
        return loggedInAsAdmin;
    }

    /**
     * Set admin login flag.
     *
     * @param loggedInAsAdmin login flag
     */
    public void setLoggedInAsAdmin(boolean loggedInAsAdmin) {
        this.loggedInAsAdmin = loggedInAsAdmin;
    }

    /**
     * Check if user is logged in.
     *
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedInAsUser() {
        return loggedInAsUser;
    }

    /**
     * Set user login flag.
     *
     * @param loggedInAsUser login flag
     */
    public void setLoggedInAsUser(boolean loggedInAsUser) {
        this.loggedInAsUser = loggedInAsUser;
    }

    private boolean isAdmin(String username) {
        for (String adminGroupName : adminGroupNameList) {
            if (userFacade.isUserMemberOfUserGroup(username, adminGroupName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Login action.
     *
     * @return url to service home page if login is successful, or null in case
     * of errors
     */
    public String login() {
        loggedInAsAdmin = false;
        loggedInAsUser = false;
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            SessionUtility.addWarningMessage("Incomplete Input", "Please enter both username and password.");
            return (username = password = null);
        }

        user = userFacade.findByUsername(username);
        if (user == null) {
            SessionUtility.addErrorMessage("Unknown User", "Username " + username + " is not registered.");
            return (username = password = null);
        }

        boolean isAdminUser = isAdmin(username);
        logger.debug("User " + username + " is admin: " + isAdminUser);
        boolean validCredentials = false;
        if (user.getPassword() != null && user.getPassword().equals(password)) {
            logger.debug("User " + username + " is authorized by CMS");
            validCredentials = true;
        }
        else if (LdapUtility.validateCredentials(username, password)) {
            logger.debug("User " + username + " is authorized by LDAP");
            validCredentials = true;
        }
        else {
            logger.debug("User " + username + " is not authorized");
        }

        if (validCredentials) {
            SessionUtility.setUserId(user.getId());
            if (isAdminUser) {
                loggedInAsAdmin = true;
                SessionUtility.addInfoMessage("Successful Login", "Administrator " + username + " is logged in.");

            }
            else {
                loggedInAsUser = true;
                SessionUtility.addInfoMessage("Successful Login", "User " + username + " is logged in.");
            }

            return getLandingPage();
        }
        else {
            SessionUtility.addErrorMessage("Invalid Credentials", "Username/password combination could not be verified.");
            return (username = password = null);
        }

    }

    public String getLandingPage() {
        if (currentPage != null) {
            return "/views/" + currentPage + "?faces-redirect=true";
        }
        else {
            return "/views/home?faces-redirect=true";
        }
    }

    public String dropAdminRole() {
        loggedInAsAdmin = false;
        loggedInAsUser = true;
        return getLandingPage();
    }

    public String displayUsername() {
        if (isLoggedIn()) {
            return username;
        }
        else {
            return "Not Logged In";
        }
    }

    public String displayRole() {
        if (isLoggedInAsAdmin()) {
            return "Administrator";
        }
        else {
            return "User";
        }
    }

    public boolean isEntityWriteable(EntityInfo entityInfo) {
        // If user is not logged in, object is not writeable
        if (!isLoggedIn()) {
            return false;
        }

        // Admins can write any object.
        if (isLoggedInAsAdmin()) {
            return true;
        }

        // Users can write object if entityInfo != null and:
        // current user is owner, or the object is writeable by owner group
        // and current user is memebr of that group
        if (entityInfo != null) {
            User ownerUser = entityInfo.getOwnerUser();
            if (ownerUser != null && ownerUser.getId().equals(user.getId())) {
                return true;
            }

            Boolean isGroupWriteable = entityInfo.getIsGroupWriteable();
            if (isGroupWriteable == null || !isGroupWriteable.booleanValue()) {
                return false;
            }

            UserGroup ownerUserGroup = entityInfo.getOwnerUserGroup();
            if (ownerUserGroup == null) {
                return false;
            }

            for (UserGroup userGroup : user.getUserGroupList()) {
                if (ownerUserGroup.getId().equals(userGroup.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Logout action.
     *
     * @return url to logout page
     */
    public String logout() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.invalidateSession();
        loggedInAsAdmin = false;
        loggedInAsUser = false;
        user = null;
        currentPage = null;
        return "/views/home?faces-redirect=true";
    }

}
