package com.twitter.XClone.api.controllers;

import com.twitter.XClone.api.model.*;
import com.twitter.XClone.exceptions.*;
import com.twitter.XClone.model.Comment;
import com.twitter.XClone.model.Friendship;
import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.Tweet;
import com.twitter.XClone.model.dao.TweetsDAO;
import com.twitter.XClone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final TweetsDAO tweetsDAO;

    public UserController(UserService userService,
                          TweetsDAO tweetsDAO) {
        this.userService = userService;
        this.tweetsDAO = tweetsDAO;
    }

    @GetMapping("/tweets")
    public ResponseEntity friendsTweets(@AuthenticationPrincipal LocalUser user, @RequestParam Integer page, @RequestParam Integer size) {
        PaginatedTweets tweets = userService.allFriendsTweets(user, page, size);
        return ResponseEntity.ok(tweets);
    }

    @PostMapping("/like/{tweetId}")
    public ResponseEntity likeUnlikeTweet(@AuthenticationPrincipal LocalUser user, @PathVariable long tweetId) {
        try {
            String state = userService.likeTweet(user, tweetId);
            return ResponseEntity.ok(state);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (TweetDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tweet id does not exist");
        }
    }

    @PostMapping("/post")
    public ResponseEntity postTweet(@AuthenticationPrincipal LocalUser user, @Valid @RequestBody TweetBody body) {
        try {
            Tweet tweet = userService.postTweet(user, body);
            return ResponseEntity.ok().body(tweet);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (TweetDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("comments/{tweetId}")
    public PaginatedComments allComments(@PathVariable long tweetId, @RequestParam Integer page, @RequestParam Integer size) throws TweetDoesNotExistException {
        return userService.allComments(tweetId, page, size);
    }

    @GetMapping("replies/{commentId}")
    public PaginatedReplies allReplies(@PathVariable long commentId, @RequestParam Integer page, Integer size ) throws TweetDoesNotExistException {
        try {
            return userService.tweetReplies(commentId, page, size);
        } catch (CommentDoesNotExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("friend-request/{friendId}")
    public ResponseEntity sendFriendRequest(@AuthenticationPrincipal LocalUser user, @PathVariable long friendId) {
        try {
            Friendship friendRequest = userService.sendFriendRequest(user, friendId);
            return ResponseEntity.ok(friendRequest);
        } catch (UnableToSendRequestToOnselfException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot send friend request to yourself");
        } catch (FriendRequestAlreadySentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friend request already sent");
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

    }

    @DeleteMapping("friend-request/{friendId}")
    public ResponseEntity removeFriendRequest(@AuthenticationPrincipal LocalUser user, @PathVariable long friendId) {
        try {
            long friendRequest = userService.removeFriendRequest(user, friendId);
            return ResponseEntity.ok(friendRequest);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }
    }

    @PostMapping("accept-request/{friendId}")
    public ResponseEntity acceptFriendRequest(@AuthenticationPrincipal LocalUser user, @PathVariable long friendId) {

        try {
            boolean res = userService.acceptFriendRequest(user, friendId);
            return ResponseEntity.ok(res);
        } catch (FriendRequestDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friend Request does not exist");
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }
    }

    @PostMapping("reject-request/{friendId}")
    public List<Friendship> rejectFriendRequest(@AuthenticationPrincipal LocalUser user, @PathVariable long friendId) {
        try {
            return userService.rejectFriendRequest(user, friendId);
        } catch (FriendRequestDoesNotExistException e) {
            throw new RuntimeException(e);
        } catch (UserDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/post/{tweetId}")
    public ResponseEntity<TweetWithLikeStatus> getTweetDetails(@AuthenticationPrincipal LocalUser user, @PathVariable long tweetId) {
        try {
            TweetWithLikeStatus tweet = userService.getTweetDetails(user, tweetId);
            return ResponseEntity.status(HttpStatus.OK).body(tweet);
        } catch (TweetDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/details")
    public ResponseEntity getRequestedUserTweets(
            @AuthenticationPrincipal LocalUser user,
            @RequestParam String username,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        try {
            PaginatedTweets tweets = userService.allUserTweets(user, username, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(tweets);
        } catch (UserDoesNotExistException e) {
            HashMap<String, String> response = new HashMap<>();
            response.put("Error", "No user found with requested username");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> doesUserExist(@RequestParam String username) {
        boolean exists = userService.doesUserExist(username);
        if (exists) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/userDetails")
    public ResponseEntity<UserDetails> userDetails(@AuthenticationPrincipal LocalUser user, @RequestParam String username) {
        try {
            UserDetails details = userService.getUserDetails(user, username);
            return ResponseEntity.status(HttpStatus.OK).body(details);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/postComment")
    public ResponseEntity<Comment> postComment(@AuthenticationPrincipal LocalUser user, @RequestParam long tweetId, @Valid @RequestBody CommentBody body) {
        try {
            Comment comment = userService.postComment(user, tweetId, body);
            return ResponseEntity.ok().body(comment);
        } catch (TweetDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    @GetMapping("/searchUsers")
    public ResponseEntity<List<LocalUser>> searchUsers(@RequestParam String username) {
        List<LocalUser> users = userService.searchForUsers(username);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/commentDetails")
    public ResponseEntity<CommentWithDetails> fetchCommentDetails(@RequestParam long commentId) {
        try {
            CommentWithDetails commentWithDetails = userService.fetchCommentDetails(commentId);
            return ResponseEntity.ok(commentWithDetails);
        } catch (CommentDoesNotExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/user/update")
    public LocalUser updateProfile(@AuthenticationPrincipal LocalUser user,@Valid @RequestBody UpdateProfileBody body) {
        try {
            return userService.updateProfile(user, body);
        } catch (UserDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/followers/{username}")
    public ResponseEntity<List<UserDetails>> fetchUserFollowers(@AuthenticationPrincipal LocalUser user,  @PathVariable String username) {
        try {
            List<UserDetails> followers = userService.fetchUserFollowers(user, username);
            return ResponseEntity.status(HttpStatus.OK).body(followers);
        } catch (UserDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/following/{username}")
    public ResponseEntity<List<UserDetails>> fetchUserFollowing(@AuthenticationPrincipal LocalUser user,  @PathVariable String username) {
        try {
            List<UserDetails> followers = userService.fetchFollowed(user, username);
            return ResponseEntity.status(HttpStatus.OK).body(followers);
        } catch (UserDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }


}
