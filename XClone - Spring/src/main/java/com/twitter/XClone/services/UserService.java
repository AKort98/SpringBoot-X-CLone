package com.twitter.XClone.services;

import com.twitter.XClone.api.model.*;
import com.twitter.XClone.exceptions.*;
import com.twitter.XClone.model.*;
import com.twitter.XClone.model.dao.*;
import com.twitter.XClone.model.dao.CommentImagesDAO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final VerificationTokenDAO verificationTokenDAO;
    private final TweetsDAO tweetsDAO;
    private final LikesDAO likesDAO;
    private final FriendsDAO friendsDAO;
    private final ImagesDAO imagesDAO;
    private final CommentsDAO commentsDAO;
    private final CommentImagesDAO commentImagesDAO;


    public UserService(LocalUserDAO localUserDAO, EncryptionService encryptionService, JWTService jwtService, EmailService emailService, VerificationTokenDAO verificationTokenDAO, TweetsDAO tweetsDAO, LikesDAO likesDAO, FriendsDAO friendsDAO, ImagesDAO imagesDAO, CommentsDAO commentsDAO, CommentImagesDAO commentImagesDAO) {
        this.localUserDAO = localUserDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenDAO = verificationTokenDAO;
        this.tweetsDAO = tweetsDAO;
        this.likesDAO = likesDAO;
        this.friendsDAO = friendsDAO;
        this.imagesDAO = imagesDAO;
        this.commentsDAO = commentsDAO;
        this.commentImagesDAO = commentImagesDAO;
    }


    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExists, EmailFailureException {
        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent() || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExists();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setDisplayname(registrationBody.getDisplayname());
        user.setAvatar(registrationBody.getAvatar());
        VerificationToken token = createToken(user);
        emailService.sendVerificationEmail(token);
        return localUserDAO.save(user);
    }

    private VerificationToken createToken(LocalUser user) {
        VerificationToken token = new VerificationToken();
        token.setToken(jwtService.generateVerificationToken(user));
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token.setUser(user);
        user.getVerificationTokens().add(token);
        return token;
    }


    public String loginUser(LoginBody body) throws UserDoesNotExistException, UserIsNotVerifiedException, IncorrectPasswordException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(body.getUsername());
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        if (!user.isVerified()) {
            throw new UserIsNotVerifiedException();
        }
        if (!encryptionService.checkPassword(body.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        return jwtService.generateToken(user);
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);
        if (!opToken.isPresent()) {
            return false;
        }
        VerificationToken verificationToken = opToken.get();
        LocalUser user = verificationToken.getUser();
        user.setVerified(true);
        localUserDAO.save(user);
        verificationTokenDAO.deleteByUser(user);
        return true;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> opUser = localUserDAO.findByEmailIgnoreCase(email);
        if (!opUser.isPresent()) {
            throw new EmailNotFoundException();
        }
        LocalUser user = opUser.get();
        String token = jwtService.generatePasswordResetToken(user);
        emailService.sendPasswordResetEmail(user, token);
    }

    public void resetPassword(ResetPasswordBody body) throws UserDoesNotExistException {
        String token = body.getToken();
        String email = jwtService.getResetEmail(token);
        Optional<LocalUser> opUser = localUserDAO.findByEmailIgnoreCase(email);
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        user.setPassword(encryptionService.encryptPassword(body.getPassword()));
        localUserDAO.save(user);
    }

    @Transactional
    public PaginatedTweets allFriendsTweets(LocalUser user, Integer page, Integer size) {
        //Find all friends of provided user
        List<Friendship> friends = friendsDAO.findByUser(user);
        //Initialize an empty arrayList
        List<LocalUser> allUsers = new ArrayList();
        //Add current user to the arrayList to also fetch the users tweets
        allUsers.add(user);

        //Check the friendship status => if accepted = add to arrayList
        for (Friendship friend : friends) {
            if (friend.getStatus().equalsIgnoreCase("accepted")) {
                allUsers.add(friend.getFriend());
            }
        }

        //Sort according to tweet creation date
        Sort sort = Sort.by(Direction.DESC, "createdAt");

        //Fetch all the tweets based on the allUsers arrayList
        List<Tweet> allTweets = tweetsDAO.findByUsers(allUsers, sort);

        //Check total number of tweets returned ==> find how many pages based on the size arg
        int totalTweets = allTweets.size();
        int totalPages = totalTweets / size;

        List<UserLikes> allLikesByUser = likesDAO.findByUser(user);
        Set<Long> allLikesIds = new HashSet<>();

        for (UserLikes like : allLikesByUser) {
            allLikesIds.add(like.getTweet().getId());
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        List<Tweet> tweets = tweetsDAO.findByUser(allUsers, pageable);
        List<TweetWithLikeStatus> tweetWithLikeStatuses = new ArrayList<>();

        for (Tweet tweet : tweets) {
            TweetWithLikeStatus tweetWithLikeStatus = new TweetWithLikeStatus();
            tweetWithLikeStatus.setTweet(tweet);
            if (!allLikesIds.contains(tweet.getId())) {
                tweetWithLikeStatus.setLiked(false);
            } else {
                tweetWithLikeStatus.setLiked(true);
            }
            tweetWithLikeStatuses.add(tweetWithLikeStatus);
        }

        PaginatedTweets paginatedTweets = new PaginatedTweets();
        paginatedTweets.setTweets(tweetWithLikeStatuses);
        paginatedTweets.setTotalPageNumber(totalPages);
        paginatedTweets.setNextPage(page + 1);
        return paginatedTweets;
    }

    @Transactional
    public String likeTweet(LocalUser user, Long tweetId) throws UserDoesNotExistException, TweetDoesNotExistException {

        Optional<Tweet> opTweet = tweetsDAO.findById(tweetId);
        if (!opTweet.isPresent()) {
            throw new TweetDoesNotExistException();
        }
        Tweet tweet = opTweet.get();
        Optional<UserLikes> opLike = likesDAO.findByUserAndTweet(user, tweet);
        if (!opLike.isPresent()) {
            UserLikes likes = new UserLikes();
            likes.setTweet(tweet);
            likes.setUser(user);
            likesDAO.save(likes);
            return "Tweet liked!";
        }
        UserLikes like = opLike.get();
        likesDAO.deleteById(like.getId());
        return "Tweet unliked";
    }

    @Transactional
    public Tweet postTweet(LocalUser user, TweetBody body) throws UserDoesNotExistException, TweetDoesNotExistException {
        Tweet tweet = new Tweet();
        tweet.setContent(body.getContent());
        tweet.setUser(user);
        tweet.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        long id = tweetsDAO.save(tweet).getId();
        Optional<Tweet> opTweet = tweetsDAO.findById(id);
        if (!opTweet.isPresent()) {
            throw new TweetDoesNotExistException();
        }
        Tweet savedTweet = opTweet.get();
        List<String> urls = body.getImages();
        if (urls != null) {
            if (urls.size() > 0) {
                for (String url : urls) {
                    Images images = new Images();
                    images.setUrl(url);
                    images.setTweet(savedTweet);
                    imagesDAO.save(images);
                }
            }
        }
        return savedTweet;
    }

    public PaginatedComments allComments(long tweetId, Integer page, Integer size) throws TweetDoesNotExistException {
        Optional<Tweet> optionalTweet = tweetsDAO.findById(tweetId);
        if (!optionalTweet.isPresent()) {
            throw new TweetDoesNotExistException();
        }
        Tweet tweet = optionalTweet.get();

        long totalComment = commentsDAO.countByTweet(tweet);
        int totalPages = (int) totalComment / size;
        Integer nextPage = (page < totalPages - 1) ? page + 1 : -1;

        Sort sort = Sort.by(Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        List<Comment> allComments = commentsDAO.findByTweet(tweet, pageable);
        ArrayList<CommentWithDetails> commentWithDetailsList = new ArrayList<>();

        for (Comment c : allComments) {
            CommentWithDetails commentWithDetails = new CommentWithDetails();
            commentWithDetails.setComment(c);
            long count = commentsDAO.countByParent_comment(c);
            commentWithDetails.setReplyCount(count);
            commentWithDetailsList.add(commentWithDetails);
        }

        PaginatedComments comments = new PaginatedComments();
        comments.setComments(commentWithDetailsList);
        comments.setTotalPageNumber(totalPages);
        comments.setNextPage(nextPage);

        return comments;
    }

    public PaginatedReplies tweetReplies(long commentId, Integer page, Integer size) throws CommentDoesNotExistsException {


        Optional<Comment> opComment = commentsDAO.findById(commentId);
        if (!opComment.isPresent()) {
            throw new CommentDoesNotExistsException();
        }
        Comment comment = opComment.get();

        long replyCount = commentsDAO.countByParent_comment(comment);
        Integer totalPageNumber = (int) Math.ceil((double) replyCount / size);
        Integer nextPage = (page < totalPageNumber - 1) ? page + 1 : -1;

        Sort sort = Sort.by(Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        List<Comment> allReplies = commentsDAO.findByParent_comment(comment, pageable);
        List<CommentWithDetails> commentWithDetailsList = new ArrayList<>();
        for (Comment c : allReplies) {
            CommentWithDetails commentWithDetails = new CommentWithDetails();
            commentWithDetails.setComment(c);
            long count = commentsDAO.countByParent_comment(c);
            commentWithDetails.setReplyCount(count);
            commentWithDetailsList.add(commentWithDetails);
        }

        PaginatedReplies replies = new PaginatedReplies();
        replies.setReplies(commentWithDetailsList);
        replies.setNextPage(nextPage);
        replies.setTotalPageCount(totalPageNumber);
        return replies;


    }

    public Friendship sendFriendRequest(LocalUser user, long friendId) throws UnableToSendRequestToOnselfException, FriendRequestAlreadySentException, UserDoesNotExistException {
        Optional<LocalUser> opFriend = localUserDAO.findById(friendId);
        if (!opFriend.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser friend = opFriend.get();
        if (friend.getId() == user.getId()) {
            throw new UnableToSendRequestToOnselfException();
        }
        Optional<Friendship> optionalFriendship = friendsDAO.findByUserAndFriend(user, friend);
        if (optionalFriendship.isPresent()) {
            throw new FriendRequestAlreadySentException();
        }
        Friendship friendship = new Friendship();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus("accepted");
        return friendsDAO.save(friendship);
    }

    @Transactional
    public long removeFriendRequest(LocalUser localUser, long friendId) throws UserDoesNotExistException {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Optional<LocalUser> opFriend = localUserDAO.findById(friendId);
        if (!opFriend.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser friend = opFriend.get();
        return friendsDAO.deleteByUserAndFriend(localUser, friend);
    }

    public boolean acceptFriendRequest(LocalUser localUser, long friendId) throws FriendRequestDoesNotExistException, UserDoesNotExistException {
        Optional<LocalUser> opFriend = localUserDAO.findById(friendId);
        Optional<LocalUser> opUser = localUserDAO.findById(localUser.getId());
        if (!opFriend.isPresent() || !opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        LocalUser friend = opFriend.get();
        Optional<Friendship> opRecord = friendsDAO.findByUserAndFriend(user, friend);
        if (!opRecord.isPresent()) {
            throw new FriendRequestDoesNotExistException();
        }
        Friendship record = opRecord.get();
        record.setStatus("Accepted");
        friendsDAO.save(record);
        return true;
    }

    public List<Friendship> rejectFriendRequest(LocalUser localUser, long userId) throws FriendRequestDoesNotExistException, UserDoesNotExistException {
        Optional<LocalUser> opUser = localUserDAO.findById(localUser.getId());
        Optional<LocalUser> opFriend = localUserDAO.findById(userId);
        if (!opUser.isPresent() || !opFriend.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        LocalUser friend = opFriend.get();
        List<Friendship> friendRequests = friendsDAO.findByFriend(user);
        for (Friendship friendship : friendRequests) {
            if (friendship.getUser().getId() == friend.getId()) {
                friendship.setStatus("Rejected");
                friendsDAO.save(friendship);
            }
        }

        return friendRequests;
    }

    public TweetWithLikeStatus getTweetDetails(LocalUser user, long tweetId) throws TweetDoesNotExistException {
        Optional<Tweet> opTweet = tweetsDAO.findById(tweetId);
        if (!opTweet.isPresent()) {
            throw new TweetDoesNotExistException();
        }
        Tweet tweet = opTweet.get();
        Optional<UserLikes> like = likesDAO.findByUserAndTweet(user, tweet);
        TweetWithLikeStatus tweetWithLikeStatus = new TweetWithLikeStatus();
        tweetWithLikeStatus.setTweet(tweet);
        if (like.isPresent()) {
            tweetWithLikeStatus.setLiked(true);
        } else {
            tweetWithLikeStatus.setLiked(false);
        }
        return tweetWithLikeStatus;
    }

    public PaginatedTweets allUserTweets(LocalUser loggedUser, String username, Integer page, Integer size) throws UserDoesNotExistException {
        try {
            Thread.sleep(1000); // simulate traffic
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        long tweetCount = tweetsDAO.countByUser(user);

        Integer totalPageNumber = (int) Math.ceil((double) tweetCount / size);
        Integer nextPage = (page < totalPageNumber - 1) ? page + 1 : -1;


        List<UserLikes> userLikes = likesDAO.findByUser(loggedUser);
        Set<Long> userLikesIds = new HashSet<>();

        for (UserLikes like : userLikes) {
            userLikesIds.add(like.getTweet().getId());
        }

        Sort sort = Sort.by(Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);


        List<Tweet> requestedUserTweets = tweetsDAO.findByUser_Username(user.getUsername(), pageable);
        List<TweetWithLikeStatus> tweetWithLikeStatuses = new ArrayList<>();

        for (Tweet t : requestedUserTweets) {
            TweetWithLikeStatus tweet = new TweetWithLikeStatus();
            tweet.setTweet(t);
            if (userLikesIds.contains(t.getId())) {
                tweet.setLiked(true);
            } else {
                tweet.setLiked(false);
            }
            tweetWithLikeStatuses.add(tweet);
        }

        PaginatedTweets paginatedTweets = new PaginatedTweets();
        paginatedTweets.setTweets(tweetWithLikeStatuses);
        paginatedTweets.setNextPage(nextPage);
        paginatedTweets.setTotalPageNumber(totalPageNumber);
        return paginatedTweets;
    }

    public boolean doesUserExist(String username) {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (!opUser.isPresent()) {
            return false;
        }
        LocalUser user = opUser.get();
        if (user.isVerified()) {
            return true;
        }
        return false;
    }

    public UserDetails getUserDetails(LocalUser loggedInUser, String username) throws UserDoesNotExistException {

        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser user = opUser.get();
        long tweetCount = tweetsDAO.countByUser(user);
        long followingCount = friendsDAO.countByUserAndStatusLike(user, "accepted");
        long followerCount = friendsDAO.countByFriendAndStatusLike(user, "accepted");


        Friendship friendship = friendsDAO.findByUserAndFriendAndStatusLikeIgnoreCase(loggedInUser, user, "accepted");

        UserDetails details = new UserDetails();
        details.setUser(user);
        details.setTweetCount(tweetCount);
        details.setFollowingCount(followingCount);
        details.setFollowerCount(followerCount);
        if (friendship != null) {
            details.setFollowedByCurrentLoggedInUser(true);
        } else {
            details.setFollowedByCurrentLoggedInUser(false);
        }
        return details;
    }

    public Comment postComment(LocalUser user, long tweetId, CommentBody body) throws TweetDoesNotExistException {
        Optional<Tweet> optionalTweet = tweetsDAO.findById(tweetId);
        if (!optionalTweet.isPresent()) {
            throw new TweetDoesNotExistException();
        }
        Tweet tweet = optionalTweet.get();
        Comment comment = new Comment();
        comment.setLocalUser(user);
        comment.setTweet(tweet);
        comment.setContent(body.getContent());
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Comment savedComment = commentsDAO.save(comment);

        String imageUrl = body.getImageUrl();
        if (imageUrl != null) {
            CommentImages image = new CommentImages();
            image.setUrl(imageUrl);
            image.setComment(savedComment);
            commentImagesDAO.save(image);
        }
        return savedComment;
    }

    public List<LocalUser> searchForUsers(String username) {
        if (username.equalsIgnoreCase("")) {
            return null;
        }
        List<LocalUser> users = localUserDAO.findByUsernameContainsIgnoreCase(username);
        return users;
    }

    public CommentWithDetails fetchCommentDetails(long commentId) throws CommentDoesNotExistsException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Optional<Comment> opComment = commentsDAO.findById(commentId);
        if (!opComment.isPresent()) {
            throw new CommentDoesNotExistsException();
        }
        Comment comment = opComment.get();
        long replyCount = commentsDAO.countByParent_comment(comment);
        CommentWithDetails commentWithDetails = new CommentWithDetails();
        commentWithDetails.setComment(comment);
        commentWithDetails.setReplyCount(replyCount);

        return commentWithDetails;

    }

    public LocalUser updateProfile(LocalUser user, UpdateProfileBody body) throws UserDoesNotExistException {
        Optional<LocalUser> opUser = localUserDAO.findById(user.getId());
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser cUser = opUser.get();
        cUser.setDisplayname(body.getDisplayName());
        if (body.getHeader().equalsIgnoreCase("remove")) {
            cUser.setHeader(null);
        } else {
            cUser.setHeader(body.getHeader());
        }
        cUser.setDescription(body.getDescription());
        cUser.setAvatar(body.getAvatar());

        return localUserDAO.save(cUser);
    }

    public List<UserDetails> fetchUserFollowers(LocalUser user, String username) throws UserDoesNotExistException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser requestedUser = opUser.get();
        List<Friendship> friendships = friendsDAO.findByFriendAndStatusLike(requestedUser, "accepted");
        List<UserDetails> followers = new ArrayList<>();
        for (Friendship f : friendships) {
            UserDetails u = new UserDetails();
            u.setUser(f.getUser());
            boolean isFriendsWith = friendsDAO.existsByUserAndFriendAndStatusLike(user, f.getUser(), "accepted");
            u.setFollowedByCurrentLoggedInUser(isFriendsWith);
            followers.add(u);
        }
        return followers;
    }

    public List<UserDetails> fetchFollowed(LocalUser user, String username) throws UserDoesNotExistException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (!opUser.isPresent()) {
            throw new UserDoesNotExistException();
        }
        LocalUser requestedUser = opUser.get();
        List<Friendship> friendships = friendsDAO.findByUserAndStatusLike(requestedUser, "accepted");
        List<UserDetails> followers = new ArrayList<>();
        for (Friendship f : friendships) {
            UserDetails u = new UserDetails();
            u.setUser(f.getFriend());
            boolean isFriendsWith = friendsDAO.existsByUserAndFriendAndStatusLike(user, f.getFriend(), "accepted");
            u.setFollowedByCurrentLoggedInUser(isFriendsWith);
            followers.add(u);
        }
        return followers;

    }

}
