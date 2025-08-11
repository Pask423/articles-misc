db = db.getSiblingDB("chats");

db.createUser({
    user: "chats-admin",
    pwd: "admin",
    roles: [{role: "readWrite", db: "chats"}]
});