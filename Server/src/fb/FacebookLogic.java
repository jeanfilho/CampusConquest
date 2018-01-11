package fb;

import java.util.List;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.Post;
import com.restfb.types.User;

public class FacebookLogic {
	
	/**
	 * Returns a Facebook user based on the delivered access token.
	 * @param accessToken
	 * @return
	 */
	
	@SuppressWarnings("deprecation")
	public static User getUser(String accessToken) {
		FacebookClient fbc = new DefaultFacebookClient(accessToken); //#yolo
		User facebookUser = fbc.fetchObject("me", User.class);
		
		return facebookUser;
	}
	
	public static List<User> getFriendsOfUser(String accessToken) {
		FacebookClient fbc = new DefaultFacebookClient(accessToken);
		
		Connection<User> myFriends = fbc.fetchConnection("me/friends", User.class);				
		Connection<Post> myFeed = fbc.fetchConnection("me/feed", Post.class);

		System.out.println("Count of my friends: " + myFriends.getData().size());

		if(myFeed.getData().size() > 0) {
			System.out.println("First item in my feed: " + myFeed.getData().get(0));
		}		

		User thatsMe = fbc.fetchObject("me", User.class);
		
		System.out.println("User: "+thatsMe);
		
		List<User> usersFriends = myFriends.getData();
		
		
			for(User myBestestFriend: usersFriends) {
				System.out.println("---");
				System.out.println("his/her bestest friend is "+ myBestestFriend.getName() + " " +myBestestFriend.getFirstName() + " "+myBestestFriend.getLastName()+ " -- "+myBestestFriend.getId());
			}			
		
		
		return usersFriends;
				
	}
	
	
	// DefaultFacebookClient is the FacebookClient implementation
	// that ships with RestFB. You can customize it by passing in
	// custom JsonMapper and WebRequestor implementations, or simply
	// write your own FacebookClient instead for maximum control.

//	FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);

	// It's also possible to create a client that can only access
	// publicly-visible data - no access token required. 
	// Note that many of the examples below will not work unless you supply an access token! 

//	FacebookClient publicOnlyFacebookClient = new DefaultFacebookClient();

	// Get added security by using your app secret:

//	FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN, MY_APP_SECRET);
	
	// Google App credentials
	public static String googleAppID = "585987882659";
	public static String googleAppKey = "AIzaSyB-LMoiDEsNu9iWZC1QUYT7u2AX1dY_tdk";

}
































