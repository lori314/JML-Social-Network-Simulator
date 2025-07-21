import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.TagInterface;

public class RedEnvelopeMessage extends Message implements RedEnvelopeMessageInterface {

    public RedEnvelopeMessage(int messageId, int luckyMoney,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        super(messageId,luckyMoney * 5,messagePerson1,messagePerson2);
    }

    public RedEnvelopeMessage(int messageId, int luckyMoney,
        PersonInterface messagePerson1, TagInterface messageTag) {
        super(messageId,luckyMoney * 5,messagePerson1,messageTag);
    }

    public int getMoney() {
        return super.getSocialValue() / 5;
    }
}