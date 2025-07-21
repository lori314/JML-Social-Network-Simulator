import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private final int id;
    private int socialValue;
    private final int type;
    private final PersonInterface person1;
    private final PersonInterface person2;
    private final TagInterface tag;

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.person2 = messagePerson2;
        this.tag = null;
        this.type = 0;
    }

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, TagInterface messageTag) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.tag = messageTag;
        this.type = 1;
        this.person2 = null;
    }

    public int getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public int getSocialValue() {
        return this.socialValue;
    }

    public PersonInterface getPerson1() {
        return this.person1;
    }

    public PersonInterface getPerson2() {
        return this.person2;
    }

    public TagInterface getTag() {
        return this.tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageInterface)) {
            return false;
        }
        MessageInterface other = (MessageInterface) obj;
        return this.id == other.getId();
    }
}
