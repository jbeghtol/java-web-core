package com.ilsian.tomcat;

/**
 * UserInfo - A reference to an authenticated user. This is the user object
 * which is passed around for any of the Action web handlers, and includes
 * only the basic information needed for the servicee (users name and permission level).
 * @author justin
 *
 */
public class UserInfo {
	// available permission levels
	public static final int kLoginInvalid=0;
	public static final int kLoginUser=1;
	public static final int kLoginInstaller=2;
	public static final int kLoginAdmin=3;
	
	// member data
	public String mUsername;		///< User's login name
	public int mLevel;				///< User's security level
	public int mReferenceUID;		///< Application specific reference UID
	
	public UserInfo() {}
	public UserInfo(String uname, int upermit) {
		mUsername = uname;
		mLevel = upermit;
	}
	
	private static final String [] _ROLE_NAMES = { "Invalid", "User", "Installer", "Admin" };
	public static String getRoleName(int level)
	{
		if (level < 0 || level >= _ROLE_NAMES.length)
			return _ROLE_NAMES[kLoginInvalid];
		return _ROLE_NAMES[level];
	}
}
