package com.brothers.development.remotecontrol.communication;

import com.brothers.development.remotecontrol.message.Constants;
import com.brothers.development.remotecontrol.message.Request;

/**
 * Created by iv4nlop3z on 14/11/15.
 */
public class BuilderDatagram {

    /**
     * @param request type Request
     * @return type byte[]
     */
    public static byte[] buildDatagram(Request request) {
        String datagram = messageStructure();
        datagram = datagram.replace(Constants.POSITION_TYPE_MESSAGE, request.getTypeMessage().getValue())
                .replace(Constants.POSITION_MESSAGE, request.getMessage());
        return datagram.getBytes();
    }

    /**
     * @return type String
     */
    private static String messageStructure(){
        StringBuilder datagram = new StringBuilder();
        datagram.append(Constants.HEADER)
                .append(Constants.POSITION_TYPE_MESSAGE)
                .append(Constants.POSITION_MESSAGE)
                .append(Constants.FOOTER)
                .append(Constants.FOOTER);
        return datagram.toString();
    }
}
