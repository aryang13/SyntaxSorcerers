import { database } from '../db/db.js';
import Forums from './Forums.js';
import manipulateForumOutput from '../helpers/manipulateForumOuput.js';

class UserForums {
    constructor() {
        this.collection = database.collection("userForums");
    }

    /* ChatGPT usage: No */
    async getUsersForums(userId) {
        const forums = await this.collection.find({ userId }).toArray();
        const forumIds = forums.map(forum => forum.forumId);
        const model = new Forums();
        const userForums = await model.getForumsByIds(forumIds);
        return manipulateForumOutput(userForums);
    }

    /* ChatGPT usage: No */
    async addUserForum(userId, forumId) {
        await this.collection.insertOne({ userId, forumId });
    }

    /* ChatGPT usage: No */
    async removeUserForum(userId, forumId) {
        await this.collection.deleteOne({ userId, forumId });
    }
}

export default UserForums;