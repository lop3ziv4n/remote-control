package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 12/12/15.
 */
public class Response {

    private TypeMessage typeMessage;
    private String message;

    /**
     * @param input type byte[]
     * @param bytes type int
     */
    public Response(byte[] input, int bytes) {
        analyzeMessage(input, bytes);
    }

    /**
     * @param input type byte[]
     * @param bytes type int
     */
    private void analyzeMessage(byte[] input, int bytes) {
        String readMessage = new String(input, 0, bytes);
        String body = readMessage.substring(readMessage.indexOf(Constants.HEADER),
                readMessage.indexOf(Constants.FOOTER + Constants.FOOTER));
        
        typeMessage = TypeMessage.valueOf(String.valueOf(body.charAt(1)));
        message = body.substring(2);
    }

    /**
     * @return TypeMessage
     */
    public TypeMessage getTypeMessage() {
        return typeMessage;
    }

    /**
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Boolean
     */
    public Boolean isMessageOk() {
        return message.contentEquals(Constants.MESSAGE_OK);
    }

    /**
     * @return Boolean
     */
    public Boolean isMessageNOK(){
        return message.contentEquals(Constants.MESSAGE_NOK);
    }
}
