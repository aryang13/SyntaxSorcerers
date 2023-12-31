import { database } from '../db/db.js';

class UserCourses {
    constructor() {
        this.collection = database.collection("userCourses");
    }

    /* ChatGPT usage: No */
    async getUserCourses(userId) {
        const response = await this.collection.find({userId: userId}).toArray();
        return response.map(course => {
            return course.courseId
        });
    }

    /* ChatGPT usage: No */
    async addUserCourse(userId, courseId) {
        const doc = { userId: userId, courseId: courseId }
        await this.collection.updateOne(doc, { $set: doc }, {upsert:true});
    }

    /* ChatGPT usage: No */
    async removeUserCourse(userId, courseId) {
        const result = await this.collection.deleteOne({userId: userId, courseId: courseId});
        return result.deletedCount > 0;
    }
}

export default UserCourses;