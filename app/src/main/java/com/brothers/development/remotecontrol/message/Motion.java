package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 22/11/15.
 */
public class Motion implements Request {

    private String address;
    private String personalPurposeOne;
    private String personalPurposeTwo;
    private String enginePowerOne;
    private String enginePowerTwo;

    /**
     * @param address type String
     * @param personalPurposeOne type String
     * @param personalPurposeTwo type String
     * @param enginePowerOne type String
     * @param enginePowerTwo type String
     */
    public Motion(String address, String personalPurposeOne, String personalPurposeTwo,
                  String enginePowerOne, String enginePowerTwo) {
        this.address = address;
        this.personalPurposeOne = personalPurposeOne;
        this.personalPurposeTwo = personalPurposeTwo;
        this.enginePowerOne = enginePowerOne;
        this.enginePowerTwo = enginePowerTwo;

    }

    /**
     * @return TypeMessage
     */
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.MOTION;
    }

    /**
     * @return String
     */
    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        message.append(address.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_ADDRESS_MESSAGE));
        message.append(personalPurposeOne.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_PP_MESSAGE));
        message.append(personalPurposeTwo.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_PP_MESSAGE));
        message.append(enginePowerOne.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_EPW_MESSAGE));
        message.append(enginePowerTwo.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_EPW_MESSAGE));

        return message.toString();
    }

}
