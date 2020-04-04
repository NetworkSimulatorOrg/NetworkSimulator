public interface Protocol {
    int sendMsg(Message msg);
    int recvMsg(Message msg);
    int handleCollision();
}
