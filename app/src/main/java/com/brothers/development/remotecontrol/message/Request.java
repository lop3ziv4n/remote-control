package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 12/12/15.
 */
public interface Request {

    /**
     * @return TypeMessage
     */
    TypeMessage getTypeMessage();

    /**
     * @return String
     */
    String getMessage();
}
