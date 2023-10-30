import User from '../models/User.js';
import UserCourses from '../models/UserCourses.js';
import fs from 'fs';

async function getUser(req, res) {
    const model = new User();
    try {
        const user = await model.getUser(req.userId);
        return res.json(user);
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
}

async function createUser(req, res) {
    const model = new User();
    try {
        const user = {
            userId: req.userId,
            email: req.body.email || "",
            year_level : req.body.year_level || "",
            major : req.body.major || "",
            name : req.body.name || "",
            notification_token : req.body.notification_token || ""
        };
        await model.createUser(user);
        return res.json(user);
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
}

async function updateUser(req, res) {
    const model = new User();
    try {
        await model.updateUser(req.userId, req.body);
        res.json({message: 'User updated'});
    } catch (error) {  
        return res.status(500).json({message: error.message});
    }
}

async function deleteUser(req, res) {
    const model = new User();
    try {
        await model.deleteUser(req.userId);
        res.json({message:'User deleted'});
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
}

async function addFavouriteCourse(req, res) {
    const model = new UserCourses();
    try {
        const courses = await model.addUserCourse(req.userId, req.body.courseId);
        return res.json({message:'Course added'});
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
};

async function getFavouriteCourses(req, res) {
    const model = new UserCourses();
    try {
        const courses = await model.getUserCourses(req.userId);
        return res.json(courses);
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
}

async function removeFavouriteCourse(req, res) {
    const model = new UserCourses();
    const course_id = req.params.course_id;
    try {
        await model.removeUserCourse(req.userId, course_id);
        return res.json({message: 'Course removed'});;
    } catch (error) {
        return res.status(500).json({message: error.message});
    }
}

/* Generated by ChatGPT */
async function getCourseKeywords(req, res) {

    /* User will communicate their interests by selecting a bunch of keywords */

    fs.readFile('./src/jsonFiles/courseCategories.json', 'utf8', (err, data) => {
        if (err) {
            console.error('Error reading the JSON file: ' + err);
            return;
        }

        var categories = [];
        const jsonData = JSON.parse(data);

        Object.keys(jsonData).forEach(category => {
            categories.push(category);
        })

        res.json(categories);
        
    });
}

/* Generated by ChatGPT */
async function getRecommendedCourses(req, res) {

    const { userKeywords } = req.query;
    
    const userKeywordsArray = userKeywords.split(',');
    var reccomendedCourses = {};

    if (Array.isArray(userKeywordsArray)) {
        
        fs.readFile('./src/jsonFiles/courseCategories.json', 'utf8', (err, data) => {
            if (err) {
                console.error('Error reading the JSON file: ' + err);
                return;
            }
    
            const jsonData = JSON.parse(data);
    
            for (var i = 0; i < userKeywordsArray.length; i++) {
                var category = userKeywordsArray[i];
                if (jsonData[category]) {
                    reccomendedCourses[category] = jsonData[category];
    
                }
            }
    
            res.json(reccomendedCourses);
    
        })

    } else {
        
        res.status(400).send("Invalid parameter.")
    }


    

}

export { getUser, createUser, updateUser, deleteUser, addFavouriteCourse, getFavouriteCourses, removeFavouriteCourse, getCourseKeywords, getRecommendedCourses };