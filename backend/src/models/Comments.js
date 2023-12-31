import { database } from '../db/db.js';
import manipulateCommentOutput from '../helpers/manipulateCommentOutput.js';
import User from './User.js';
import { ObjectId } from 'mongodb';

class Comments {
    constructor() {
        this.collection = database.collection("comments");
    }

    /* ChatGPT usage: No */
    async getAllComments(postId) {
        const comments = await this.collection.find({ postId: postId }).toArray();
        const res = [];
        for (let comment of comments) {
            const user =  new User();
            const username = await user.getUser(comment.writtenBy);
            res.push({...comment, username: username.name});
        }
        return manipulateCommentOutput(res);
    }

    /* ChatGPT usage: No */
    async getCommentById(commentId) {
        const comments = await this.collection.find({ _id : new ObjectId(commentId)}).toArray();
        const user =  new User();
        const username = await user.getUser(comments[0].writtenBy);
        const comment = {...comments[0], username: username.name};
        return manipulateCommentOutput([comment])[0];
    }

    /* ChatGPT usage: No */
    async addComment(content, postId, userId) {
        const comment = {
            content: content,
            writtenBy: userId,
            postId: postId,
            dateWritten: new Date()
        };
        const result = await this.collection.insertOne(comment);
        return result.insertedId;
    }

    /* ChatGPT usage: No */
    async editComment(content, commentId, userId) {
        const filter = { userId: userId, _id: new ObjectId(commentId) };
        const update = { $set: { content, dateWritten: new Date() } }
        const result = await this.collection.updateOne(filter, update);
        return result.matchedCount > 0;
    }
    
    /* ChatGPT usage: No */
    async deleteComment(commentId, userId) {
        const result = await this.collection.deleteOne({ _id : new ObjectId(commentId), writtenBy: userId });
        return result.deletedCount > 0;
    }
}

export default Comments;