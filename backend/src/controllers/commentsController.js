import Comments from '../models/Comments.js';

async function getComments(req, res) {
    const model = new Comments();
    try {
        const comments = model.getAllComments(req.params.post_id);
        return res.json(comments);
    } catch (error) {
        return res.status(500).send(error.message);
    }
}

async function addComment(req, res) {
    const model = new Comments();
    try {
        const commentId = await model.addComment(req.body.content, req.body.postId, req.userId);
        return res.json(commentId);
    } catch (error) {
        return res.status(500).send(error.message);
    }
}

async function editComment(req, res) {
    const model = new Comments();
    try {
        const result = await model.editComment(req.body.content, req.params.commentId, req.userId);
        return result ? res.send("comment edited") : res.status(403).send("not authorized to edit post");
    } catch (error) {
        return res.status(500).send(error.message);
    }
}

async function deleteComment(req, res) {
    const model = new Comments();
    try {
        const result = await model.deleteComment(req.params.commentId, req.userId);
        return result ? res.send("deleted comment") : res.status(403).send("not authorized to delete comment");
    } catch (error) {
        return res.status(500).send(error.message);
    }
}

export { getComments, addComment, editComment, deleteComment }