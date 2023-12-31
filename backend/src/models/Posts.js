import { database } from '../db/db.js';
import { ObjectId } from 'mongodb';
import manipulatePostOutput from '../helpers/manipulatePostOutput.js';
import { getSentiment } from '../helpers/sentimentHelper.js';
import Likes from './Likes.js';
import Comments from './Comments.js';
import User from './User.js';

class Posts {
    constructor() {
        this.collection = database.collection("posts");
    }

    /* ChatGPT usage: No */
    async getAllPost(forumId, userId) {
        const posts = await this.collection.find({forumId: forumId}).toArray();
        const res = [];
        for (let post of posts) {
            const likes = new Likes();
            const postLikes = await likes.getAllLikes(post._id.toString());
            const userLiked = await likes.userLikedPost(post._id.toString(), userId);
            const comments = new Comments();
            const postComments = await comments.getAllComments(post._id.toString());
            const user =  new User();
            const username = await user.getUser(post.writtenBy);
            res.push({...post, likes_count: postLikes.length, comment_count: postComments.length, username: username.name, userLiked: userLiked});
        }
        return manipulatePostOutput(res);
    }

    /* ChatGPT usage: No */
    async getFilteredPosts(forumId, category, userId) {
        const posts = await this.collection.find({forumId: forumId, category: category}).toArray();
        const res = [];
        for (let post of posts) {
            const likes = new Likes();
            const postLikes = await likes.getAllLikes(post._id.toString());
            const userLiked = await likes.userLikedPost(post._id.toString(), userId);
            const comments = new Comments();
            const postComments = await comments.getAllComments(post._id.toString());
            const user =  new User();
            const username = await user.getUser(post.writtenBy);
            res.push({...post, likes_count: postLikes.length, comment_count: postComments.length, username: username.name, userLiked: userLiked});
        }
        return manipulatePostOutput(res);
    };

    /* ChatGPT usage: No */
    async getPostById(userId, postId) {
        const posts = await this.collection.find({ _id : new ObjectId(postId)}).toArray();
        const likes = new Likes();
        const postLikes = await likes.getAllLikes(postId);
        const userLiked = await likes.userLikedPost(postId, userId);
        const comments = new Comments();
        const postComments = await comments.getAllComments(postId);
        const user =  new User();
        const username = await user.getUser(posts[0].writtenBy);
        const post = {...posts[0], likes_count: postLikes.length, comment_count: postComments.length, username: username.name, userLiked: userLiked};
        return manipulatePostOutput([post])[0];
    }

    /* ChatGPT usage: No */
    async addPost(content, userId, forumId) {
        const result = await this.collection.insertOne({ writtenBy: userId, forumId: forumId, dateWritten: new Date(), content: content, category: getSentiment(content) });
        return result.insertedId.toString();
    }

    /* ChatGPT usage: No */
    async editPost(content, postId, userId) {
        const filter = { userId: userId, _id: new ObjectId(postId) }
        const update = { $set: { content, dateWritten: new Date() } }
        const result = await this.collection.updateOne(filter, update);
        return result.matchedCount > 0;
    }
    
    /* ChatGPT usage: No */
    async deletePost(postId, userId) {
        const result = await this.collection.deleteOne({ _id : new ObjectId(postId), writtenBy: userId });
        return result.deletedCount > 0;
    }
}

export default Posts;