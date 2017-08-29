package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 14/11/15.
 */
public interface Constants {

    // Start and end of the datagram
    String HEADER = "$";
    String FOOTER = "??";

    // Replace the datagram body
    String POSITION_TYPE_MESSAGE = "&1";
    String POSITION_MESSAGE = "&2";

    // Length message
    int LENGTH_INITIAL_MESSAGE = 0;
    int LENGTH_PIN_MESSAGE = 4;
    int LENGTH_NAME_MESSAGE = 10;
    int LENGTH_ADDRESS_MESSAGE = 4;
    int LENGTH_PP_MESSAGE = 2;
    int LENGTH_EPW_MESSAGE = 2;

    // Template message
    String MESSAGE_TIMEOUT = "TIMEOUT";
    String MESSAGE_OK = "OK";
    String MESSAGE_NOK = "NOK";

}
