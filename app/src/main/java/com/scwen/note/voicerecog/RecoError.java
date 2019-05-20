package com.scwen.note.voicerecog;

/**
 * Created by scwen on 2019/4/24.
 * QQ ：811733738
 * 作用：
 */
public class RecoError {
    int errorCode;
    int subErrorCode;
    String descMessage;

    public RecoError(int errorCode, int subErrorCode, String descMessage) {
        this.errorCode = errorCode;
        this.subErrorCode = subErrorCode;
        this.descMessage = descMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getSubErrorCode() {
        return subErrorCode;
    }

    public void setSubErrorCode(int subErrorCode) {
        this.subErrorCode = subErrorCode;
    }

    public String getDescMessage() {
        return descMessage == null ? "" : descMessage;
    }

    public void setDescMessage(String descMessage) {
        this.descMessage = descMessage;
    }
}
