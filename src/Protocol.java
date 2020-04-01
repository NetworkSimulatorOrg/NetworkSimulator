public interface Protocol {
    int sendMsg(Message msg);
    int handleCollision();
    int sendManagementMsg(Object msg);
    Object recvManagementMsg();
}
