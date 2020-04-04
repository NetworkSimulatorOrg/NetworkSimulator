public interface Protocol {
    int sendMsg(Message msg, int id);
    int recvMsg(Message msg);
    // int handleCollision(); This is done in the recvMsg and send msg.
}
