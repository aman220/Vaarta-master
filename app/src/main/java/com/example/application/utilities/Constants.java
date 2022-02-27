package com.example.application.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_PCM_TOKEN = "fcmToken";
    public static final String KEY_USER ="User";
    public static final String KEY_BIO="bio";
    public static final String KEY_PHONE="phone";
    public static final String KEY_COLLECTION_CHAT="chat";
    public static final String KEY_SENDER_ID="senderId";
    public static final String KEY_RECEIVER_ID="receiverId";
    public static final String KEY_RECEIVER_TOKEN="receiverToken";
    public static final String KEY_MESSAGE ="message";
    public static final String KEY_TIMESTAMP="timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS="conversations";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_IMAGE="senderImage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_LAST_MESSAGE="lastMessage";
    public static final String KEY_AVAILABILITY="availability";
    public static final String KEY_USER_STATUS="status";
    public static final String KEY_CONVERSATION_ID="conversationId";


    static int RECORDING_REQUEST_CODE = 3000;

    public static final String FROM_LANGUAGE="from";
    public static final String TO_LANGUAGE="to";

    public static final String FROM_LANGUAGE_CODE = "0";
    public static final String TO_LANGUAGE_CODE = "0";




    public static final String KEY_PREFERENCE_NAME1 = "videoMeetingPreference";
    public static final String KEY_IS_SIGNED_IN1 = "isSignedIn";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";

    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static final String IMAGE_URL = "imageUrl";
    public static final String MEDIA_TYPE = "type";

    public static HashMap<String,String> remoteMsgHeaders=null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders==null) {
            remoteMsgHeaders=new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAANywgq50:APA91bH8iMTGT-D9db-D5rBwuHxYkTclc6IOZOn3dYI_PZp7vLpGL4WkrWc__1BEr-cmEjuyhr-8D4fbPafPq7zIo3tu20VJQLlXSt3MCLZvIOSEKMcvOXgO52L90eexoKN-uckj76mU"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
    public static HashMap<String,String> getRemoteMessageHeaders(){
        HashMap<String,String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAANywgq50:APA91bH8iMTGT-D9db-D5rBwuHxYkTclc6IOZOn3dYI_PZp7vLpGL4WkrWc__1BEr-cmEjuyhr-8D4fbPafPq7zIo3tu20VJQLlXSt3MCLZvIOSEKMcvOXgO52L90eexoKN-uckj76mU"

        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE,"application/json");
        return headers;

    }
}






