package Sookmyung.Lingo.exception;

import Sookmyung.Lingo.common.CustomException;
import Sookmyung.Lingo.common.ErrorCode;

public class MemberNotFoundException extends CustomException {
    public MemberNotFoundException() {
        super(ErrorCode.NOT_EXISTS_MEMBER_ID);
    }
}
