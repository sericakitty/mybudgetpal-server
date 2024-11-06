package hh.sof03.mybudgetpal.security.services;

public class UserNotFoundException extends Exception {

  /**
   *  UserNotFoundException
   * 
   * @param message
   * @return UserNotFoundException
   */
	public UserNotFoundException(String message) {
		super(message);
	}

}
